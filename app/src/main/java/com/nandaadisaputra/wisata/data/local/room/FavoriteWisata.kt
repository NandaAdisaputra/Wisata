package com.nandaadisaputra.wisata.data.local.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// Anotasi Room untuk menandai bahwa data class ini merupakan tabel database dengan nama "favorite_wisata"
@Entity(tableName = "favorite_wisata")
// Anotasi Parcelize dari Kotlin untuk mempermudah transfer objek ini antar-Activity atau Fragment melalui Intent/Bundle
@Parcelize
data class FavoriteWisata(
    // Anotasi untuk menentukan field 'id' sebagai kunci utama (Primary Key) dari tabel database
    @PrimaryKey
    val id: Int?,

    // Properti untuk menyimpan nama tempat wisata
    val namaWisata: String,

    // Properti untuk menyimpan kategori wisata (misal: Alam, Sejarah, Kuliner)
    val kategori: String,

    // Properti untuk menyimpan lokasi atau alamat tempat wisata
    val lokasi: String,

    // Properti untuk menyimpan harga tiket masuk wisata dalam bentuk angka (integer)
    val hargaTiket: Int,

    // Properti untuk menyimpan deskripsi tempat wisata (bersifat opsional/nullable)
    val deskripsi: String?,

    // Properti untuk menyimpan URL atau path foto tempat wisata (bersifat opsional/nullable)
    val fotoUrl: String?
) : Parcelable // Mengimplementasikan interface Parcelable agar objek dapat dikirim via Intent