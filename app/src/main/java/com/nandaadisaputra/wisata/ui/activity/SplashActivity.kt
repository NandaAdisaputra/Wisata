package com.nandaadisaputra.wisata.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nandaadisaputra.wisata.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.nandaadisaputra.wisata.databinding.ActivitySplashBinding

// SplashActivity mengelola tampilan layar pembuka (splash screen), penundaan waktu awal (delay), serta pengalihan navigasi otomatis berdasarkan status login pengguna
class SplashActivity : AppCompatActivity() {

    // Properti binding untuk mengakses elemen UI pada layout activity_splash.xml secara aman (View Binding)
    private lateinit var binding: ActivitySplashBinding

    // Method siklus hidup (lifecycle) yang dipanggil saat Activity pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Meng-inflate tata letak XML activity_splash ke dalam objek ViewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        // Menetapkan tampilan UI utama Activity menggunakan root View dari binding
        setContentView(binding.root)

        // Menginstansiasi SessionManager untuk memeriksa status sesi login pengguna
        val sessionManager = SessionManager(this)

        // Menjalankan coroutine yang terikat pada siklus hidup Activity (lifecycleScope) agar aman dari kebocoran memori (memory leak)
        lifecycleScope.launch {
            // Memberikan penundaan waktu selama 1,5 detik (1500 milidetik) agar tampilan splash screen terlihat oleh pengguna
            delay(1500)

            // Memeriksa status login dari SessionManager untuk menentukan Activity tujuan
            val targetActivity = if (sessionManager.isLoggedIn()) {
                MainActivity::class.java // Mengarahkan ke MainActivity jika pengguna sudah login
            } else {
                LoginActivity::class.java // Mengarahkan ke LoginActivity jika pengguna belum login
            }

            // Memulai Activity tujuan menggunakan Intent
            startActivity(Intent(this@SplashActivity, targetActivity))

            // Mengakhiri SplashActivity dan menghapusnya dari tumpukan (backstack) agar tidak dapat diakses kembali via tombol Back
            finish()
        }
    }
}