package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityDetailWisataBinding
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.utils.getParcelableExtraCompat
import com.nandaadisaputra.wisata.utils.loadImage
import com.nandaadisaputra.wisata.utils.observeUiState
import com.nandaadisaputra.wisata.utils.orDefault
import com.nandaadisaputra.wisata.utils.setupActionBar
import com.nandaadisaputra.wisata.utils.showToast
import com.nandaadisaputra.wisata.utils.toRupiahFormat
import com.nandaadisaputra.wisata.viewmodel.DetailWisataViewModel

/**
 * DetailWisataActivity bertugas mengambil dan menampilkan informasi detail
 * tempat wisata langsung dari API menggunakan ViewModel dan UiState.
 */
class DetailWisataActivity : AppCompatActivity() {

    // Properti binding untuk mengontrol view di activity_detail_wisata.xml
    private lateinit var binding: ActivityDetailWisataBinding

    // Inisialisasi ViewModel khusus detail menggunakan delegate viewModels()
    private val viewModel: DetailWisataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout XML menggunakan ViewBinding
        binding = ActivityDetailWisataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Konfigurasi Title dan Tombol Back pada ActionBar via extension function setupActionBar()
        setupActionBar("Detail Tempat Wisata")

        // Daftarkan observer UiState menggunakan extension function observeUiState()
        observeViewModel()

        // Proses ekstraksi parameter Intent dan panggil API jika ID valid
        setupIntentData()
    }

    /**
     * Mengamati aliran status UiState (Loading, Success, Error) dari DetailWisataViewModel
     * secara otomatis dengan menangani ProgressBar dan Toast Error via Helper.kt.
     */
    private fun observeViewModel() {
        observeUiState(
            liveData = viewModel.detailState,
            progressBar = binding.progressBar,
            onSuccess = { wisata ->
                setupDataToView(wisata)
            }
        )
    }

    /**
     * Mengambil ID dari Intent (baik ID langsung maupun dari objek Parcelable Wisata)
     * dan memicu pemanggilan data ke ViewModel.
     */
    private fun setupIntentData() {
        // Ambil ID langsung dari intent extra
        val wisataId = intent.getIntExtra(EXTRA_ID, -1)

        // Ambil objek Parcelable menggunakan extension function dari Helper.kt (Tiramisu Compat)
        val wisataParcelable = intent.getParcelableExtraCompat<Wisata>(EXTRA_WISATA)

        // Tentukan ID target (utamakan EXTRA_ID, fallback ke id dari Parcelable)
        val targetId = if (wisataId != -1) {
            wisataId
        } else {
            wisataParcelable?.id ?: -1
        }

        // Jalankan pemanggilan API jika ID valid
        if (targetId != -1) {
            viewModel.fetchDetailWisata(targetId)
        } else {
            // Menampilkan Toast pesan error menggunakan extension function showToast()
            showToast("ID tempat wisata tidak ditemukan")
            finish()
        }
    }

    /**
     * Memasukkan data objek Wisata dari API ke komponen UI di layar.
     *
     * @param wisata Objek Wisata yang dikembalikan oleh API.
     */
    private fun setupDataToView(wisata: Wisata) {
        // Mengisi teks UI dengan proteksi fallback menggunakan extension function orDefault()
        binding.tvNamaDetail.text = wisata.namaWisata.orDefault("Tanpa Nama")
        binding.tvKategoriDetail.text = wisata.kategori.orDefault("Umum")
        binding.tvLokasiDetail.text = wisata.lokasi.orDefault("Lokasi tidak tersedia")
        binding.tvDescriptionDetail.text = wisata.deskripsi.orDefault("Tidak ada deskripsi detail.")

        // Format harga tiket ke mata uang IDR via extension function toRupiahFormat()
        binding.tvPriceDetail.text = wisata.hargaTiket.toRupiahFormat()

        // Ambil URL gambar utama (fotoUrl atau foto)
        val imageUrl = wisata.fotoUrl ?: wisata.foto

        // Pemuatan gambar header menggunakan extension function loadImage() dari Helper.kt
        binding.imgDetailHeader.loadImage(imageUrl)
    }

    /**
     * Menangani aksi penekanan tombol panah kembali (Back Arrow) pada Action Bar.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    companion object {
        const val EXTRA_WISATA = "extra_wisata"
        const val EXTRA_ID = "extra_id"
    }
}
