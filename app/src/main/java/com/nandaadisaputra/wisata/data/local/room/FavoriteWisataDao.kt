package com.nandaadisaputra.wisata.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Anotasi Room untuk menandai interface ini sebagai Data Access Object (DAO) yang menyediakan metode akses database
@Dao
interface FavoriteWisataDao {

    // Anotasi untuk operasi Insert. Strategi REPLACE akan menimpa data lama jika ditemukan konflik Primary Key (ID) yang sama
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteWisata: FavoriteWisata)

    // Anotasi Query SQL untuk menghapus data wisata dari tabel 'favorite_wisata' berdasarkan ID tertentu
    @Query("DELETE FROM favorite_wisata WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int?)

    // Anotasi Query SQL untuk mencari dan mengambil satu data tempat wisata berdasarkan ID (mengembalikan null jika tidak ada)
    @Query("SELECT * FROM favorite_wisata WHERE id = :id")
    suspend fun getFavoriteWisataById(id: Int): FavoriteWisata?

    // Anotasi Query SQL untuk mengambil seluruh data tempat wisata favorit yang dibungkus dalam LiveData agar dapat diamati secara reaktif
    @Query("SELECT * FROM favorite_wisata")
    fun getAllFavorite(): LiveData<List<FavoriteWisata>>
}