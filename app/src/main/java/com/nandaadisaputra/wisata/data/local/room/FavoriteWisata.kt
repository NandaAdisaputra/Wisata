package com.nandaadisaputra.wisata.data.local.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "favorite_wisata")
@Parcelize
data class FavoriteWisata(
    @PrimaryKey
    val id: Int?,
    val namaWisata: String,
    val kategori: String,
    val lokasi: String,
    val hargaTiket: Int,
    val deskripsi: String?,
    val fotoUrl: String?
) : Parcelable