package com.nandaadisaputra.wisata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.network.AuthResponse
import com.nandaadisaputra.wisata.repository.AuthRepository
import com.nandaadisaputra.wisata.utils.UiState
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Encapsulation Concept:
    // MutableLiveData (_loginState): Privat, nilainya bisa diubah dari dalam ViewModel.
    private val _loginState = MutableLiveData<UiState<AuthResponse>>()
    // LiveData (loginState): Publik dan Read-Only, di-observe oleh Activity/Fragment.
    val loginState: LiveData<UiState<AuthResponse>> = _loginState

    private val _registerState = MutableLiveData<UiState<AuthResponse>>()
    val registerState: LiveData<UiState<AuthResponse>> = _registerState

    private val _logoutState = MutableLiveData<Boolean>()
    val logoutState: LiveData<Boolean> = _logoutState

    /**
     * Memicu eksekusi proses Login.
     */
    fun login(request: AuthRequest) {
        // Set UI ke status Loading sebelum request dikirim
        _loginState.value = UiState.Loading

        // viewModelScope.launch: Menjalankan coroutine yang akan otomatis dibatalkan jika ViewModel dihancurkan
        viewModelScope.launch {
            _loginState.value = repository.login(request)
        }
    }

    /**
     * Memicu eksekusi proses Registrasi.
     */
    fun register(request: AuthRequest) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(request)
        }
    }

    /**
     * Memicu pembersihan sesi di repository dan memperbarui status logout.
     */
    fun logout() {
        repository.logout()
        _logoutState.value = true // Beri tahu UI bahwa pengguna berhasil logout
    }

    /**
     * Mengecek apakah pengguna sedang dalam keadaan login.
     */
    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    /**
     * Mengambil nama pengguna yang tersimpan di sesi.
     */
    fun getUsername(): String? = repository.getUsername()

    /**
     * Mengambil token autentikasi yang tersimpan di sesi.
     */
    fun getToken(): String? = repository.getToken()

    /**
     * Mengambil ID pengguna yang tersimpan di sesi.
     */
    fun getUserId(): Int = repository.getUserId()
}