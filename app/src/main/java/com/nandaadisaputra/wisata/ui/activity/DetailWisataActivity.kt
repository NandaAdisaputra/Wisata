package com.nandaadisaputra.wisata.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.R
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisata
import com.nandaadisaputra.wisata.databinding.ActivityDetailWisataBinding
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.utils.*
import com.nandaadisaputra.wisata.viewmodel.DetailWisataViewModel
import com.nandaadisaputra.wisata.viewmodel.FavoriteViewModel
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel

// Kelas Activity untuk menampilkan detail tempat wisata, mengelola status favorit, serta opsi Edit dan Hapus data
class DetailWisataActivity : AppCompatActivity() {

    // Variable View Binding untuk mengakses elemen-elemen layout XML secara langsung
    private lateinit var binding: ActivityDetailWisataBinding

    // ViewModel khusus untuk mengambil data detail tempat wisata dari API/Repository
    private val detailViewModel: DetailWisataViewModel by viewModels()

    // ViewModel yang mengelola operasi CRUD ke API (digunakan di sini khusus untuk aksi hapus/deleteWisata)
    private val wisataViewModel: WisataViewModel by viewModels()

    // ViewModel untuk mengelola data favorit pada database lokal (Room)
    private val favoriteViewModel: FavoriteViewModel by viewModels()

    // Flag penanda apakah tempat wisata ini ditandai sebagai favorit atau tidak
    private var isFavorite = false

    // Menyimpan objek data tempat wisata yang sedang ditampilkan saat ini
    private var currentWisata: Wisata? = null

    // Menyimpan ID dari tempat wisata yang sedang dibuka (-1 jika tidak ditemukan)
    private var targetId = -1

