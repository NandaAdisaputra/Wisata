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

// LoginActivity mengelola alur autentikasi masuk pengguna, validasi input kredensial, serta navigasi menuju MainActivity setelah sukses login
class LoginActivity : AppCompatActivity() {

    // Properti binding untuk mengakses view pada layout activity_login.xml secara aman (View Binding)
    private lateinit var binding: ActivityLoginBinding

    // Deklarasi SessionManager untuk mengelola status dan data sesi login pengguna
    private lateinit var sessionManager: SessionManager

    // Inisialisasi AuthViewModel menggunakan ViewModelProvider Factory (AuthViewModelFactory) secara lazy
    private val viewModel: AuthViewModel by viewModels(
        factoryProducer = {
            AuthViewModelFactory(AuthRepository(ApiClient.instance, sessionManager))
        }
    )

    // Method lifecycle yang dipanggil saat Activity pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi SessionManager dengan context Activity
        sessionManager = SessionManager(this)

        // 2. Auto-Login Check: Jika sesi pengguna masih aktif (sudah login), langsung alihkan ke MainActivity tanpa perlu meng-inflate layout XML
        if (sessionManager.isLoggedIn()) {
            startActivity<MainActivity>(clearTask = true)
            finish()
            return
        }

        // 3. Meng-inflate tampilan layout XML jika pengguna belum login
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menyiapkan observer LiveData dan event listener tombol
        setupObservers()
        setupActionListeners()
    }

    // Mengatur listener event klik untuk tombol Login dan tombol navigasi ke halaman Register
    private fun setupActionListeners() {
        // Event listener saat tombol Login diklik
        binding.btnLogin.setOnSingleClickListener {
            // Menyembunyikan papan ketik (keyboard) lunak
            hideKeyboard()

            // Membaca teks input yang sudah dibersihkan dari spasi awal/akhir menggunakan extension property trimmedText
            val username = binding.edtUsername.trimmedText
            val password = binding.edtPassword.trimmedText

            // Reset indikator error lokal pada input field sebelum melakukan validasi ulang
            clearInputErrors(binding.edtUsername, binding.edtPassword)

            // Validasi input form sebelum melakukan panggilan API login
            when {
                // Jika username kosong, tampilkan pesan error dan berikan fokus pada input field
                username.isEmpty() -> {
                    binding.edtUsername.error = "Username tidak boleh kosong!"
                    binding.edtUsername.requestFocus()
                }
                // Jika password kosong, tampilkan pesan error dan berikan fokus pada input field
                password.isEmpty() -> {
                    binding.edtPassword.error = "Password tidak boleh kosong!"
                    binding.edtPassword.requestFocus()
                }
                // Jika seluruh input valid, jalankan fungsi login pada ViewModel
                else -> {
                    viewModel.login(AuthRequest(username, password))
                }
            }
        }

        // Event listener untuk berpindah ke RegisterActivity saat teks/tombol registrasi diklik
        binding.tvGoToRegister.setOnSingleClickListener {
            // Menggunakan extension generic startActivity<T>() untuk navigasi
            startActivity<RegisterActivity>()
        }
    }

    // Memantau perubahan status aliran data (UiState) dari loginState pada AuthViewModel
    private fun setupObservers() {
        observeUiState(
            liveData = viewModel.loginState,
            progressBar = binding.progressBar,
            // Callback yang dieksekusi saat proses login sedang berjalan (loading)
            onLoading = {
                // 1. Matikan tombol login untuk mencegah klik ganda/spam click
                binding.btnLogin.isEnabled = false

                // 2. Bersihkan tampilan error pada input field saat proses loading dimulai
                clearInputErrors(binding.edtUsername, binding.edtPassword)
            },
            // Callback yang dieksekusi ketika proses login berhasil dari API
            onSuccess = { response ->
                binding.btnLogin.isEnabled = true
                showToast(response.message)

                // Sesi login telah otomatis disimpan di AuthRepository saat API bernilai sukses

                // Navigasi ke MainActivity dengan flag CLEAR_TASK (menghapus stack Activity sebelumnya)
                startActivity<MainActivity>(clearTask = true)
                // Menutup LoginActivity agar pengguna tidak dapat kembali ke halaman login melalui tombol Back
                finish()
            },
            // Callback yang dieksekusi ketika proses login mengalami kegagalan/error
            onError = { message ->
                binding.btnLogin.isEnabled = true

                // Tampilkan pesan kesalahan dari API melalui Toast
                showToast(message)
            }
        )
    }
}