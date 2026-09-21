package com.nandaadisaputra.wisata.network

import com.nandaadisaputra.wisata.model.BaseResponse
import com.nandaadisaputra.wisata.model.DetailWisataResponse
import com.nandaadisaputra.wisata.model.WisataResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

// Interface Retrofit yang mendefinisikan rute (endpoint) API untuk komunikasi jaringan/server
interface ApiService {

    // Mengambil daftar tempat wisata dari server secara paginasi (per halaman)
    // Endpoint: GET /apiwisata/places.php?page={page}
    @GET("apiwisata/places.php")
    suspend fun getWisata(
        @Query("page") page: Int // Parameter query 'page' untuk memilih nomor halaman
    ): Response<WisataResponse>

    // Mencari data tempat wisata berdasarkan kata kunci pencarian
    // Endpoint: GET /apiwisata/places.php?search={keyword}
    @GET("apiwisata/places.php")
    suspend fun searchWisata(
        @Query("search") keyword: String // Parameter query 'search' berisi kata kunci pencarian
    ): Response<WisataResponse>

    // Mengambil detail lengkap satu tempat wisata berdasarkan ID
    // Endpoint: GET /apiwisata/places.php?id={id}
    @GET("apiwisata/places.php")
    suspend fun getDetailWisata(
        @Query("id") id: Int // Parameter query 'id' wisata yang ingin diambil detailnya
    ): Response<DetailWisataResponse>

    // Otentikasi login pengguna dengan mengirim data credential pada request body (JSON)
    // Endpoint: POST /apiwisata/auth.php?action=login
    @POST("apiwisata/auth.php?action=login")
    suspend fun login(
        @Body request: AuthRequest // Objek AuthRequest yang dikirim sebagai HTTP Request Body (JSON)
    ): Response<AuthResponse>

    // Pendaftaran (register) akun pengguna baru
    // Endpoint: POST /apiwisata/auth.php?action=register
    @POST("apiwisata/auth.php?action=register")
    suspend fun register(
        @Body request: AuthRequest // Objek AuthRequest berisi data akun baru (JSON)
    ): Response<AuthResponse>

    // Menambahkan data tempat wisata baru beserta unggahan file gambar (menggunakan format Multipart/form-data)
    // Endpoint: POST /apiwisata/places.php
    @Multipart
    @POST("apiwisata/places.php")
    suspend fun addWisata(
        @Part("nama_wisata") namaWisata: RequestBody, // Parameter teks untuk nama tempat wisata
        @Part("kategori") kategori: RequestBody,       // Parameter teks untuk kategori wisata
        @Part("lokasi") lokasi: RequestBody,           // Parameter teks untuk lokasi wisata
        @Part("harga_tiket") hargaTiket: RequestBody,   // Parameter teks untuk harga tiket masuk
        @Part("deskripsi") deskripsi: RequestBody,     // Parameter teks untuk deskripsi wisata
        @Part foto: MultipartBody.Part                // Parameter berkas/file gambar foto wisata
    ): Response<BaseResponse>

    // Memperbarui (update) data tempat wisata yang sudah ada, dengan opsi memperbarui foto (foto bersifat opsional/nullable)
    // Endpoint: POST /apiwisata/places.php
    @Multipart
    @POST("apiwisata/places.php")
    suspend fun updateWisata(
        @Part("id") id: RequestBody,                   // ID wisata yang datanya akan diperbarui
        @Part("nama_wisata") namaWisata: RequestBody, // Teks nama wisata baru/perbaruan
        @Part("kategori") kategori: RequestBody,       // Teks kategori baru/perbaruan
        @Part("lokasi") lokasi: RequestBody,           // Teks lokasi baru/perbaruan
        @Part("harga_tiket") hargaTiket: RequestBody,   // Teks harga tiket baru/perbaruan
        @Part("deskripsi") deskripsi: RequestBody,     // Teks deskripsi baru/perbaruan
        @Part foto: MultipartBody.Part? = null        // Berkas foto baru (opsional, bernilai null jika gambar tidak diganti)
    ): Response<BaseResponse>

    // Menghapus data tempat wisata dari server berdasarkan ID
    // Endpoint: DELETE /apiwisata/places.php?id={id}
    @DELETE("apiwisata/places.php")
    suspend fun deleteWisata(
        @Query("id") id: Int // Parameter query 'id' tempat wisata yang ingin dihapus
    ): Response<BaseResponse>
}