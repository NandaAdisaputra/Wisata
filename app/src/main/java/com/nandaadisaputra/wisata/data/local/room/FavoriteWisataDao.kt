package com.nandaadisaputra.wisata.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteWisataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteWisata: FavoriteWisata)

    @Query("DELETE FROM favorite_wisata WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int?)

    @Query("SELECT * FROM favorite_wisata WHERE id = :id")
    suspend fun getFavoriteWisataById(id: Int): FavoriteWisata?

    @Query("SELECT * FROM favorite_wisata")
    fun getAllFavorite(): LiveData<List<FavoriteWisata>>
}