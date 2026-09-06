package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nandaadisaputra.wisata.adapter.WisataAdapter
import com.nandaadisaputra.wisata.databinding.ActivityMainBinding
import com.nandaadisaputra.wisata.utils.UiState
import com.nandaadisaputra.wisata.utils.addInfiniteScrollListener
import com.nandaadisaputra.wisata.utils.hide
import com.nandaadisaputra.wisata.utils.hideKeyboard
import com.nandaadisaputra.wisata.utils.observeUiState
import com.nandaadisaputra.wisata.utils.onQueryTextChanged
import com.nandaadisaputra.wisata.utils.setupActionBar
import com.nandaadisaputra.wisata.utils.showToast
import com.nandaadisaputra.wisata.utils.startDetailWisataActivity
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel

/**
 * MainActivity bertugas mengelola tampilan utama daftar wisata,
 * pencarian (SearchView), penyegaran data (SwipeRefreshLayout),
 * pagination (Infinite Scroll), serta memantau data dari WisataViewModel.
 */
class MainActivity : AppCompatActivity() {

    // Properti binding untuk mengakses view pada layout activity_main.xml secara aman
    private lateinit var binding: ActivityMainBinding

    // Inisialisasi ViewModel menggunakan extension fragment-ktx ('by viewModels()')
    private val viewModel: WisataViewModel by viewModels()

    // Adapter untuk mengelola data dan tampilan daftar item pada RecyclerView
    private lateinit var adapter: WisataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout XML activity_main ke dalam ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menggunakan extension function dari Helper.kt untuk mengatur ActionBar
        setupActionBar(title = "Daftar Wisata", showBackButton = false)

        // Konfigurasi komponen UI dan pendaftaran observer ViewModel
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        setupObservers()

        // Panggil data halaman pertama secara otomatis saat activity pertama kali dibuka
        viewModel.fetchWisata(isRefresh = true)
    }

    /**
     * Mengatur konfigurasi RecyclerView, inisialisasi Adapter dengan event klik item,
     * serta memasang Infinite Scrolling Listener dari Helper.kt
     */
    private fun setupRecyclerView() {
        adapter = WisataAdapter { wisata ->
            // Menggunakan extension function dari Helper.kt untuk pindah Activity
            startDetailWisataActivity(wisata)
        }

        binding.rvWisata.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter

            // Memasang Infinite Scroll untuk pagination otomatis saat scroll mencapai bagian bawah
            addInfiniteScrollListener(
                isLoading = { viewModel.isLoadingMore },
                onLoadMore = { viewModel.fetchWisata() }
            )
        }
    }

    /**
     * Mengatur fungsi SwipeRefreshLayout saat layar ditarik ke bawah oleh pengguna.
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Bersihkan input teks pada SearchView
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()

            // Gunakan extension function Helper.kt untuk memastikan keyboard tertutup
            hideKeyboard()

            // Panggil ulang data dari halaman awal (isRefresh = true)
            viewModel.fetchWisata(isRefresh = true)
        }
    }

    /**
     * Mengatur fungsi pencarian tempat wisata pada SearchView
     * menggunakan extension dari Helper.kt
     */
    private fun setupSearchView() {
        binding.searchView.onQueryTextChanged(
            onSubmit = { query ->
                if (query.isNotEmpty()) {
                    viewModel.searchWisata(query)
                }
            },
            onChange = { newText ->
                // Mengembalikan data awal jika teks pencarian dihapus hingga kosong
                if (newText.isEmpty()) {
                    viewModel.searchWisata("")
                }
            }
        )
    }

    /**
     * Memantau (observe) aliran data dan status UiState dari WisataViewModel
     * menggunakan extension function observeUiState dari Helper.kt.
     */
    private fun setupObservers() {
        // Memantau perubahan UiState utama (Loading, Success, dan Error)
        observeUiState(
            liveData = viewModel.wisataState,
            progressBar = binding.progressBar,
            onLoading = {
                // Sembunyikan ProgressBar tengah jika loading berasal dari SwipeRefresh atau Load More
                if (viewModel.isLoadingMore || binding.swipeRefresh.isRefreshing) {
                    binding.progressBar.hide() // Menggunakan hide() dari Helper.kt
                }
            },
            onSuccess = { wisataList ->
                // Hentikan animasi putar SwipeRefresh
                binding.swipeRefresh.isRefreshing = false

                // Perbarui data daftar wisata pada adapter
                val isLoadMoreActive = viewModel.isLoadMore.value ?: false
                adapter.setData(wisataList, isLoadMoreActive)
            },
            onError = { message ->
                // Hentikan animasi putar SwipeRefresh dan tampilkan Toast error
                binding.swipeRefresh.isRefreshing = false
                showToast(message, Toast.LENGTH_LONG) // Menggunakan showToast() dari Helper.kt
            }
        )

        // Memantau status indikator loading footer di bagian bawah RecyclerView (pagination)
        viewModel.isLoadMore.observe(this) { isLoadMore ->
            val currentState = viewModel.wisataState.value
            if (currentState is UiState.Success) {
                adapter.setData(currentState.data, isLoadMore)
            }
        }
    }
}