package com.nandaadisaputra.wisata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.network.ApiClient
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.network.AuthResponse
import com.nandaadisaputra.wisata.repository.AuthRepository
import com.nandaadisaputra.wisata.utils.UiState
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Inisialisasi repository menggunakan ApiClient yang sudah kita buat
    private val repository = AuthRepository(ApiClient.instance)

    // MutableLiveData: Nilainya bisa diubah-ubah, tapi hanya boleh diakses di dalam ViewModel ini
    private val _loginState = MutableLiveData<UiState<AuthResponse>>()

    // LiveData: Nilainya hanya bisa dibaca (read-only), akan dipantau (observe) oleh Activity
    val loginState: LiveData<UiState<AuthResponse>> = _loginState

    private val _registerState = MutableLiveData<UiState<AuthResponse>>()
    val registerState: LiveData<UiState<AuthResponse>> = _registerState

    /**
     * Fungsi untuk memicu proses login.
     */
    fun login(request: AuthRequest) {
        // 1. Ubah state menjadi Loading (agar UI menampilkan progress bar)
        _loginState.value = UiState.Loading

        // 2. Jalankan proses pemanggilan API di background (viewModelScope)
        viewModelScope.launch {
            val result = repository.login(request)
            // 3. Setelah selesai, update state dengan hasil dari repository (Success / Error)
            _loginState.value = result
        }
    }

    /**
     * Fungsi untuk memicu proses registrasi.
     */
    fun register(request: AuthRequest) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(request)
        }
    }
}