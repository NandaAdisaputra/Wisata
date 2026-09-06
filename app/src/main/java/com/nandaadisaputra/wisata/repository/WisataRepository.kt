package com.nandaadisaputra.wisata.repository

import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.model.WisataResponse
import com.nandaadisaputra.wisata.network.ApiClient
import com.nandaadisaputra.wisata.utils.UiState // Sesuaikan dengan lokasi package UiState Anda

class WisataRepository {

    /**
     * Memanggil API untuk mengambil daftar tempat wisata secara paginasi.
     * @param page Nomor halaman yang ingin dimuat.
     * @return UiState<WisataResponse> berupa Success atau Error.
     */
    suspend fun getWisata(page: Int): UiState<WisataResponse> {
        return try {
            val response = ApiClient.instance.getWisata(page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    UiState.Success(body)
                } else {
                    UiState.Error("Data wisata kosong dari server")
                }
            } else {
                UiState.Error("Gagal memuat data (${response.code()}): ${response.message()}")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi internet")
        }
    }

    /**
     * Memanggil API untuk melakukan pencarian berdasarkan kata kunci tempat wisata.
     * @param keyword Kata kunci nama/lokasi wisata yang dicari user.
     * @return UiState<WisataResponse> berupa Success atau Error.
     */
    suspend fun searchWisata(keyword: String): UiState<WisataResponse> {
        return try {
            val response = ApiClient.instance.searchWisata(keyword)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    UiState.Success(body)
                } else {
                    UiState.Error("Hasil pencarian tidak ditemukan")
                }
            } else {
                UiState.Error("Pencarian gagal (${response.code()}): ${response.message()}")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi internet")
        }
    }

    /**
     * Memanggil API untuk mengambil detail tempat wisata berdasarkan ID.
     * @param id ID unik tempat wisata.
     * @return UiState<Wisata> berupa Success atau Error.
     */
    suspend fun getDetailWisata(id: Int): UiState<Wisata> {
        return try {
            val response = ApiClient.instance.getDetailWisata(id)
            if (response.isSuccessful) {
                val body = response.body()
                // Mengambil objek data detail (sesuaikan dengan struktur response API Anda)
                val detailData = body?.data
                if (detailData != null) {
                    UiState.Success(detailData)
                } else {
                    UiState.Error("Detail wisata tidak ditemukan")
                }
            } else {
                UiState.Error("Gagal memuat detail (${response.code()}): ${response.message()}")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi internet")
        }
    }
}