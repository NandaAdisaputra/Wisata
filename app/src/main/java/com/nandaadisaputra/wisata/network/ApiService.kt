package com.nandaadisaputra.wisata.network

import com.nandaadisaputra.wisata.model.WisataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

//interface ApiService {
//
//    // Pastikan endpoint ini sudah sesuai dengan route API di servermu
//    @GET("apiwisata/places")
//
//    suspend fun getWisata(): Response<WisataResponse>
//}
/**
 * Interface ini mendefinisikan rute (endpoint) dari API yang kita gunakan.
 */
interface ApiService {

    // Menggunakan anotasi @GET untuk mengakses endpoint API wisata
    @GET("apiwisata/places")
    suspend fun getWisata(
        // Menambahkan anotasi @Query("page") untuk mengirim parameter halaman ke server
        // Contoh: /apiwisata/places?page=1, /apiwisata/places?page=2, dst.
        @Query("page") page: Int
    ): Response<WisataResponse>


    // Anotasi @POST menandakan metode HTTP POST.
    // Anotasi @Body akan otomatis mengubah objek AuthRequest menjadi format JSON (Raw).
    @POST("apiwisata/auth.php?action=login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("apiwisata/auth.php?action=register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>
}