package com.nandaadisaputra.wisata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.model.BaseResponse
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.repository.WisataRepository
import com.nandaadisaputra.wisata.utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

// Kelas ViewModel yang bertugas mengelola status data (UI State), logika bisnis, dan komunikasi dengan Repository untuk data Wisata
class WisataViewModel : ViewModel() {

    // Inisialisasi sumber data (Repository) yang menangani pemanggilan API/Remote Data
    private val repository = WisataRepository()

    // LiveData privat (Mutable) untuk menyimpan UI State daftar tempat wisata
    private val _wisataState = MutableLiveData<UiState<List<Wisata>>>()
    // LiveData publik (Read-Only) yang diamati oleh UI (Fragment/Activity) untuk pembaruan daftar wisata
    val wisataState: LiveData<UiState<List<Wisata>>> = _wisataState

    // LiveData privat untuk mengontrol visibilitas indikator loading pagination (Load More)
    private val _isLoadMore = MutableLiveData<Boolean>()
    // LiveData publik untuk mendeteksi status proses Load More pada UI
    val isLoadMore: LiveData<Boolean> = _isLoadMore

    // LiveData privat untuk menyimpan status/respon dari operasi CRUD (Tambah, Edit, Hapus)
    private val _crudState = MutableLiveData<UiState<BaseResponse>?>()
    // LiveData publik yang diamati oleh UI untuk merespons hasil aksi CRUD
    val crudState: LiveData<UiState<BaseResponse>?> = _crudState

    // Daftar buffer lokal untuk menggabungkan data antar-halaman pada fitur pagination
    private val currentList = mutableListOf<Wisata>()

    // Variabel kontrol untuk pelacakan halaman pagination dan status permintaan
    private var currentPage = 1
    private var totalPage = 1
    var isLoadingMore = false
    private var isSearching = false

    // Fungsi utama untuk mengambil daftar data wisata (mendukung pagination dan swipe refresh)
    fun fetchWisata(isRefresh: Boolean = false) {
        // Jika sedang dalam pencarian dan bukan minta refresh, abaikan permintaan fetch pagination biasa
        if (isSearching && !isRefresh) return

        // Jika melakukan Refresh (misal: SwipeRefresh): Reset halaman, status, dan bersihkan daftar data lokal
        if (isRefresh) {
            currentPage = 1
            totalPage = 1
            isLoadingMore = false
            isSearching = false
            currentList.clear()
        }

        // Jika halaman saat ini melebihi total halaman yang tersedia dan bukan mode refresh, hentikan fetching
        if (currentPage > totalPage && !isRefresh) return

        // Jalankan proses asynchronous menggunakan Coroutine pada viewModelScope
        viewModelScope.launch {
            // Jika memuat halaman pertama, tampilkan State Loading utama (ProgressBar tengah)
            if (currentPage == 1) {
                _wisataState.value = UiState.Loading
            } else {
                // Jika memuat halaman selanjutnya (Pagination), aktifkan penanda Load More (Loading bawah)
                isLoadingMore = true
                _isLoadMore.value = true
            }

            try {
                // Memberikan penundaan buatan 1 detik untuk pengalaman visual pagination yang halus pada halaman > 1
                if (currentPage > 1) delay(1000)

                // Mengambil data dari repository berdasarkan halaman aktif
                when (val result = repository.getWisata(currentPage)) {
                    is UiState.Success -> {
                        val body = result.data
                        val newData = body.data ?: emptyList()
                        totalPage = body.meta?.totalPage ?: 1

                        // Tambahkan data baru ke dalam daftar lokal dan kirim daftar gabungan terbaru ke UI
                        currentList.addAll(newData)
                        _wisataState.value = UiState.Success(currentList.toList())

                        // Naikkan nomor halaman jika masih tersedia halaman berikutnya
                        if (currentPage <= totalPage) currentPage++
                    }

                    is UiState.Error -> {
                        // Jika halaman 1 atau list masih kosong lalu error, kirimkan Error State ke UI
                        if (currentPage == 1 || currentList.isEmpty()) {
                            _wisataState.value = UiState.Error(result.message)
                        } else {
                            // Jika error terjadi saat load more, tetap tampilkan data yang sudah berhasil dimuat sebelumnya
                            _wisataState.value = UiState.Success(currentList.toList())
                        }
                    }

                    is UiState.Loading -> {}
                }
            } finally {
                // Nonaktifkan indikator Load More setelah proses selesai
                isLoadingMore = false
                _isLoadMore.value = false
            }
        }
    }

    // Fungsi untuk melakukan pencarian tempat wisata berdasarkan kata kunci (keyword)
    fun searchWisata(keyword: String) {
        // Jika kata kunci kosong, kembalikan ke daftar wisata normal (halaman awal)
        if (keyword.isBlank()) {
            fetchWisata(isRefresh = true)
            return
        }

        // Tandai bahwa mode pencarian sedang aktif
        isSearching = true

        viewModelScope.launch {
            // Tampilkan state loading sebelum pemanggilan API pencarian
            _wisataState.value = UiState.Loading
            when (val result = repository.searchWisata(keyword)) {
                is UiState.Success -> {
                    val searchData = result.data.data ?: emptyList()
                    _wisataState.value = UiState.Success(searchData)
                    totalPage = 1 // Reset total halaman menjadi 1 saat mode pencarian
                }
                is UiState.Error -> {
                    _wisataState.value = UiState.Error(result.message)
                }
                is UiState.Loading -> {}
            }
        }
    }

    // --- FUNGSI CRUD REAL API ---

    // Fungsi untuk menambah data wisata baru menggunakan format RequestBody & MultipartBody
    fun addWisata(
        namaWisata: RequestBody,
        kategori: RequestBody,
        lokasi: RequestBody,
        hargaTiket: RequestBody,
        deskripsi: RequestBody,
        foto: MultipartBody.Part
    ) {
        viewModelScope.launch {
            _crudState.value = UiState.Loading
            _crudState.value = repository.addWisata(namaWisata, kategori, lokasi, hargaTiket, deskripsi, foto)
        }
    }

    // Fungsi untuk memperbarui data wisata yang sudah ada (foto bersifat opsional / nullable)
    fun updateWisata(
        id: RequestBody,
        namaWisata: RequestBody,
        kategori: RequestBody,
        lokasi: RequestBody,
        hargaTiket: RequestBody,
        deskripsi: RequestBody,
        foto: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            _crudState.value = UiState.Loading
            _crudState.value = repository.updateWisata(id, namaWisata, kategori, lokasi, hargaTiket, deskripsi, foto)
        }
    }

    // Fungsi untuk menghapus data wisata berdasarkan ID
    fun deleteWisata(id: Int) {
        viewModelScope.launch {
            _crudState.value = UiState.Loading
            _crudState.value = repository.deleteWisata(id)
        }
    }

    // Memereset CRUD state setelah UI memproses aksi (mencegah komit ganda/pemicu ulang observer)
    fun resetCrudState() {
        _crudState.value = null
    }
}