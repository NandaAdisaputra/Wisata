package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.R
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisata
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
import com.nandaadisaputra.wisata.viewmodel.FavoriteViewModel

/**
 * DetailWisataActivity bertugas mengambil dan menampilkan informasi detail
 * tempat wisata langsung dari API menggunakan ViewModel dan UiState,
 * serta mengelola penyimpanan status favorit ke Room Database.
 */
class DetailWisataActivity : AppCompatActivity() {

    // Properti binding untuk mengontrol view di activity_detail_wisata.xml
    private lateinit var binding: ActivityDetailWisataBinding

    // Inisialisasi ViewModel khusus detail menggunakan delegate viewModels()
    private val viewModel: DetailWisataViewModel by viewModels()

    // Inisialisasi FavoriteViewModel untuk manajemen database lokal favorit
    private val favoriteViewModel: FavoriteViewModel by viewModels()

    // Variabel pendukung status favorit
    private var isFavorite = false
    private var currentWisata: Wisata? = null
    private var targetId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout XML menggunakan ViewBinding
        binding = ActivityDetailWisataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Konfigurasi Title dan Tombol Back pada ActionBar via extension function setupActionBar()
        setupActionBar("Detail Tempat Wisata")

        // Daftarkan observer UiState menggunakan extension function observeUiState()
        observeViewModel()

        // Proses ekstraksi parameter Intent dan panggil API jika ID valid (dengan proteksi try-catch)
        setupIntentData()

        // Inisialisasi aksi klik tombol Floating Action Button Favorite
        setupFavoriteAction()
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
                currentWisata = wisata
                setupDataToView(wisata)
            }
        )
    }

    /**
     * Mengambil ID dari Intent (baik ID langsung maupun dari objek Parcelable Wisata)
     * dan memicu pemanggilan data ke ViewModel serta pengecekan status favorit secara aman.
     */
    private fun setupIntentData() {
        try {
            // Ambil ID langsung dari intent extra (default -1 jika tidak ada)
            val wisataId = intent.getIntExtra(EXTRA_ID, -1)

            // Ambil objek Parcelable dengan pengecekan aman untuk mencegah crash jika extra tidak ada
            val wisataParcelable = if (intent.hasExtra(EXTRA_WISATA)) {
                intent.getParcelableExtraCompat<Wisata>(EXTRA_WISATA)
            } else {
                null
            }

            // Tentukan ID target (utamakan EXTRA_ID, fallback ke id dari Parcelable)
            targetId = if (wisataId != -1) {
                wisataId
            } else {
                wisataParcelable?.id ?: -1
            }

            // Jalankan pemanggilan API jika ID valid
            if (targetId != -1) {
                viewModel.fetchDetailWisata(targetId)

                // Cek status apakah wisata ini sudah ada di database favorit lokal
                favoriteViewModel.checkFavorite(targetId).observe(this) { favorite ->
                    isFavorite = favorite ?: false
                    updateFavoriteIcon(isFavorite)
                }
            } else {
                showToast("ID tempat wisata tidak ditemukan")
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Terjadi kesalahan saat memuat data")
            finish()
        }
    }

    /**
     * Mengatur aksi klik pada tombol FloatingActionButton (FAB) Favorite.
     */
    private fun setupFavoriteAction() {
        binding.fabFavorite.setOnClickListener {
            currentWisata?.let { wisata ->
                // Konversi model API (Wisata) ke model Room Entity (FavoriteWisata)
                val favoriteWisata = FavoriteWisata(
                    id = wisata.id,
                    namaWisata = wisata.namaWisata.orEmpty(),
                    kategori = wisata.kategori.orEmpty(),
                    lokasi = wisata.lokasi.orEmpty(),
                    hargaTiket = wisata.hargaTiket ?: 0,
                    deskripsi = wisata.deskripsi,
                    fotoUrl = wisata.fotoUrl ?: wisata.foto
                )

                // 1. UBAH UI SECARA INSTAN (OPTIMISTIC UI)
                // Balik nilai isFavorite secara langsung agar ikon langsung berganti detik itu juga
                isFavorite = !isFavorite
                updateFavoriteIcon(isFavorite)

                // 2. EKSEKUSI DATABASE DI BACKGROUND
                if (!isFavorite) {
                    // Karena tadi isFavorite dibalik jadi false, berarti awalnya true (artinya mau dihapus)
                    favoriteViewModel.removeFromFavorite(wisata.id)
                    showToast("Dihapus dari Favorit")
                } else {
                    // Sebaliknya, berarti mau ditambah
                    favoriteViewModel.addToFavorite(favoriteWisata)
                    showToast("Ditambahkan ke Favorit")
                }
            } ?: run {
                showToast("Data wisata belum siap")
            }
        }
    }
    /**
     * Memperbarui ikon FAB berdasarkan status isFavorite (menggunakan ic_favorite_white / ic_favorite_black).
     */
    private fun updateFavoriteIcon(favorite: Boolean) {
        isFavorite = favorite
        if (favorite) {
            // Ketika status favorit aktif
            binding.fabFavorite.setImageResource(R.drawable.ic_favorite_red)
        } else {
            // Ketika status favorit tidak aktif
            binding.fabFavorite.setImageResource(R.drawable.ic_favorite_black)
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