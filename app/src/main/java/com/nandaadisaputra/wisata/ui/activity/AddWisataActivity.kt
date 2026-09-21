package com.nandaadisaputra.wisata.ui.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityAddWisataBinding
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.utils.*
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel

// Kelas Activity yang berfungsi untuk menambah data wisata baru maupun memperbarui (edit) data wisata yang sudah ada
class AddWisataActivity : AppCompatActivity() {

    // Variable untuk mengimplementasikan View Binding agar dapat mengakses tampilan UI secara efisien
    private lateinit var binding: ActivityAddWisataBinding

    // Inisialisasi ViewModel secara lazy menggunakan extension `viewModels()`
    private val viewModel: WisataViewModel by viewModels()

    // Menyimpan Uri dari gambar yang dipilih dari galeri pengguna
    private var selectedImageUri: Uri? = null

    // Flag untuk menentukan apakah halaman ini dibuka untuk mode "Edit" atau "Tambah Baru"
    private var isEditMode = false

    // Menyimpan ID wisata yang sedang diedit (default -1 jika tambah data)
    private var wisataId: Int = -1

    // Menyimpan URL foto lama jika pengguna tidak memilih foto baru saat dalam mode edit
    private var existingFotoUrl: String? = null

    // Register Activity Result Launcher untuk membuka galeri dan mengambil file gambar
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Callback jika pengguna berhasil memilih gambar dari galeri
        if (uri != null) {
            selectedImageUri = uri
            // Tampilkan pratinjau gambar ke ImageView menggunakan extension function loadImage
            binding.imagePreview.loadImage(uri.toString())
        } else if (selectedImageUri == null && existingFotoUrl.isNullOrEmpty()) {
            // Tampilkan toast jika pengguna membatalkan pilihan gambar dan belum ada gambar terpasang
            showToast("Tidak ada gambar yang dipilih")
        }
    }

    // Fungsi siklus hidup Activity yang dipanggil saat pertama kali Activity dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Memasang View Binding dengan meregangkan layout XML
        binding = ActivityAddWisataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Panggil fungsi-fungsi inisialisasi awal
        checkEditMode()       // Memeriksa apakah masuk mode Edit atau Tambah
        setupListeners()      // Menyiapkan event listener untuk tombol/klik
        observeCrudState()    // Memantau status respon dari ViewModel (CRUD)
    }

    // Fungsi untuk memeriksa intent apakah Activity dibuka dalam mode edit atau mode tambah baru
    private fun checkEditMode() {
        // Cek data Intent berdasarkan boolean extra atau keberadaan data Parcelable wisata
        isEditMode = intent.getBooleanExtra("is_edit_mode", false) || intent.hasExtra(DetailWisataActivity.EXTRA_WISATA)

        if (isEditMode) {
            // Mengatur judul ActionBar dan teks tombol jika dalam mode Edit
            setupActionBar("Edit Tempat Wisata", showBackButton = true)
            binding.btnSimpan.text = "Perbarui Wisata"

            // Mengambil objek data Wisata dari intent secara aman (kompatibel versi Android lama/baru)
            val wisata = intent.getParcelableExtraCompat<Wisata>(DetailWisataActivity.EXTRA_WISATA)
            wisata?.let {
                wisataId = it.id ?: -1
                existingFotoUrl = it.fotoUrl ?: it.foto

                // Mengisi form input dengan data wisata yang akan diedit
                binding.edtNamaWisata.setText(it.namaWisata)
                binding.edtKategori.setText(it.kategori)
                binding.edtLokasi.setText(it.lokasi)
                binding.edtHargaTiket.setText(it.hargaTiket?.toString() ?: "0")
                binding.edtDeskripsi.setText(it.deskripsi)

                // Jika ada URL foto lama, muat gambar tersebut ke dalam ImageView
                val imageUrl = it.fotoUrl ?: it.foto
                if (!imageUrl.isNullOrEmpty()) {
                    binding.imagePreview.loadImage(imageUrl)
                }
            }
        } else {
            // Mengatur judul ActionBar dan teks tombol jika dalam mode Tambah Baru
            setupActionBar("Tambah Wisata Baru", showBackButton = true)
            binding.btnSimpan.text = "Simpan Wisata"
        }
    }

    // Fungsi untuk mendaftarkan aksi listener pada elemen UI
    private fun setupListeners() {
        // Buka galeri ketika area pilih gambar atau card preview gambar diklik
        binding.layoutSelectImage.setOnSingleClickListener { launcherGallery.launch("image/*") }
        binding.cardImagePreview.setOnSingleClickListener { launcherGallery.launch("image/*") }

        // Jalankan fungsi validasi dan simpan ketika tombol simpan diklik
        binding.btnSimpan.setOnSingleClickListener { validateAndSaveWisata() }
    }

    // Fungsi untuk melakukan validasi input data sebelum dikirim ke ViewModel/Server
    private fun validateAndSaveWisata() {
        // Mengambil nilai teks dari EditText yang telah dibersihkan spasi awalnya/akhirnya
        val namaWisata = binding.edtNamaWisata.trimmedText
        val kategori = binding.edtKategori.trimmedText
        val lokasi = binding.edtLokasi.trimmedText
        val hargaStr = binding.edtHargaTiket.trimmedText
        val deskripsi = binding.edtDeskripsi.trimmedText

        // Bersihkan seluruh pesan error input sebelum dilakukan validasi ulang
        clearInputErrors(
            binding.edtNamaWisata,
            binding.edtKategori,
            binding.edtLokasi,
            binding.edtHargaTiket,
            binding.edtDeskripsi
        )

        // Validasi input Nama Wisata
        if (namaWisata.isEmpty()) {
            binding.edtNamaWisata.error = "Nama wisata tidak boleh kosong"
            binding.edtNamaWisata.requestFocus()
            return
        }

        // Validasi input Kategori
        if (kategori.isEmpty()) {
            binding.edtKategori.error = "Kategori tidak boleh kosong"
            binding.edtKategori.requestFocus()
            return
        }

        // Validasi input Lokasi
        if (lokasi.isEmpty()) {
            binding.edtLokasi.error = "Lokasi tidak boleh kosong"
            binding.edtLokasi.requestFocus()
            return
        }

        // Validasi input Harga Tiket
        if (hargaStr.isEmpty()) {
            binding.edtHargaTiket.error = "Harga tiket tidak boleh kosong"
            binding.edtHargaTiket.requestFocus()
            return
        }

        // Validasi input Deskripsi
        if (deskripsi.isEmpty()) {
            binding.edtDeskripsi.error = "Deskripsi tidak boleh kosong"
            binding.edtDeskripsi.requestFocus()
            return
        }

        // Validasi gambar: Jika mode Tambah Baru, gambar wajib dipilih terlebih dahulu
        if (!isEditMode && selectedImageUri == null) {
            showToast("Silakan pilih foto wisata terlebih dahulu")
            return
        }

        // Sembunyikan keyboard virtual saat proses submit dimulai
        hideKeyboard()

        // Mengonversi string input menjadi RequestBody dan MultipartBody untuk kebutuhan request Multipart (upload file)
        val namaRb = namaWisata.toPlainRequestBody()
        val kategoriRb = kategori.toPlainRequestBody()
        val lokasiRb = lokasi.toPlainRequestBody()
        val hargaRb = hargaStr.toPlainRequestBody()
        val deskripsiRb = deskripsi.toPlainRequestBody()
        val imagePart = selectedImageUri?.toMultipartBody(this, "foto")

        // Eksekusi pemanggilan ke ViewModel berdasarkan mode (Edit atau Tambah)
        if (isEditMode) {
            val idRb = wisataId.toString().toPlainRequestBody()
            viewModel.updateWisata(idRb, namaRb, kategoriRb, lokasiRb, hargaRb, deskripsiRb, imagePart)
        } else {
            if (imagePart != null) {
                viewModel.addWisata(namaRb, kategoriRb, lokasiRb, hargaRb, deskripsiRb, imagePart)
            } else {
                showToast("File gambar tidak valid")
            }
        }
    }

    // Fungsi untuk mengamati (observe) status hasil operasi CRUD dari LiveData di ViewModel
    private fun observeCrudState() {
        viewModel.crudState.observe(this) { state ->
            when (state) {
                // Tampilkan ProgressBar dan nonaktifkan tombol simpan saat proses loading
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.btnSimpan.isEnabled = false
                }
                // Handler jika operasi CRUD berhasil diproses
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.btnSimpan.isEnabled = true
                    val message = state.data.message ?: "Operasi berhasil"

                    // Tampilkan AlertDialog pemberitahuan sukses
                    showAlertDialog(
                        title = "Sukses",
                        message = message,
                        positiveButtonText = "OK"
                    ) {
                        viewModel.resetCrudState()     // Reset state CRUD di ViewModel
                        setResult(Activity.RESULT_OK)  // Set result OK ke Activity pemanggil
                        finish()                       // Tutup Activity ini
                    }
                }
                // Handler jika terjadi error saat operasi CRUD
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.btnSimpan.isEnabled = true
                    showToast(state.message)           // Tampilkan pesan error
                    viewModel.resetCrudState()         // Reset state CRUD
                }
                null -> {}
            }
        }
    }

    // Handler ketika tombol kembali di ActionBar diklik
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Eksekusi aksi kembali (back)
        return true
    }
}