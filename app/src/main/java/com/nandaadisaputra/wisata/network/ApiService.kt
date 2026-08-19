package com.nandaadisaputra.wisata.network

import com.nandaadisaputra.wisata.model.WisataResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    // Pastikan endpoint ini sudah sesuai dengan route API di servermu
    @GET("apiwisata/places")

    suspend fun getWisata(): Response<WisataResponse>
}