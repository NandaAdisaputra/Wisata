package com.nandaadisaputra.wisata.ui.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityEditWisataBinding
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.utils.*
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

// Kelas Activity khusus untuk menangani proses pengubahan (edit) data tempat wisata
class EditWisataActivity : AppCompatActivity() {

    // Deklarasi variabel binding untuk menghubungkan file layout XML ActivityEditWisataBinding secara aman
    private lateinit var binding: ActivityEditWisataBinding

    // Inisialisasi WisataViewModel menggunakan delegasi by viewModels() untuk mengelola fungsi pembaruan data & state CRUD
    private val wisataViewModel: WisataViewModel by viewModels()

    // Variable lokal untuk menyimpan objek Wisata yang ditransfer dari halaman sebelumnya
    private var currentWisata: Wisata? = null

    // Siklus hidup onCreate yang dipanggil pertama kali ketika Activity dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Memasang View Binding untuk mengakses elemen UI
        binding = ActivityEditWisataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengatur judul ActionBar halaman
        setupActionBar("Edit Tempat Wisata")

        // Memanggil fungsi-fungsi inisialisasi awal
        getIntentExtraData()   // Mengambil data intent yang dikirim
        observeUpdateState()   // Mengamati status respons proses pembaruan data
        setupActionListeners() // Menyiapkan event klik tombol
    }

    // Fungsi untuk mengambil data Wisata yang dikirim melalui Intent Extra
    private fun getIntentExtraData() {
        // Mengambil objek Parcelable data Wisata menggunakan extension function getParcelableExtraCompat
        currentWisata = intent.getParcelableExtraCompat<Wisata>(DetailWisataActivity.EXTRA_WISATA)

        // Jika data tersedia, isi formulir input; jika tidak, tampilkan pesan error dan tutup activity
        currentWisata?.let { populateForm(it) } ?: run {
            showToast("Data wisata tidak ditemukan")
            finish()
        }
    }

    // Fungsi untuk mengisi input form (EditText) dengan data wisata yang sudah ada sebelumnya
    private fun populateForm(wisata: Wisata) {
        binding.apply {
            etNamaWisata.setText(wisata.namaWisata)
            etKategori.setText(wisata.kategori)
            etLokasi.setText(wisata.lokasi)
            etHargaTiket.setText(wisata.hargaTiket?.toString() ?: "0")
            etFotoUrl.setText(wisata.fotoUrl ?: wisata.foto)
            etDeskripsi.setText(wisata.deskripsi)
        }
    }

    // Menyiapkan listener aksi klik pada elemen UI
    private fun setupActionListeners() {
        // Ketika tombol 'Simpan Perubahan' diklik, lakukan validasi input terlebih dahulu sebelum mengirim pembaruan
        binding.btnSimpanPerubahan.setOnClickListener {
            if (validateInput()) {
                submitUpdate()
            }
        }
    }

    // Fungsi untuk memvalidasi input form agar tidak ada bidang wajib yang kosong
    private fun validateInput(): Boolean {
        binding.apply {
            // Validasi nama wisata
            if (etNamaWisata.text.toString().trim().isEmpty()) {
                tilNamaWisata.error = "Tidak boleh kosong"
                return false
            } else tilNamaWisata.error = null

            // Validasi kategori
            if (etKategori.text.toString().trim().isEmpty()) {
                tilKategori.error = "Tidak boleh kosong"
                return false
            } else tilKategori.error = null

            // Validasi lokasi
            if (etLokasi.text.toString().trim().isEmpty()) {
                tilLokasi.error = "Tidak boleh kosong"
                return false
            } else tilLokasi.error = null

            // Validasi harga tiket
            if (etHargaTiket.text.toString().trim().isEmpty()) {
                tilHargaTiket.error = "Tidak boleh kosong"
                return false
            } else tilHargaTiket.error = null
        }
        return true
    }

    // Fungsi untuk memproses data dari form input dan mengirimkannya ke ViewModel
    private fun submitUpdate() {
        currentWisata?.let { wisata ->
            // Ambil teks dari masing-masing form input
            val idStr = wisata.id.toString()
            val namaStr = binding.etNamaWisata.text.toString().trim()
            val kategoriStr = binding.etKategori.text.toString().trim()
            val lokasiStr = binding.etLokasi.text.toString().trim()
            val hargaStr = binding.etHargaTiket.text.toString().trim()
            val deskripsiStr = binding.etDeskripsi.text.toString().trim()

            // Konversi String ke format RequestBody untuk mengakomodasi format API @Multipart Retrofit
            val mediaType = "text/plain".toMediaTypeOrNull()
            val idBody = idStr.toRequestBody(mediaType)
            val namaBody = namaStr.toRequestBody(mediaType)
            val kategoriBody = kategoriStr.toRequestBody(mediaType)
            val lokasiBody = lokasiStr.toRequestBody(mediaType)
            val hargaBody = hargaStr.toRequestBody(mediaType)
            val deskripsiBody = deskripsiStr.toRequestBody(mediaType)

            // Mengirim data ke server melalui fungsi updateWisata pada WisataViewModel
            // Parameter foto bernilai null karena pada halaman ini tidak ada pengubahan file fisik gambar
            wisataViewModel.updateWisata(
                id = idBody,
                namaWisata = namaBody,
                kategori = kategoriBody,
                lokasi = lokasiBody,
                hargaTiket = hargaBody,
                deskripsi = deskripsiBody,
                foto = null
            )
        }
    }

    // Fungsi untuk mengamati perubahan state respon dari WisataViewModel
    private fun observeUpdateState() {
        wisataViewModel.crudState.observe(this) { state ->
            when (state) {
                // Saat status Loading: Tampilkan indikator progressBar
                is UiState.Loading -> binding.progressBar.show()

                // Saat status Success: Sembunyikan progressBar, beri pesan toast, kembalikan RESULT_OK, lalu tutup activity
                is UiState.Success -> {
                    binding.progressBar.hide()
                    showToast(state.data.message ?: "Data berhasil diperbarui")
                    wisataViewModel.resetCrudState()
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                // Saat status Error: Sembunyikan progressBar dan tampilkan pesan kesalahan
                is UiState.Error -> {
                    binding.progressBar.hide()
                    showToast(state.message)
                    wisataViewModel.resetCrudState()
                }
                null -> {}
            }
        }
    }

    // Handling navigasi tombol Kembali (Up Arrow) di ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}