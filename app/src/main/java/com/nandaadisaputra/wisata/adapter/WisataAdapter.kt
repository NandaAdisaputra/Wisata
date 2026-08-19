package com.nandaadisaputra.wisata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nandaadisaputra.wisata.databinding.ItemWisataBinding
import com.nandaadisaputra.wisata.model.Wisata

class WisataAdapter : RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    // Kumpulan data yang akan ditampilkan di dalam RecyclerView
    private val listWisata = ArrayList<Wisata>()

    /**
     * Fungsi untuk memasukkan data baru ke dalam adapter.
     * Biasanya dipanggil oleh Activity/Fragment setelah menerima data dari ViewModel.
     */
    fun setData(data: List<Wisata>) {
        listWisata.clear() // Menghapus data lama agar tidak terjadi duplikasi saat memuat ulang
        listWisata.addAll(data) // Memasukkan data baru dari server
        notifyDataSetChanged() // Memberi tahu RecyclerView bahwa data berubah, sehingga UI diperbarui
    }

    /**
     * ViewHolder bertugas sebagai tempat (wadah) untuk menyimpan referensi
     * elemen-elemen UI (seperti TextView dan ImageView) pada setiap baris item.
     */
    inner class WisataViewHolder(private val binding: ItemWisataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk mengikat (memetakan) data dari model Wisata ke elemen UI layout
        fun bind(wisata: Wisata) {
            // Mengatur teks, menggunakan Elvis Operator (?:) untuk memberikan nilai default
            // jika seandainya data dari server bernilai null
            binding.tvNamaWisata.text = wisata.namaWisata ?: "Tanpa Nama"
            binding.tvDeskripsi.text = wisata.deskripsi ?: "Tidak ada deskripsi"

            // Menggunakan library Glide untuk memuat gambar dari URL internet ke ImageView.
            // PERUBAHAN: Diubah dari 'wisata.gambar' menjadi 'wisata.fotoUrl' sesuai struktur Model terbaru.
            Glide.with(itemView.context)
                .load(wisata.fotoUrl)
                .centerCrop() // Menyesuaikan ukuran gambar agar memenuhi batas ImageView secara proporsional
                .into(binding.ivGambar)
        }
    }

    /**
     * Dipanggil saat RecyclerView membutuhkan ViewHolder baru.
     * Menghubungkan layout per-item (item_wisata.xml) menggunakan ViewBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val binding = ItemWisataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WisataViewHolder(binding)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi (indeks) tertentu.
     */
    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        holder.bind(listWisata[position])
    }

    /**
     * Mengembalikan total jumlah item wisata yang ada di dalam list.
     */
    override fun getItemCount(): Int = listWisata.size
}