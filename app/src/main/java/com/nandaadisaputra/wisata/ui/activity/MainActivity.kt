package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nandaadisaputra.wisata.R
import com.nandaadisaputra.wisata.databinding.ActivityMainBinding
import com.nandaadisaputra.wisata.ui.fragment.FavoriteFragment
import com.nandaadisaputra.wisata.ui.fragment.HomeFragment
import com.nandaadisaputra.wisata.ui.fragment.ProfileFragment
import com.nandaadisaputra.wisata.utils.setupActionBar

/**
 * MainActivity bertugas sebagai Host Activity yang mengelola BottomNavigationView
 * serta navigasi antar Fragment (Home, Favorite, dan Profile).
 */
class MainActivity : AppCompatActivity() {

    // Properti binding untuk mengakses view pada layout activity_main.xml secara aman
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout XML activity_main ke dalam ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menyiapkan listener Bottom Navigation Bar
        setupBottomNavigation()

        // Tampilkan HomeFragment secara default saat Activity pertama kali dibuat
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), "Daftar Wisata")
        }
    }

    /**
     * Mengatur event listener saat item BottomNavigationView diklik oleh pengguna.
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment(), "Daftar Wisata")
                    true
                }
                R.id.navigation_favorite -> {
                    replaceFragment(FavoriteFragment(), "Wisata Favorit")
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment(), "Profil Saya")
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Memindahkan/mengganti Fragment aktif di dalam FragmentContainerView
     * serta memperbarui judul ActionBar secara dinamis.
     */
    private fun replaceFragment(fragment: Fragment, title: String) {
        setupActionBar(title = title, showBackButton = false)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}