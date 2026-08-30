package com.nandaadisaputra.wisata.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityLoginBinding
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.utils.UiState
import com.nandaadisaputra.wisata.viewmodel.AuthViewModel
import kotlin.getValue

class LoginActivity : AppCompatActivity() {

    // Menggunakan ViewBinding agar tidak perlu findViewById
    private lateinit var binding: ActivityLoginBinding

    // Inisialisasi ViewModel
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Panggil fungsi untuk memantau perubahan data
        setupObservers()

        // Aksi ketika tombol Login diklik
        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            // Validasi input tidak boleh kosong
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Panggil fungsi login di ViewModel
                viewModel.login(AuthRequest(username, password))
            } else {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }

        // Aksi ketika teks "Belum punya akun?" diklik
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupObservers() {
        // Pantau (observe) loginState dari ViewModel
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Tampilkan loading, matikan tombol agar user tidak spam klik
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is UiState.Success -> {
                    // Sembunyikan loading, nyalakan kembali tombol
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true

                    Toast.makeText(this, state.data.message, Toast.LENGTH_SHORT).show()

                    // Arahkan ke MainActivity setelah login sukses
                    val intent = Intent(this, MainActivity::class.java)
                    // Hapus riwayat (stack) agar saat di MainActivity, user klik tombol back tidak kembali ke Login
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is UiState.Error -> {
                    // Sembunyikan loading, nyalakan tombol, tampilkan pesan error dari API
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}