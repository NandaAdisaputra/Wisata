package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityRegisterBinding
import com.nandaadisaputra.wisata.network.ApiClient
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.repository.AuthRepository
import com.nandaadisaputra.wisata.utils.SessionManager
import com.nandaadisaputra.wisata.utils.UiState
import com.nandaadisaputra.wisata.utils.hide
import com.nandaadisaputra.wisata.utils.hideKeyboard
import com.nandaadisaputra.wisata.utils.setOnSingleClickListener
import com.nandaadisaputra.wisata.utils.show
import com.nandaadisaputra.wisata.utils.showToast
import com.nandaadisaputra.wisata.viewmodel.AuthViewModel
import com.nandaadisaputra.wisata.viewmodel.AuthViewModelFactory

// RegisterActivity mengelola alur pendaftaran akun baru pengguna, validasi input formulir, serta penanganan status UI berdasarkan respons ViewModel
class RegisterActivity : AppCompatActivity() {

    // Properti binding untuk mengakses elemen UI pada layout activity_register.xml secara aman (View Binding)
    private lateinit var binding: ActivityRegisterBinding

    // Inisialisasi AuthViewModel menggunakan ViewModelProvider Factory (AuthViewModelFactory) secara lazy
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(ApiClient.instance, SessionManager(this)))
    }

    // Method siklus hidup (lifecycle) yang dipanggil saat Activity pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Meng-inflate layout XML activity_register ke dalam objek ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        // Menetapkan tampilan UI utama Activity menggunakan root View dari binding
        setContentView(binding.root)

        // Memasang pemantau (observer) LiveData untuk mendengarkan perubahan status dari ViewModel
        setupObservers()

        // Event listener saat tombol 'Daftar' diklik (menggunakan setOnSingleClickListener untuk mencegah klik ganda/spam)
        binding.btnRegister.setOnSingleClickListener {
            // Menyembunyikan papan ketik (keyboard) virtual agar tidak menghalangi indikator loading/UI
            hideKeyboard()

            // Membaca input teks dari EditText dan menghapus spasi berlebih di awal/akhir string
            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            // Mengambil nilai role dari RadioGroup yang dipilih
            val role = if (binding.rbAdmin.isChecked) {
                "admin"
            } else {
                "user"
            }
            // Validasi kelengkapan teks pada formulir pendaftaran
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Mengirimkan permintaan data pendaftaran ke server melalui AuthViewModel
                viewModel.register(AuthRequest(username, password, role))
            } else {
                // Menampilkan pesan Toast peringatan jika ada input field yang belum terisi
                showToast("Harap isi semua kolom!")
            }
        }

        // Event listener saat teks 'Sudah punya akun? Login' diklik untuk kembali ke halaman login
        binding.tvGoToLogin.setOnSingleClickListener {
            // Mengakhiri RegisterActivity dan kembali ke Activity sebelumnya (LoginActivity) pada tumpukan (backstack)
            finish()
        }
    }

    // Memantau perubahan status aliran data (UiState) dari registerState pada AuthViewModel
    private fun setupObservers() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                // 1. Kondisi saat proses request registrasi sedang berjalan di background thread
                is UiState.Loading -> {
                    // Memunculkan indikator ProgressBar menggunakan fungsi ekstensi show()
                    binding.progressBar.show()
                    // Mematikan tombol register agar pengguna tidak dapat menekan tombol secara berulang saat loading
                    binding.btnRegister.isEnabled = false
                }

                // 2. Kondisi saat pendaftaran akun berhasil diproses dan dikonfirmasi oleh server
                is UiState.Success -> {
                    // Menyembunyikan ProgressBar menggunakan fungsi ekstensi hide()
                    binding.progressBar.hide()
                    // Mengaktifkan kembali status interaksi tombol register
                    binding.btnRegister.isEnabled = true

                    // Memberikan pesan konfirmasi keberhasilan registrasi kepada pengguna
                    showToast("Registrasi sukses! Silakan Login")

                    // Mengakhiri activity ini agar pengguna otomatis kembali ke halaman Login
                    finish()
                }

                // 3. Kondisi saat pendaftaran gagal (misal: username telah terdaftar atau koneksi internet terputus)
                is UiState.Error -> {
                    // Menyembunyikan indikator ProgressBar
                    binding.progressBar.hide()
                    // Mengaktifkan kembali tombol agar pengguna dapat memperbaiki data dan mencoba lagi
                    binding.btnRegister.isEnabled = true

                    // Menampilkan pesan kesalahan resmi yang didapatkan dari respons server/API
                    showToast(state.message)
                }
            }
        }
    }
}