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

        // 2. Auto-Login Check dengan pembagian Role
        if (sessionManager.isLoggedIn()) {
            val role = sessionManager.getRole()
            if (role == "admin") {
                startActivity<AdminWisataActivity>(clearTask = true) // Navigasi ke Dashboard Admin
            } else {
                startActivity<MainActivity>(clearTask = true)  // Navigasi ke Dashboard User
            }
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
    // Fungsi ini bertugas untuk mengatur semua aksi klik (listener) pada elemen antarmuka (UI) di halaman Login
    private fun setupActionListeners() {

        // Mengatur aksi ketika tombol Login (btnLogin) diklik.
        // Menggunakan ekstensi setOnSingleClickListener untuk mencegah pengguna menekan tombol berkali-kali secara cepat (mencegah spam klik yang bisa membuat API error).
        binding.btnLogin.setOnSingleClickListener {

            // Menyembunyikan papan ketik (keyboard virtual) dari layar agar tidak menghalangi tampilan saat proses login (loading) berjalan.
            hideKeyboard()

            // 1. Mengambil data input dari pengguna
            // Mengambil teks dari kolom EditText username.
            // 'trimmedText' adalah fungsi ekstensi yang otomatis menghapus spasi kosong yang tidak sengaja terketik di awal atau akhir kata.
            val username = binding.edtUsername.trimmedText

            // Mengambil teks dari kolom EditText password dan membersihkan spasi berlebih.
            val password = binding.edtPassword.trimmedText

            // 2. Menentukan Role berdasarkan pilihan pengguna di antarmuka (RadioGroup)
            // Mengecek apakah komponen RadioButton untuk Admin (rbAdmin) sedang dipilih/dicentang oleh pengguna.
            val role = if (binding.rbAdmin.isChecked) {
                "admin" // Jika dicentang, maka nilai variabel role diatur menjadi "admin"
            } else {
                "user"  // Jika tidak dicentang (berarti RadioButton User yang terpilih), maka nilai role diatur menjadi "user"
            }

            // Menghapus pesan error (tulisan merah) yang mungkin masih menempel di kolom input dari percobaan login yang gagal sebelumnya.
            clearInputErrors(binding.edtUsername, binding.edtPassword)

            // 3. Proses Validasi Form Input
            // Memeriksa kondisi kolom input satu per satu menggunakan blok 'when' (pengganti if-else berantai di Kotlin).
            when {
                // Pengecekan 1: Jika kolom username ternyata masih kosong
                username.isEmpty() -> {
                    // Munculkan pesan peringatan di bawah kolom username
                    binding.edtUsername.error = "Username tidak boleh kosong!"
                    // Pindahkan kursor secara otomatis ke kolom username agar pengguna langsung bisa mengetik
                    binding.edtUsername.requestFocus()
                }

                // Pengecekan 2: Jika kolom password ternyata masih kosong
                password.isEmpty() -> {
                    // Munculkan pesan peringatan di bawah kolom password
                    binding.edtPassword.error = "Password tidak boleh kosong!"
                    // Pindahkan kursor secara otomatis ke kolom password
                    binding.edtPassword.requestFocus()
                }

                // Kondisi default (else): Jika semua kolom input sudah terisi dengan benar (lolos validasi)
                else -> {
                    // 4. Mengirimkan data kredensial ke Server/API
                    // Menggabungkan username, password, dan role yang sudah diambil ke dalam satu model data AuthRequest.
                    // Kemudian, memerintahkan ViewModel untuk menjalankan fungsi login() dengan membawa data tersebut ke server.
                    viewModel.login(AuthRequest(username, password, role))
                }
            }
        }

        // Mengatur aksi ketika tombol atau teks "Belum punya akun? Daftar" (tvGoToRegister) diklik.
        binding.tvGoToRegister.setOnSingleClickListener {
            // Melakukan navigasi atau perpindahan halaman menuju layar Pendaftaran (RegisterActivity).
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

                // Pengecekan role saat login berhasil
                val role = response.user?.role
                if (role == "admin") {
                    startActivity<AdminWisataActivity>(clearTask = true) // Navigasi ke Dashboard Admin
                } else {
                    startActivity<MainActivity>(clearTask = true)  // Navigasi ke Dashboard User
                }
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