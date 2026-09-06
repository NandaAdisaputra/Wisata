package com.nandaadisaputra.wisata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.repository.WisataRepository
import com.nandaadisaputra.wisata.utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel bertugas mengelola data UI dan logika bisnis untuk layar Wisata.
 * ViewModel akan bertahan dari perubahan konfigurasi (seperti rotasi layar).
 */
class WisataViewModel : ViewModel() {

    // Instance repository untuk melakukan pemanggilan API
    private val repository = WisataRepository()

    // LiveData privat yang menyimpan kondisi UI (Loading, Success, Error)
    private val _wisataState = MutableLiveData<UiState<List<Wisata>>>()
    // LiveData publik yang diamati (observe) oleh Activity/Fragment (Immutable)
    val wisataState: LiveData<UiState<List<Wisata>>> = _wisataState

    // LiveData privat untuk menandai status footer loading (pagiansi bawah)
    private val _isLoadMore = MutableLiveData<Boolean>()
    // LiveData publik untuk memantau status footer loading dari Activity/Fragment
    val isLoadMore: LiveData<Boolean> = _isLoadMore

    // List lokal mutable untuk menampung seluruh akumulasi data wisata dari paginasi
    private val currentList = mutableListOf<Wisata>()

    // Halaman yang sedang dimuat saat ini (dimulai dari halaman 1)
    private var currentPage = 1
    // Total halaman yang tersedia dari response server (default 1)
    private var totalPage = 1
    // Flag penanda untuk mencegah pemanggilan 'load more' ganda secara bersamaan
    var isLoadingMore = false
    // Flag penanda apakah pengguna sedang dalam mode pencarian
    private var isSearching = false

    /**
     * Memuat data tempat wisata secara paginasi (Load More & Refresh)
     * @param isRefresh Jika true, akan mereset halaman dan memuat data dari awal (halaman 1)
     */
    fun fetchWisata(isRefresh: Boolean = false) {
        // Mencegah pemanggilan paginasi biasa jika sedang dalam mode pencarian
        if (isSearching && !isRefresh) return

        // Jika tombol/gesture Refresh dipicu (misal: SwipeRefreshLayout)
        if (isRefresh) {
            currentPage = 1             // Reset halaman ke awal
            totalPage = 1               // Reset total halaman
            isLoadingMore = false        // Reset status load more
            isSearching = false          // Matikan mode pencarian
            currentList.clear()          // Bersihkan cache list lokal
        }

        // Jika halaman saat ini sudah melebihi total halaman server dan bukan refresh, batalkan request
        if (currentPage > totalPage && !isRefresh) return

        viewModelScope.launch {
            // Tentukan indikator loading berdasarkan posisi halaman
            if (currentPage == 1) {
                // Tampilkan status Loading utama di tengah layar jika memuat halaman pertama
                _wisataState.value = UiState.Loading
            } else {
                // Tampilkan indikator loading footer di bawah RecyclerView untuk halaman lanjutan
                isLoadingMore = true
                _isLoadMore.value = true
            }

            try {
                // Jeda buatan 2 detik khusus halaman lanjutan agar animasi loading footer terlihat jelas
                if (currentPage > 1) delay(2000)

                // Memanggil repository yang mengembalikan UiState<WisataResponse>
                when (val result = repository.getWisata(currentPage)) {
                    is UiState.Success -> {
                        val body = result.data
                        val newData = body.data ?: emptyList()
                        totalPage = body.meta?.totalPage ?: 1

                        // Tambahkan data baru dari server ke dalam list lokal
                        currentList.addAll(newData)

                        // Kirim salinan list data terbaru ke UI sebagai UiState.Success
                        _wisataState.value = UiState.Success(currentList.toList())

                        // Naikkan nomor halaman jika belum mencapai batas maksimum totalPage
                        if (currentPage <= totalPage) currentPage++
                    }

                    is UiState.Error -> {
                        // Jika gagal pada halaman pertama, tampilkan layar error utama.
                        // Jika gagal pada load more, pertahankan list yang sudah ada agar tidak hilang.
                        if (currentPage == 1 || currentList.isEmpty()) {
                            _wisataState.value = UiState.Error(result.message)
                        } else {
                            _wisataState.value = UiState.Success(currentList.toList())
                        }
                    }

                    is UiState.Loading -> {
                        // Di-handle oleh penanda _wisataState.value = UiState.Loading di awal
                    }
                }
            } finally {
                // Matikan status loading footer setelah proses paginasi selesai
                isLoadingMore = false
                _isLoadMore.value = false
            }
        }
    }

    /**
     * Memanggil API untuk mencari tempat wisata berdasarkan kata kunci (Keyword)
     * @param keyword Kata kunci pencarian dari SearchView/EditText
     */
    fun searchWisata(keyword: String) {
        // Jika kata kunci kosong/blank, kembalikan ke daftar wisata normal
        if (keyword.isBlank()) {
            fetchWisata(isRefresh = true)
            return
        }

        // Tandai bahwa mode pencarian sedang aktif
        isSearching = true

        viewModelScope.launch {
            // Tampilkan indikator Loading utama di tengah layar
            _wisataState.value = UiState.Loading

            when (val result = repository.searchWisata(keyword)) {
                is UiState.Success -> {
                    val searchData = result.data.data ?: emptyList()

                    // Kirim data hasil pencarian langsung ke UI
                    _wisataState.value = UiState.Success(searchData)

                    // Set totalPage ke 1 agar sistem paginasi bawah tidak aktif saat mode pencarian
                    totalPage = 1
                }

                is UiState.Error -> {
                    // Teruskan pesan error dari repository ke UI
                    _wisataState.value = UiState.Error(result.message)
                }

                is UiState.Loading -> {
                    // Di-handle oleh penanda _wisataState.value = UiState.Loading di awal
                }
            }
        }
    }
}