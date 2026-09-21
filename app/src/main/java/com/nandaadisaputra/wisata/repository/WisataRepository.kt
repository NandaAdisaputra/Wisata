package com.nandaadisaputra.wisata.repository

import com.nandaadisaputra.wisata.model.BaseResponse
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.model.WisataResponse
import com.nandaadisaputra.wisata.network.ApiClient
import com.nandaadisaputra.wisata.utils.UiState
import okhttp3.MultipartBody
import okhttp3.RequestBody

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

    /**
     * Memanggil API untuk menambahkan tempat wisata baru (CREATE).
     * Menggunakan Multipart karena terdapat upload gambar.
     */
    suspend fun addWisata(
        namaWisata: RequestBody,
        kategori: RequestBody,
        lokasi: RequestBody,
        hargaTiket: RequestBody,
        deskripsi: RequestBody,
        foto: MultipartBody.Part
    ): UiState<BaseResponse> {
        return try {
            val response = ApiClient.instance.addWisata(
                namaWisata, kategori, lokasi, hargaTiket, deskripsi, foto
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    UiState.Success(body)
                } else {
                    UiState.Error("Gagal menambahkan wisata: Response kosong")
                }
            } else {
                UiState.Error("Gagal menambahkan wisata (${response.code()}): ${response.message()}")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi internet")
        }
    }

    /**
     * Memanggil API untuk memperbarui data tempat wisata (UPDATE).
     * Parameter foto dibuat opsional (nullable) agar user tidak wajib mengunggah gambar ulang jika hanya mengubah teks.
     */
    suspend fun updateWisata(
        id: RequestBody,
        namaWisata: RequestBody,
        kategori: RequestBody,
        lokasi: RequestBody,
        hargaTiket: RequestBody,
        deskripsi: RequestBody,
        foto: MultipartBody.Part? = null
    ): UiState<BaseResponse> {
        return try {
            val response = ApiClient.instance.updateWisata(
                id, namaWisata, kategori, lokasi, hargaTiket, deskripsi, foto
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    UiState.Success(body)
                } else {
                    UiState.Error("Gagal memperbarui wisata: Response kosong")
                }
            } else {
                UiState.Error("Gagal memperbarui wisata (${response.code()}): ${response.message()}")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi internet")
        }
    }

    /**
     * Memanggil API untuk menghapus tempat wisata berdasarkan ID (DELETE).
     */
    suspend fun deleteWisata(id: Int): UiState<BaseResponse> {
        return try {
            val response = ApiClient.instance.deleteWisata(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    UiState.Success(body)
                } else {
                    UiState.Error("Gagal menghapus wisata: Response kosong")
                }
            } else {
                UiState.Error("Gagal menghapus wisata (${response.code()}): ${response.message()}")
            }
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi internet")
        }
    }
}