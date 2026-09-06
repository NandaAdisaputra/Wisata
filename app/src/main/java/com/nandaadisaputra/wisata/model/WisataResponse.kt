package com.nandaadisaputra.wisata.model

import com.google.gson.annotations.SerializedName

/**
 * Model response untuk daftar tempat wisata (List)
 */
data class WisataResponse(
    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<Wisata>? = null,

    @SerializedName("meta")
    val meta: Meta? = null
)

/**
 * Model response untuk detail satu tempat wisata (Single Object)
 */
data class DetailWisataResponse(
    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: Wisata? = null,

    @SerializedName("meta")
    val meta: Meta? = null
)

/**
 * Model metadata untuk paginasi
 */
data class Meta(
    @SerializedName("total_data")
    val totalData: Int? = null,

    @SerializedName("total_page")
    val totalPage: Int? = null,

    @SerializedName("current_page")
    val currentPage: Int? = null,

    @SerializedName("per_page")
    val perPage: Int? = null
)