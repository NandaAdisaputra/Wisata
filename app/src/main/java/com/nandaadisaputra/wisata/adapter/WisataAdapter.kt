package com.nandaadisaputra.wisata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nandaadisaputra.wisata.databinding.ItemLoadingBinding
import com.nandaadisaputra.wisata.databinding.ItemWisataBinding
import com.nandaadisaputra.wisata.model.Wisata

// Menggunakan RecyclerView.ViewHolder umum karena adapter ini menangani 2 layout berbeda
class WisataAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Menggunakan List nullable (Wisata?) untuk menampung data wisata dan nilai null (sebagai penanda loading)
    private val listWisata = ArrayList<Wisata?>()

    companion object {
        private const val VIEW_TYPE_ITEM = 0    // Penanda untuk item data wisata biasa
        private const val VIEW_TYPE_LOADING = 1 // Penanda untuk item footer loading di bawah
    }

    /**
     * Fungsi untuk memperbarui data sekaligus menyisipkan atau menghapus footer loading.
     */
    fun setData(data: List<Wisata>, isLoadingMore: Boolean) {
        listWisata.clear()
        listWisata.addAll(data)

        // Jika sedang proses load more, tambahkan nilai 'null' di akhir list
        // sebagai sinyal untuk memunculkan layout loading di bawah.
        if (isLoadingMore) {
            listWisata.add(null)
        }
        notifyDataSetChanged()
    }

    /**
     * Menentukan jenis view (tampilan) berdasarkan isi data pada posisi tertentu.
     * Jika datanya null, kembalikan tipe loading. Jika berisi objek wisata, kembalikan tipe item.
     */
    override fun getItemViewType(position: Int): Int {
        return if (listWisata[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    /**
     * ViewHolder untuk menampilkan data wisata biasa.
     */
    inner class WisataViewHolder(private val binding: ItemWisataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(wisata: Wisata) {
            binding.tvNamaWisata.text = wisata.namaWisata ?: "Tanpa Nama"
            binding.tvDeskripsi.text = wisata.deskripsi ?: "Tidak ada deskripsi"

            // Memuat gambar menggunakan library Glide
            Glide.with(itemView.context)
                .load(wisata.fotoUrl)
                .centerCrop()
                .into(binding.ivGambar)
        }
    }

    /**
     * ViewHolder khusus untuk menampung layout footer loading di bagian bawah.
     */
    inner class LoadingViewHolder(binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Cek tipe view, lalu inflasi layout XML yang bersesuaian
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemWisataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            WisataViewHolder(binding)
        } else {
            val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Jika holder adalah WisataViewHolder, ikat datanya
        if (holder is WisataViewHolder) {
            val wisata = listWisata[position]
            if (wisata != null) {
                holder.bind(wisata)
            }
        }
        // Jika LoadingViewHolder, tidak perlu aksi karena ProgressBar di XML akan otomatis berputar
    }

    override fun getItemCount(): Int = listWisata.size
}