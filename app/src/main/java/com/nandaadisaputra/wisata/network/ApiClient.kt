package com.nandaadisaputra.wisata.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Base URL dari server kamu.
    // Aturan penting: Selalu pastikan diakhiri dengan garis miring (/)
    private const val BASE_URL = "https://smkmuh3pucanggading.sch.id/"

    val instance: ApiService by lazy {

        // 1. Membuat Interceptor untuk melihat log (request & response API) di Logcat
        // Pastikan kamu menambahkan library logging di build.gradle:
        // implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Menampilkan seluruh isi JSON di Logcat
        }

        // 2. Mengatur OkHttpClient
        // Di sini kita memasukkan interceptor dan mengatur batas waktu (timeout) agar aplikasi tidak hang jika server lambat
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS) // Waktu maksimal untuk koneksi ke server
            .readTimeout(30, TimeUnit.SECONDS)    // Waktu maksimal untuk menunggu balasan server
            .build()

        // 3. Membangun Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Masukkan konfigurasi OkHttpClient yang sudah dibuat di atas
            .addConverterFactory(GsonConverterFactory.create()) // Mengonversi JSON mentah menjadi Data Class (Model)
            .build()

        // 4. Membuat instance dari Interface ApiService
        retrofit.create(ApiService::class.java)
    }
}