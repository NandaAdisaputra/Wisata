package com.nandaadisaputra.wisata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.repository.WisataRepository
import kotlinx.coroutines.launch

class WisataViewModel : ViewModel() {

    // Inisialisasi repository sebagai jembatan untuk mengambil data dari API
    private val repository = WisataRepository()

    // _wisataList bersifat private dan bisa diubah (MutableLiveData) HANYA di dalam ViewModel ini
    private val _wisataList = MutableLiveData<List<Wisata>>()
    // wisataList bersifat public (LiveData) dan HANYA bisa diamati (observe) oleh Activity/Fragment (Immutable)
    val wisataList: LiveData<List<Wisata>> = _wisataList

    // LiveData untuk memantau status loading (berguna untuk menampilkan/menyembunyikan ProgressBar di UI)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData untuk menyimpan dan memantau pesan error jika terjadi kegagalan jaringan atau server
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Fungsi untuk mengambil data wisata dari server.
     * Biasanya dipanggil oleh Activity/Fragment saat tampilan pertama kali dimuat.
     */
    fun fetchWisata() {
        // viewModelScope.launch digunakan untuk menjalankan Coroutine (proses asynchronous di background)
        // Ini memastikan proses ambil data (yang mungkin memakan waktu) tidak membekukan UI (Main Thread)
        viewModelScope.launch {

            // 1. Mulai proses: Set loading menjadi true agar UI menampilkan indikator loading
            _isLoading.value = true

            try {
                // 2. Eksekusi: Memanggil fungsi dari repository untuk menembak API
                val response = repository.getWisata()

                // 3. Validasi: Mengecek apakah respons API sukses (kode HTTP 2xx) dan isi body-nya tidak kosong
                if (response.isSuccessful && response.body() != null) {

                    // 4. Sukses: response.body() menghasilkan objek wrapper (WisataResponse).
                    // Kita memanggil properti .data untuk mengambil daftar wisatanya (List<Wisata>).
                    // Elvis operator (?: emptyList()) memastikan aplikasi tidak crash jika data bernilai null.
                    _wisataList.value = response.body()?.data ?: emptyList()

                } else {
                    // 5. Gagal dari Server: Menangkap error seperti 404 (Not Found) atau 500 (Server Error)
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }

            } catch (e: Exception) {
                // 6. Gagal Jaringan: Menangkap error aplikasi gagal konek (misal: kuota habis, internet putus, timeout)
                _errorMessage.value = e.localizedMessage ?: "Terjadi kesalahan koneksi"

            } finally {
                // 7. Selesai: Blok ini akan selalu dieksekusi di akhir, entah hasilnya sukses atau gagal.
                // Set loading menjadi false agar indikator loading di UI menghilang.
                _isLoading.value = false
            }
        }
    }
}