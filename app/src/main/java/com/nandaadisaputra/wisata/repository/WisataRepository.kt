package com.nandaadisaputra.wisata.repository

import com.nandaadisaputra.wisata.model.WisataResponse
import com.nandaadisaputra.wisata.network.ApiClient
import retrofit2.Response

class WisataRepository {

    /**
     * Mengambil data wisata dari API.
     * Menggunakan 'suspend' karena fungsi ini berjalan secara asynchronous (Coroutine).
     * Tipe kembalian didefinisikan secara eksplisit sebagai Response<WisataResponse>
     * agar struktur datanya jelas dan mudah dipahami.
     */
    suspend fun getWisata(): Response<WisataResponse> {
        return ApiClient.instance.getWisata()
    }
}