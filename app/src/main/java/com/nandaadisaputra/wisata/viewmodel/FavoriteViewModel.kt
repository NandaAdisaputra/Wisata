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

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi Repository
    private val repository: FavoriteRepository

    init {
        // Panggil database dan DAO di blok init, lalu masukkan ke Repository
        // Catatan: Pastikan nama fungsi pemanggil DAO di WisataDatabase adalah favoriteWisataDao()
        val dao = WisataDatabase.getDatabase(application).favoriteWisataDao()
        repository = FavoriteRepository(dao)
    }

    fun addToFavorite(wisata: FavoriteWisata) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToFavorite(wisata)
        }
    }

    fun removeFromFavorite(id: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFromFavorite(id)
        }
    }

    fun checkFavorite(id: Int): LiveData<Boolean> {
        val isFavorite = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            // Memanggil fungsi dari repository yang sekarang mereturn Boolean
            val isFav = repository.checkIsFavorite(id)
            isFavorite.postValue(isFav) // Langsung masukkan nilai true/false
        }
        return isFavorite
    }

    fun getAllFavorite(): LiveData<List<FavoriteWisata>> = repository.getAllFavorite()
}