package com.nandaadisaputra.wisata.repository

import androidx.lifecycle.LiveData
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisataDao
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisata

class FavoriteRepository(private val favoriteDao: FavoriteWisataDao) {

    suspend fun addToFavorite(wisata: FavoriteWisata) {
        // Memanggil fungsi insertFavorite yang ada di DAO
        favoriteDao.insertFavorite(wisata)
    }

    suspend fun removeFromFavorite(id: Int?) {
        // Memanggil fungsi deleteFavoriteById yang ada di DAO
        favoriteDao.deleteFavoriteById(id)
    }

    // Mengubah return type menjadi Boolean agar lebih mudah digunakan
    suspend fun checkIsFavorite(id: Int): Boolean {
        // Jika data ditemukan (tidak null), maka mengembalikan nilai true. Jika null = false.
        val favorite = favoriteDao.getFavoriteWisataById(id)
        return favorite != null
    }

    fun getAllFavorite(): LiveData<List<FavoriteWisata>> {
        return favoriteDao.getAllFavorite()
    }
}