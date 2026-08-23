package com.nandaadisaputra.wisata

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nandaadisaputra.wisata.adapter.WisataAdapter
import com.nandaadisaputra.wisata.databinding.ActivityMainBinding
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WisataAdapter
    private val viewModel: WisataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        // Panggil data halaman pertama saat aplikasi pertama kali dijalankan
        viewModel.fetchWisata(isRefresh = true)
    }

    /**
     * Konfigurasi RecyclerView dan pasang Scroll Listener untuk mendeteksi batas bawah layar.
     */
    private fun setupRecyclerView() {
        adapter = WisataAdapter()
        val layoutManager = LinearLayoutManager(this)

        binding.rvWisata.layoutManager = layoutManager
        binding.rvWisata.adapter = adapter

        // Listener untuk mendeteksi posisi scroll (Infinite Scrolling)
        binding.rvWisata.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Jika user scroll sampai mendekati item terakhir dan tidak sedang memuat data lain
                if (!viewModel.isLoadingMore && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    // Minta halaman berikutnya ke ViewModel
                    viewModel.fetchWisata()
                }
            }
        })
    }

    /**
     * Mengamati perubahan data dari LiveData ViewModel.
     */
    private fun observeViewModel() {

        // Memantau perubahan daftar wisata
        viewModel.wisataList.observe(this) { data ->
            if (data != null) {
                // Perbarui adapter dengan data dan status loading more saat ini
                adapter.setData(data, viewModel.isLoadingMore)
            }
        }

        // Memantau loading utama (tengah layar)
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Memantau status load more untuk menampilkan/menyembunyikan footer loading di bawah list
        viewModel.isLoadMore.observe(this) { isLoadMore ->
            val currentData = viewModel.wisataList.value
            if (currentData != null) {
                adapter.setData(currentData, isLoadMore)
            }
        }

        // Memantau pesan error
        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}