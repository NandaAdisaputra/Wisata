package com.nandaadisaputra.wisata

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nandaadisaputra.wisata.adapter.WisataAdapter
import com.nandaadisaputra.wisata.databinding.ActivityMainBinding
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel

class MainActivity : AppCompatActivity() {

    // Menggunakan ViewBinding untuk menghubungkan file layout XML (activity_main.xml)
    // ke Activity ini dengan lebih aman tanpa perlu findViewByID.
    private lateinit var binding: ActivityMainBinding

    // Deklarasi adapter yang akan mengatur tampilan tiap baris/item di dalam RecyclerView
    private lateinit var adapter: WisataAdapter

    // Inisialisasi ViewModel menggunakan delegasi 'by viewModels()'.
    // Keuntungannya: Data wisata tidak akan hilang dan API tidak perlu dipanggil ulang
    // jika terjadi perubahan konfigurasi (misalnya saat orientasi layar di-rotate/diputar).
    private val viewModel: WisataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Proses inflate layout dan menetapkannya sebagai tampilan utama aplikasi
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Memanggil fungsi-fungsi untuk menyiapkan komponen UI
        setupRecyclerView()
        observeViewModel()

        // Memicu ViewModel untuk mulai mengambil data wisata dari server melalui API.
        // Langkah ini dilakukan paling akhir agar UI sudah siap saat data masuk.
        viewModel.fetchWisata()
    }

    /**
     * Fungsi khusus untuk mengatur konfigurasi RecyclerView.
     */
    private fun setupRecyclerView() {
        // Membuat instance/objek baru dari adapter
        adapter = WisataAdapter()

        // Mengatur LayoutManager agar daftar ditampilkan secara vertikal dari atas ke bawah
        binding.rvWisata.layoutManager = LinearLayoutManager(this)

        // Memasang adapter ke RecyclerView yang ada di layout XML
        binding.rvWisata.adapter = adapter
    }

    /**
     * Fungsi untuk "mengamati" (observe) perubahan pada LiveData yang ada di ViewModel.
     * Tampilan UI (Activity) akan merespons secara otomatis ketika ada perubahan data.
     */
    private fun observeViewModel() {

        // 1. Memantau data daftar wisata
        viewModel.wisataList.observe(this) { data ->
            // Pastikan data tidak kosong sebelum dimasukkan ke adapter
            if (data != null && data.isNotEmpty()) {
                // Fungsi setData() ini harus ada di dalam class WisataAdapter kamu
                adapter.setData(data)
            }
        }

        // 2. Memantau status loading
        viewModel.isLoading.observe(this) { isLoading ->
            // Jika isLoading = true (sedang memuat), ProgressBar muncul, RecyclerView disembunyikan.
            // Jika isLoading = false (selesai memuat), ProgressBar hilang, RecyclerView muncul.
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvWisata.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // 3. Memantau pesan error
        viewModel.errorMessage.observe(this) { message ->
            // Jika terdapat pesan error dari API atau koneksi, tampilkan pop-up Toast singkat
            if (message != null && message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}