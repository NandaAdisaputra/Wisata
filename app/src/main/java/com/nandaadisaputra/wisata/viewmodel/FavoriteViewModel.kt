package com.nandaadisaputra.wisata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisata
import com.nandaadisaputra.wisata.data.local.room.WisataDatabase
import com.nandaadisaputra.wisata.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Kelas ViewModel bertipe AndroidViewModel (memiliki akses ke Application context) untuk mengelola logika data tempat wisata favorit dari Room Database
class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    // Deklarasi variabel instance untuk FavoriteRepository
    private val repository: FavoriteRepository

    // Blok inisialisasi yang dipanggil saat kelas FavoriteViewModel pertama kali dibuat
    init {
        // Mengambil instance DAO dari WisataDatabase menggunakan context application
        val dao = WisataDatabase.getDatabase(application).favoriteWisataDao()
        // Menginstansiasi FavoriteRepository dengan memasukkan DAO yang diperoleh
        repository = FavoriteRepository(dao)
    }

    // Fungsi untuk menambahkan tempat wisata ke dalam daftar favorit
    fun addToFavorite(wisata: FavoriteWisata) {
        // Menjalankan proses penyimpanan di background thread (Dispatchers.IO) agar tidak membebani UI Thread
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToFavorite(wisata)
        }
    }

    // Fungsi untuk menghapus tempat wisata dari daftar favorit berdasarkan ID
    fun removeFromFavorite(id: Int?) {
        // Menjalankan proses penghapusan di background thread (Dispatchers.IO)
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFromFavorite(id)
        }
    }

    // Fungsi untuk memeriksa apakah suatu wisata sudah masuk daftar favorit atau belum berdasarkan ID-nya
    fun checkFavorite(id: Int): LiveData<Boolean> {
        val isFavorite = MutableLiveData<Boolean>()
        // Melakukan pengecekan di background thread
        viewModelScope.launch(Dispatchers.IO) {
            val isFav = repository.checkIsFavorite(id)
            // Memasukkan hasil boolean (true/false) dari background thread ke LiveData menggunakan postValue
            isFavorite.postValue(isFav)
        }
        return isFavorite
    }

    // Fungsi untuk mengambil seluruh daftar wisata favorit yang tersimpan dalam bentuk LiveData
    fun getAllFavorite(): LiveData<List<FavoriteWisata>> = repository.getAllFavorite()
}