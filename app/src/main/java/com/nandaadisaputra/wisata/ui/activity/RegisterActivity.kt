package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityRegisterBinding
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.utils.UiState
import com.nandaadisaputra.wisata.utils.hide
import com.nandaadisaputra.wisata.utils.hideKeyboard
import com.nandaadisaputra.wisata.utils.setOnSingleClickListener
import com.nandaadisaputra.wisata.utils.show
import com.nandaadisaputra.wisata.utils.showToast
import com.nandaadisaputra.wisata.viewmodel.AuthViewModel

/**
 * RegisterActivity mengelola alur pendaftaran akun baru pengguna,
 * validasi input formulir, serta penanganan status UI berdasarkan respons ViewModel.
 */
class RegisterActivity : AppCompatActivity() {

    // Menampung referensi objek ViewBinding untuk mengakses elemen UI tanpa findViewById
    private lateinit var binding: ActivityRegisterBinding

    // Inisialisasi AuthViewModel menggunakan 'by viewModels()' dari ktx library (Lifecycle Aware)
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout XML activity_register ke dalam ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pasang pemantau (observer) data dari ViewModel
        setupObservers()

        // Listener saat tombol 'Daftar' diklik (menggunakan setOnSingleClickListener pencegah double-click)
        binding.btnRegister.setOnSingleClickListener {
            // Menyembunyikan keyboard virtual agar tidak menutupi indikator loading
            hideKeyboard()

            // Membaca teks dari EditText dan menghapus spasi berlebih di awal/akhir
            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            // Validasi ketersediaan teks pada formulir
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Mengirimkan data pendaftaran ke server melalui ViewModel
                viewModel.register(AuthRequest(username, password))
            } else {
                // Menampilkan Toast peringatan jika input belum lengkap
                showToast("Harap isi semua kolom!")
            }
        }

        // Listener saat teks 'Sudah punya akun? Login' diklik (menggunakan setOnSingleClickListener)
        binding.tvGoToLogin.setOnSingleClickListener {
            // Mengakhiri RegisterActivity dan kembali ke LoginActivity di tumpukan sebelumnya
            finish()
        }
    }

    /**
     * Memantau perubahan status aliran data (UiState) dari registerState pada AuthViewModel.
     */
    private fun setupObservers() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                // 1. Kondisi saat proses request registrasi sedang berjalan di background
                is UiState.Loading -> {
                    // Memunculkan ProgressBar menggunakan extension function show()
                    binding.progressBar.show()
                    // Mematikan tombol register untuk mencegah aksi klik berulang (double-click/spam)
                    binding.btnRegister.isEnabled = false
                }

                // 2. Kondisi saat pendaftaran akun berhasil diproses oleh server
                is UiState.Success -> {
                    // Menyembunyikan ProgressBar menggunakan extension function hide()
                    binding.progressBar.hide()
                    // Memulihkan status tombol register
                    binding.btnRegister.isEnabled = true

                    // Memberikan konfirmasi berhasil kepada pengguna
                    showToast("Registrasi sukses! Silakan Login")

                    // Mengakhiri activity ini agar pengguna langsung kembali ke layar Login
                    finish()
                }

                // 3. Kondisi saat pendaftaran gagal (misal: username sudah terpakai atau koneksi terputus)
                is UiState.Error -> {
                    // Menyembunyikan ProgressBar
                    binding.progressBar.hide()
                    // Mengaktifkan kembali tombol agar pengguna dapat mencoba ulang
                    binding.btnRegister.isEnabled = true

                    // Menampilkan pesan kesalahan resmi dari server/API
                    showToast(state.message)
                }
            }
        }
    }
}