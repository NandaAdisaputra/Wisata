package com.nandaadisaputra.wisata.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nandaadisaputra.wisata.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.nandaadisaputra.wisata.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout XML Splash menggunakan ViewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)

        // Memberikan jeda waktu 1.5 detik (1500 ms) agar tampilan splash terlihat oleh pengguna
        lifecycleScope.launch {
            delay(1500)

            // Cek status login dari SessionManager
            val targetActivity = if (sessionManager.isLoggedIn()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }

            // Pindah ke Activity tujuan
            startActivity(Intent(this@SplashActivity, targetActivity))
            finish() // Hapus SplashActivity dari backstack
        }
    }
}