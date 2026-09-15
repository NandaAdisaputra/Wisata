package com.nandaadisaputra.wisata.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteWisata::class], version = 1, exportSchema = false)
abstract class WisataDatabase : RoomDatabase() {

    // Sesuaikan namanya di sini agar sama dengan yang dipanggil di ViewModel
    abstract fun favoriteWisataDao(): FavoriteWisataDao

    companion object {
        @Volatile
        private var INSTANCE: WisataDatabase? = null

        fun getDatabase(context: Context): WisataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WisataDatabase::class.java,
                    "wisata_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}