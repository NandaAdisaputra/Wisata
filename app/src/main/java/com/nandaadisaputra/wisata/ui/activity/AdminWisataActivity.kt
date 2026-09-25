package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nandaadisaputra.wisata.R
import com.nandaadisaputra.wisata.databinding.ActivityMainBinding
import com.nandaadisaputra.wisata.ui.fragment.FavoriteFragment
import com.nandaadisaputra.wisata.ui.fragment.HomeAdminFragment
import com.nandaadisaputra.wisata.ui.fragment.ProfileFragment
import com.nandaadisaputra.wisata.utils.setupActionBar

// Activity khusus untuk halaman admin.
// Bertugas mengelola BottomNavigationView dan fragment admin.
class AdminWisataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Fragment awal untuk admin harus HomeAdminFragment
        if (savedInstanceState == null) {
            replaceFragment(
                HomeAdminFragment(),
                "Daftar Wisata"
            )
        }
    }

    /**
     * Mengatur navigasi BottomNavigationView.
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.navigation_home -> {
                    replaceFragment(
                        HomeAdminFragment(),
                        "Daftar Wisata"
                    )
                    true
                }

                R.id.navigation_favorite -> {
                    replaceFragment(
                        FavoriteFragment(),
                        "Wisata Favorit"
                    )
                    true
                }

                R.id.navigation_profile -> {
                    replaceFragment(
                        ProfileFragment(),
                        "Profil Saya"
                    )
                    true
                }

                else -> false
            }
        }
    }

    /**
     * Mengganti fragment dan mengatur ActionBar.
     */
    private fun replaceFragment(
        fragment: Fragment,
        title: String
    ) {
        setupActionBar(
            title = title,
            showBackButton = false
        )

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container,
                fragment
            )
            .commit()
    }
}