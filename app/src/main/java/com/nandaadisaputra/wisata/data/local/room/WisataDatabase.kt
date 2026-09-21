package com.nandaadisaputra.wisata.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Anotasi Room untuk mendefinisikan kelas database, mendaftarkan daftar entitas (tabel), versi database, dan opsi export schema
@Database(entities = [FavoriteWisata::class], version = 1, exportSchema = false)
abstract class WisataDatabase : RoomDatabase() {

    // Fungsi abstrak yang mengembalikan interface FavoriteWisataDao agar bisa diakses oleh Repository/ViewModel
    abstract fun favoriteWisataDao(): FavoriteWisataDao

    // Companion object digunakan untuk mengimplementasikan pola Singleton agar database hanya diinstansiasi satu kali
    companion object {
        // Anotasi @Volatile memastikan bahwa nilai INSTANCE selalu diperbarui di memori utama dan terlihat oleh semua thread
        @Volatile
        private var INSTANCE: WisataDatabase? = null

        // Fungsi untuk mendapatkan instance database tunggal (Singleton Pattern)
        fun getDatabase(context: Context): WisataDatabase {
            // Jika INSTANCE belum null (sudah ada), kembalikan INSTANCE.
            // Jika masih null, jalankan blok synchronized agar tidak terjadi pembuatan instance ganda dari beberapa thread secara bersamaan
            return INSTANCE ?: synchronized(this) {
                // Membangun instance database baru menggunakan Room.databaseBuilder
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WisataDatabase::class.java,
                    "wisata_database" // Nama file database lokal yang disimpan di perangkat
                ).build()

                // Menyimpan instance yang baru dibuat ke variabel INSTANCE
                INSTANCE = instance

                // Mengembalikan objek instance database
                instance
            }
        }
    }
}