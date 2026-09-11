package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityLoginBinding
import com.nandaadisaputra.wisata.network.ApiClient
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.repository.AuthRepository
import com.nandaadisaputra.wisata.utils.SessionManager
import com.nandaadisaputra.wisata.utils.clearInputErrors
import com.nandaadisaputra.wisata.utils.hideKeyboard
import com.nandaadisaputra.wisata.utils.observeUiState
import com.nandaadisaputra.wisata.utils.setOnSingleClickListener
import com.nandaadisaputra.wisata.utils.showToast
import com.nandaadisaputra.wisata.utils.startActivity
import com.nandaadisaputra.wisata.utils.trimmedText
import com.nandaadisaputra.wisata.viewmodel.AuthViewModel
import com.nandaadisaputra.wisata.viewmodel.AuthViewModelFactory

/**
 * LoginActivity mengelola alur autentikasi masuk pengguna,
 * validasi input kredensial, serta navigasi menuju MainActivity setelah sukses login.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: AuthViewModel by viewModels(
        factoryProducer = {
            AuthViewModelFactory(AuthRepository(ApiClient.instance, sessionManager))
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi SessionManager
        sessionManager = SessionManager(this)

        // 2. Auto-Login Check: Jika sudah login, langsung pindah ke MainActivity tanpa inflate layout XML
        if (sessionManager.isLoggedIn()) {
            startActivity<MainActivity>(clearTask = true)
            finish()
            return
        }

        // 3. Inflate tampilan jika pengguna belum login
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupActionListeners()
    }

    /**
     * Mengatur listener event klik untuk tombol Login dan navigasi ke RegisterActivity.
     */
    private fun setupActionListeners() {
        binding.btnLogin.setOnSingleClickListener {
            hideKeyboard()

            // Membaca teks terisi menggunakan extension property trimmedText
            val username = binding.edtUsername.trimmedText
            val password = binding.edtPassword.trimmedText

            // Reset indikator error lokal menggunakan fungsi variadic clearInputErrors()
            clearInputErrors(binding.edtUsername, binding.edtPassword)

            when {
                username.isEmpty() -> {
                    binding.edtUsername.error = "Username tidak boleh kosong!"
                    binding.edtUsername.requestFocus()
                }
                password.isEmpty() -> {
                    binding.edtPassword.error = "Password tidak boleh kosong!"
                    binding.edtPassword.requestFocus()
                }
                else -> {
                    viewModel.login(AuthRequest(username, password))
                }
            }
        }

        binding.tvGoToRegister.setOnSingleClickListener {
            // Menggunakan extension generic startActivity<T>()
            startActivity<RegisterActivity>()
        }
    }

    /**
     * Memantau perubahan status aliran data (UiState) dari loginState pada AuthViewModel.
     */
    private fun setupObservers() {
        observeUiState(
            liveData = viewModel.loginState,
            progressBar = binding.progressBar,
            onLoading = {
                // 1. Matikan tombol login untuk mencegah spam click
                binding.btnLogin.isEnabled = false

                // 2. Bersihkan tampilan error pada input field saat proses loading dimulai
                clearInputErrors(binding.edtUsername, binding.edtPassword)
            },
            onSuccess = { response ->
                binding.btnLogin.isEnabled = true
                showToast(response.message)

                // Sesi login telah otomatis disimpan di AuthRepository saat API bernilai sukses

                // Navigasi ke MainActivity dengan flag CLEAR_TASK
                startActivity<MainActivity>(clearTask = true)
                finish()
            },
            onError = { message ->
                binding.btnLogin.isEnabled = true

                // Tampilkan pesan kesalahan dari API melalui Toast
                showToast(message)
            }
        )
    }
}