package com.nandaadisaputra.wisata.repository

import com.nandaadisaputra.wisata.network.ApiService
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.network.AuthResponse
import com.nandaadisaputra.wisata.utils.UiState

class AuthRepository(private val apiService: ApiService) {

    /**
     * Fungsi 'suspend' berjalan di dalam Coroutine agar tidak memblokir Main Thread (UI).
     * Jika memblokir UI, aplikasi akan terasa lag (freeze).
     */
    suspend fun login(request: AuthRequest): UiState<AuthResponse> {
        return try {
            val response = apiService.login(request)

            // Cek apakah response dari server sukses (kode 200-299) dan status JSON adalah "success"
            if (response.isSuccessful && response.body()?.status == "success") {
                UiState.Success(response.body()!!)
            } else {
                // Tangkap pesan error dari API, atau beri pesan default jika kosong
                UiState.Error(response.body()?.message ?: "Login gagal. Periksa kembali data Anda.")
            }
        } catch (e: Exception) {
            // Menangkap error dari sistem Android (misal: tidak ada koneksi internet)
            UiState.Error(e.message ?: "Terjadi kesalahan jaringan.")
        }
    }

    suspend fun register(request: AuthRequest): UiState<AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body()?.status == "success") {
                UiState.Success(response.body()!!)
            } else {
                UiState.Error(response.body()?.message ?: "Registrasi gagal.")
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Terjadi kesalahan jaringan.")
        }
    }
}