package com.nandaadisaputra.wisata.model

import com.google.gson.annotations.SerializedName

data class Wisata(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("nama_wisata")
    val namaWisata: String?,

    @SerializedName("kategori")
    val kategori: String?,

    @SerializedName("lokasi")
    val lokasi: String?,

    @SerializedName("harga_tiket")
    val hargaTiket: Int?,

    @SerializedName("deskripsi")
    val deskripsi: String?,

    @SerializedName("foto")
    val foto: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("foto_url")
    val fotoUrl: String?
)