package com.nandaadisaputra.wisata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.repository.WisataRepository
import kotlinx.coroutines.delay // Diperlukan untuk menggunakan fungsi delay
import kotlinx.coroutines.launch

class WisataViewModel : ViewModel() {
    private val repository = WisataRepository()

    private val _wisataList = MutableLiveData<List<Wisata>>()
    val wisataList: LiveData<List<Wisata>> = _wisataList

    // LiveData untuk indikator loading utama (muncul di tengah saat pertama buka aplikasi)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData khusus untuk memantau status load more (muncul di bagian bawah saat scroll)
    private val _isLoadMore = MutableLiveData<Boolean>()
    val isLoadMore: LiveData<Boolean> = _isLoadMore

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentPage = 1
    private var totalPage = 1
    var isLoadingMore = false

    /**
     * Fungsi mengambil data wisata dengan dukungan pagination dan kontrol status loading.
     */
    fun fetchWisata(isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 1
            isLoadingMore = false
        }

        if (currentPage > totalPage && !isRefresh) return

        viewModelScope.launch {
            // Tentukan jenis loading berdasarkan halaman yang diminta
            if (currentPage == 1) {
                _isLoading.value = true
            } else {
                isLoadingMore = true
                _isLoadMore.value = true // Nyalakan status loading bawah
            }

            try {
                // TAMBAHAN: Jika ini halaman lanjutan (load more), berikan jeda buatan (delay) selama 2 detik.
                // Tujuannya agar animasi ProgressBar di bawah sempat terlihat oleh mata
                // meskipun koneksi internet perangkat sangat kencang.
                // (Baris ini bisa dihapus/dikomentari nanti jika aplikasi sudah siap dirilis ke production)
                if (currentPage > 1) {
                    delay(5000)
                }

                val response = repository.getWisata(currentPage)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val newData = body.data ?: emptyList()
                    totalPage = body.meta?.totalPage ?: 1

                    // Ambil data lama jika halaman > 1, atau buat list baru jika halaman 1
                    val currentList = if (currentPage == 1) {
                        mutableListOf()
                    } else {
                        _wisataList.value?.toMutableList() ?: mutableListOf()
                    }

                    // Gabungkan data lama dengan data halaman berikutnya
                    currentList.addAll(newData)
                    _wisataList.value = currentList

                    if (currentPage <= totalPage) {
                        currentPage++
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Terjadi kesalahan koneksi"
            } finally {
                // Matikan semua indikator loading setelah proses selesai (sukses/gagal)
                _isLoading.value = false
                isLoadingMore = false
                _isLoadMore.value = false // Matikan status loading bawah
            }
        }
    }
}