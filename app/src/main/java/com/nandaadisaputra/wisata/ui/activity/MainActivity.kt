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

// MainActivity bertugas sebagai Host Activity yang mengelola BottomNavigationView serta navigasi antar Fragment (Home, Favorite, dan Profile)
class MainActivity : AppCompatActivity() {

    // Properti binding untuk mengakses elemen view pada layout activity_main.xml secara aman (View Binding)
    private lateinit var binding: ActivityMainBinding

    // Method siklus hidup (lifecycle) yang dipanggil saat Activity pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Meng-inflate layout XML activity_main ke dalam objek ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Menetapkan tampilan UI utama Activity menggunakan root View dari binding
        setContentView(binding.root)

        // Menyiapkan event listener untuk mendeteksi navigasi pada Bottom Navigation Bar
        setupBottomNavigation()

        // Memastikan fragment default (HomeFragment) hanya dimuat saat Activity pertama kali dibuat (bukan saat rekonstruksi/rotasi layar)
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), "Daftar Wisata")
        }
    }

    // Mengatur listener saat item BottomNavigationView diklik oleh pengguna
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Jika menu Home diklik, berpindah ke HomeFragment dan perbarui judul Action Bar
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment(), "Daftar Wisata")
                    true
                }
                // Jika menu Favorite diklik, berpindah ke FavoriteFragment dan perbarui judul Action Bar
                R.id.navigation_favorite -> {
                    replaceFragment(FavoriteFragment(), "Wisata Favorit")
                    true
                }
                // Jika menu Profile diklik, berpindah ke ProfileFragment dan perbarui judul Action Bar
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment(), "Profil Saya")
                    true
                }
                else -> false
            }
        }
    }

    // Fungsi pembantu untuk memindahkan/mengganti Fragment di dalam kontainer serta memperbarui judul Action Bar secara dinamis
    private fun replaceFragment(fragment: Fragment, title: String) {
        // Memanggil ekstensi/fungsi utility untuk mengatur judul ActionBar dan menyembunyikan tombol kembali
        setupActionBar(title = title, showBackButton = false)

        // Menggunakan FragmentManager untuk menjalankan transaksi penggantian Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Mengganti tampilan kontainer dengan Fragment baru
            .commit() // Eksekusi/terapkan transaksi fragment
    }
}