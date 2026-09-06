package com.nandaadisaputra.wisata.network

import com.nandaadisaputra.wisata.model.DetailWisataResponse
import com.nandaadisaputra.wisata.model.WisataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interface ini mendefinisikan rute (endpoint) dari API yang digunakan.
 */
interface ApiService {

    /**
     * Mengambil daftar tempat wisata secara paginasi.
     * Endpoint: /apiwisata/places.php?page=1
     */
    @GET("apiwisata/places.php")
    suspend fun getWisata(
        @Query("page") page: Int
    ): Response<WisataResponse>

    /**
     * Mencari data tempat wisata berdasarkan kata kunci.
     * Endpoint: /apiwisata/places.php?search=keyword
     */
    @GET("apiwisata/places.php")
    suspend fun searchWisata(
        @Query("search") keyword: String
    ): Response<WisataResponse>

    /**
     * Mengambil detail satu tempat wisata berdasarkan ID (Versi Query Parameter).
     * Endpoint: /apiwisata/places.php?id=12
     */
    @GET("apiwisata/places.php")
    suspend fun getDetailWisata(
        @Query("id") id: Int
    ): Response<DetailWisataResponse>

    /**
     * Otentikasi Login pengguna.
     */
    @POST("apiwisata/auth.php?action=login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    /**
     * Pendaftaran (Register) pengguna baru.
     */
    @POST("apiwisata/auth.php?action=register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<AuthResponse>
}