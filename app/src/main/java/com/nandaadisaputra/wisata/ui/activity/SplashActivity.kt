package com.nandaadisaputra.wisata.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nandaadisaputra.wisata.databinding.ActivitySplashBinding
import com.nandaadisaputra.wisata.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Activity untuk menampilkan splash screen dan menentukan halaman awal berdasarkan status login dan role pengguna.
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)

        lifecycleScope.launch {
            delay(1500)

            val targetActivity = if (sessionManager.isLoggedIn()) {

                // Ambil role dari session
                val role = sessionManager.getRole()

                if (role.equals("admin", ignoreCase = true)) {
                    AdminWisataActivity::class.java
                } else {
                    MainActivity::class.java
                }

            } else {
                LoginActivity::class.java
            }

            startActivity(
                Intent(this@SplashActivity, targetActivity)
            )

            finish()
        }
    }
}