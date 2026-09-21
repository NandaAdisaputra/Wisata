package com.nandaadisaputra.wisata.repository

import androidx.lifecycle.LiveData
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisataDao
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisata

// Kelas Repository yang mengelola akses data favorit pada database lokal (Room) melalui interface FavoriteWisataDao
class FavoriteRepository(private val favoriteDao: FavoriteWisataDao) {

    // Fungsi suspend untuk menambahkan data tempat wisata ke dalam daftar favorit di database lokal
    suspend fun addToFavorite(wisata: FavoriteWisata) {
        // Memanggil fungsi insertFavorite dari DAO untuk menyimpan objek FavoriteWisata
        favoriteDao.insertFavorite(wisata)
    }

    // Fungsi suspend untuk menghapus data wisata dari daftar favorit berdasarkan ID
    suspend fun removeFromFavorite(id: Int?) {
        // Memanggil fungsi deleteFavoriteById dari DAO untuk menghapus baris data berdasarkan ID
        favoriteDao.deleteFavoriteById(id)
    }

    // Fungsi suspend untuk mengecek status apakah suatu tempat wisata dengan ID tertentu sudah masuk favorit
    suspend fun checkIsFavorite(id: Int): Boolean {
        // Mengambil objek favorit dari DAO berdasarkan ID
        val favorite = favoriteDao.getFavoriteWisataById(id)
        // Mengembalikan nilai true jika data ditemukan (tidak null), atau false jika tidak ada/null
        return favorite != null
    }

    // Fungsi untuk mengambil seluruh data wisata favorit yang tersimpan dalam bentuk LiveData (dapat diamati secara reaktif)
    fun getAllFavorite(): LiveData<List<FavoriteWisata>> {
        // Mengembalikan LiveData daftar FavoriteWisata langsung dari DAO
        return favoriteDao.getAllFavorite()
    }
}