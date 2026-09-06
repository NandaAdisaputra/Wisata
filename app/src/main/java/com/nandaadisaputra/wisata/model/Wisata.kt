package com.nandaadisaputra.wisata.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Wisata(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("nama_wisata")
    val namaWisata: String? = null,

    @SerializedName("kategori")
    val kategori: String? = null,

    @SerializedName("lokasi")
    val lokasi: String? = null,

    @SerializedName("harga_tiket")
    val hargaTiket: Int? = null,

    @SerializedName("deskripsi")
    val deskripsi: String? = null,

    @SerializedName("foto")
    val foto: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("foto_url")
    val fotoUrl: String? = null
) : Parcelable