    // Register Activity Result Launcher untuk menangani kembali dari halaman Edit
    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Jika dari halaman edit mengembalikan RESULT_OK, muat ulang (refresh) data detail terbaru
        if (result.resultCode == Activity.RESULT_OK) {
            if (targetId != -1) {
                detailViewModel.fetchDetailWisata(targetId)
                setResult(Activity.RESULT_OK) // Menginformasikan ke halaman sebelumnya bahwa ada pembaruan data
            }
        }
    }

    // Siklus hidup utama Activity saat halaman pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menginisialisasi View Binding dengan meng-inflate layout XML
        binding = ActivityDetailWisataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Konfigurasi awal tampilan dan listener
        setupActionBar("Detail Tempat Wisata") // Mengatur judul ActionBar halaman
        observeViewModel()                     // Mengamati status fetching detail data
        observeDeleteState()                   // Mengamati status operasi hapus data
        setupIntentData()                      // Mengambil data/ID dari intent yang dikirim
        setupFavoriteAction()                  // Menyiapkan listener tombol favorit (FAB)
        setupBottomActionButtons()             // Menyiapkan listener tombol Edit dan Hapus
    }

    // Mengamati LiveData UiState dari DetailWisataViewModel untuk menampilkan data detail tempat wisata
    private fun observeViewModel() {
        observeUiState(
            liveData = detailViewModel.detailState,
            progressBar = binding.progressBar,
            onSuccess = { wisata ->
                currentWisata = wisata
                setupDataToView(wisata) // Tampilkan data wisata ke elemen UI
            }
        )
    }

    // Memantau status respon dari operasi Hapus (delete) pada WisataViewModel
    private fun observeDeleteState() {
        wisataViewModel.crudState.observe(this) { state ->
            when (state) {
                // Tampilkan indikator loading saat proses penghapusan sedang berlangsung
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                // Jika proses penghapusan di server berhasil
                is UiState.Success -> {
                    binding.progressBar.hide()
                    showToast(state.data.message ?: "Data berhasil dihapus")

                    // Hapus juga data dari database favorit lokal jika terdaftar
                    favoriteViewModel.removeFromFavorite(targetId)
                    wisataViewModel.resetCrudState()

                    // Mengembalikan RESULT_OK ke halaman sebelumnya agar list utama di-refresh, lalu tutup activity
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                // Tampilkan pesan error jika proses hapus gagal
                is UiState.Error -> {
                    binding.progressBar.hide()
                    showToast(state.message)
                    wisataViewModel.resetCrudState()
                }
                null -> {}
            }
        }
    }

    // Memproses data yang dikirim melalui Intent saat activity ini dibuka
    private fun setupIntentData() {
        try {
            // Mengambil ID wisata atau objek parcelable dari intent
            val wisataId = intent.getIntExtra(EXTRA_ID, -1)
            val wisataParcelable = if (intent.hasExtra(EXTRA_WISATA)) {
                intent.getParcelableExtraCompat<Wisata>(EXTRA_WISATA)
            } else {
                null
            }

            // Menentukan ID target utama yang akan dipakai
            targetId = if (wisataId != -1) {
                wisataId
            } else {
                wisataParcelable?.id ?: -1
            }

            // Jika ID valid, ambil detail data dari server dan cek status favorit dari Room Database
            if (targetId != -1) {
                detailViewModel.fetchDetailWisata(targetId)
                favoriteViewModel.checkFavorite(targetId).observe(this) { favorite ->
                    isFavorite = favorite ?: false
                    updateFavoriteIcon(isFavorite) // Perbarui ikon tombol favorit
                }
            } else {
                // Jika ID tidak ditemukan, tampilkan toast dan tutup halaman
                showToast("ID tempat wisata tidak ditemukan")
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Terjadi kesalahan saat memuat data")
            finish()
        }
    }

    // Menyiapkan listener aksi klik untuk tombol Edit dan Tombol Hapus di bagian bawah layout
    private fun setupBottomActionButtons() {
        // Klik tombol Edit
        binding.btnEditWisata.setOnClickListener {
            currentWisata?.let { navigateToEditWisata(it) }
                ?: showToast("Data belum siap untuk diedit")
        }

        // Klik tombol Hapus
        binding.btnHapusWisata.setOnClickListener {
            currentWisata?.let { showDeleteConfirmationDialog(it) }
                ?: showToast("Data belum siap untuk dihapus")
        }
    }

    // Menyiapkan listener untuk Floating Action Button (FAB) Favorit
    private fun setupFavoriteAction() {
        binding.fabFavorite.setOnClickListener {
            currentWisata?.let { wisata ->
                // Membentuk objek entity FavoriteWisata untuk disimpan ke Room Database
                val favoriteWisata = FavoriteWisata(
                    id = wisata.id,
                    namaWisata = wisata.namaWisata.orEmpty(),
                    kategori = wisata.kategori.orEmpty(),
                    lokasi = wisata.lokasi.orEmpty(),
                    hargaTiket = wisata.hargaTiket ?: 0,
                    deskripsi = wisata.deskripsi,
                    fotoUrl = wisata.fotoUrl ?: wisata.foto
                )

                // Toggle/balikkan status favorit
                isFavorite = !isFavorite
                updateFavoriteIcon(isFavorite)

                // Eksekusi penambahan atau penghapusan dari database lokal berdasarkan status favorit
                if (!isFavorite) {
                    favoriteViewModel.removeFromFavorite(wisata.id)
                    showToast("Dihapus dari Favorit")
                } else {
                    favoriteViewModel.addToFavorite(favoriteWisata)
                    showToast("Ditambahkan ke Favorit")
                }
            } ?: showToast("Data wisata belum siap")
        }
    }

    // Fungsi untuk memperbarui ikon FAB sesuai dengan nilai status favorit (merah = favorit, hitam = tidak)
    private fun updateFavoriteIcon(favorite: Boolean) {
        isFavorite = favorite
        binding.fabFavorite.setImageResource(
            if (favorite) R.drawable.ic_favorite_red else R.drawable.ic_favorite_black
        )
    }

    // Memasang data objek Wisata ke dalam masing-masing view (TextView & ImageView)
    private fun setupDataToView(wisata: Wisata) {
        binding.tvNamaDetail.text = wisata.namaWisata.orDefault("Tanpa Nama")
        binding.tvKategoriDetail.text = wisata.kategori.orDefault("Umum")
        binding.tvLokasiDetail.text = wisata.lokasi.orDefault("Lokasi tidak tersedia")
        binding.tvDescriptionDetail.text = wisata.deskripsi.orDefault("Tidak ada deskripsi detail.")
        binding.tvPriceDetail.text = wisata.hargaTiket.toRupiahFormat()

        // Muat gambar ke ImageView header menggunakan URL foto
        val imageUrl = wisata.fotoUrl ?: wisata.foto
        binding.imgDetailHeader.loadImage(imageUrl)
    }

    // Menavigasi pengguna ke EditWisataActivity dengan membawa data Parcelable Wisata
    private fun navigateToEditWisata(wisata: Wisata) {
        val intent = Intent(this, EditWisataActivity::class.java).apply {
            putExtra(EXTRA_WISATA, wisata)
        }
        editLauncher.launch(intent)
    }

    // Menampilkan dialog konfirmasi sebelum melakukan aksi penghapusan data
    private fun showDeleteConfirmationDialog(wisata: Wisata) {
        showConfirmationDialog(
            title = "Hapus Wisata",
            message = "Apakah Anda yakin ingin menghapus '${wisata.namaWisata}'? Tindakan ini tidak dapat dibatalkan.",
            positiveButtonText = "Hapus",
            negativeButtonText = "Batal"
        ) {
            // Memanggil fungsi deleteWisata dari WisataViewModel jika pengguna mengonfirmasi hapus
            wisata.id?.let { id ->
                wisataViewModel.deleteWisata(id)
            }
        }
    }

    // Handling item menu pada Toolbar/ActionBar (misal: tombol navigasi Kembali)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handling navigasi tombol 'Up' (panah kembali di toolbar)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Companion object berisi konstanta Key Extra untuk intent
    companion object {
        const val EXTRA_WISATA = "extra_wisata"
        const val EXTRA_ID = "extra_id"
    }
}