package com.nandaadisaputra.wisata.repository

import com.nandaadisaputra.wisata.network.ApiService
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.network.AuthResponse
import com.nandaadisaputra.wisata.utils.SessionManager
import com.nandaadisaputra.wisata.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    /**
     * Memproses autentikasi login pengguna.
     * Menggunakan Dispatchers.IO untuk memastikan proses jaringan berjalan di Background Thread.
     */
    suspend fun login(request: AuthRequest): UiState<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.status == "success") {
                val token = body.data?.token
                val user = body.data?.user

                // Simpan token dan data user ke SharedPreferences jika login sukses
                if (!token.isNullOrEmpty() && user != null) {
                    sessionManager.saveSession(
                        token = token,
                        userId = user.id,
                        username = user.username,
                        role = user.role
                    )
                }

                UiState.Success(body)
            } else {
                // Pada HTTP Error (400/401/500), response.body() bernilai null.
                // Pesan error diambil dari response.errorBody() atau fallback default message.
                val errorMessage = body?.message
                    ?: response.errorBody()?.string()
                    ?: "Login gagal. Periksa kembali data Anda."
                UiState.Error(errorMessage)
            }
        } catch (e: IOException) {
            UiState.Error("Tidak ada koneksi internet. Periksa jaringan Anda.")
        } catch (e: HttpException) {
            UiState.Error("Terjadi kesalahan server (${e.code()}).")
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan tidak terduga.")
        }
    }

    /**
     * Memproses registrasi akun pengguna baru.
     */
    suspend fun register(request: AuthRequest): UiState<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.status == "success") {
                UiState.Success(body)
            } else {
                val errorMessage = body?.message
                    ?: response.errorBody()?.string()
                    ?: "Registrasi gagal."
                UiState.Error(errorMessage)
            }
        } catch (e: IOException) {
            UiState.Error("Tidak ada koneksi internet. Periksa jaringan Anda.")
        } catch (e: HttpException) {
            UiState.Error("Terjadi kesalahan server (${e.code()}).")
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Terjadi kesalahan tidak terduga.")
        }
    }

    /**
     * Menghapus seluruh data sesi login (Logout).
     */
    fun logout() {
        sessionManager.clearSession()
    }

    /**
     * Memeriksa status login pengguna dari SessionManager.
     */
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    /**
     * Mengambil nama pengguna dari sesi tersimpan.
     */
    fun getUsername(): String? = sessionManager.getUsername()

    /**
     * Mengambil ID pengguna dari sesi tersimpan.
     */
    fun getUserId(): Int = sessionManager.getUserId()

    /**
     * Mengambil token autentikasi dari sesi tersimpan.
     */
    fun getToken(): String? = sessionManager.getToken()
    // Tambahkan fungsi untuk mengambil role di bagian bawah AuthRepository:
    fun getRole(): String? = sessionManager.getRole()
}