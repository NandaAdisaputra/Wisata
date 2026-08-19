package com.nandaadisaputra.wisata.model

import com.google.gson.annotations.SerializedName

data class WisataResponse(
    @SerializedName("code")
    val code: Int?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: List<Wisata>?,

    @SerializedName("meta")
    val meta: Meta?
)

data class Meta(
    @SerializedName("total_data")
    val totalData: Int?,

    @SerializedName("total_page")
    val totalPage: Int?,

    @SerializedName("current_page")
    val currentPage: Int?,

    @SerializedName("per_page")
    val perPage: Int?
)