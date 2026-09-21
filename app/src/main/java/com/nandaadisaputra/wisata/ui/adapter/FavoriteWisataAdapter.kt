package com.nandaadisaputra.wisata.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nandaadisaputra.wisata.databinding.ItemWisataBinding
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisata

// Kelas Adapter untuk menampilkan daftar tempat wisata favorit dalam RecyclerView
// Menggunakan ListAdapter dengan DiffUtil untuk efisiensi pembaruan item list secara otomatis
class FavoriteWisataAdapter(
    // Higher-order function / lambda callback untuk menangani aksi ketika salah satu item diklik
    private val onItemClick: (FavoriteWisata) -> Unit
) : ListAdapter<FavoriteWisata, FavoriteWisataAdapter.FavoriteViewHolder>(DIFF_CALLBACK) {

    // Inner class ViewHolder untuk mereferensikan dan memasangkan (bind) data ke elemen UI item XML
    inner class FavoriteViewHolder(private val binding: ItemWisataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk mengikat data dari objek FavoriteWisata ke dalam komponen UI item
        fun bind(wisata: FavoriteWisata) {
            binding.apply {
                // Memasukkan teks data nama dan deskripsi wisata ke TextView masing-masing
                tvNameWisata.text = wisata.namaWisata
                tvDescription.text = wisata.deskripsi

                // Memuat gambar dari fotoUrl ke ImageView (ivImage) menggunakan library Glide
                Glide.with(itemView.context)
                    .load(wisata.fotoUrl)
                    .centerCrop()
                    .into(ivImage)

                // Menyiapkan listener klik pada seluruh area item (root View) untuk meneruskan data melalui callback
                root.setOnClickListener {
                    onItemClick(wisata)
                }
            }
        }
    }

    // Membentuk/meng-inflate layout XML item_wisata dan mengembalikannya dalam bentuk ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemWisataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    // Menghubungkan ViewHolder dengan data FavoriteWisata sesuai urutan/posisi indeks pada daftar
    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Companion object yang mendefinisikan kriteria perbandingan DiffUtil untuk efisiensi performa RecyclerView
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FavoriteWisata>() {
            // Memeriksa apakah dua objek merupakan item yang sama secara unik (berdasarkan ID)
            override fun areItemsTheSame(oldItem: FavoriteWisata, newItem: FavoriteWisata): Boolean {
                return oldItem.id == newItem.id
            }

            // Memeriksa apakah seluruh konten/properti dari dua objek identik atau mengalami perubahan
            override fun areContentsTheSame(oldItem: FavoriteWisata, newItem: FavoriteWisata): Boolean {
                return oldItem == newItem
            }
        }
    }
}