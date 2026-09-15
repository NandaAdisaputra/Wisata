package com.nandaadisaputra.wisata.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nandaadisaputra.wisata.databinding.ItemWisataBinding
import com.nandaadisaputra.wisata.data.local.room.FavoriteWisata

class FavoriteWisataAdapter(
    private val onItemClick: (FavoriteWisata) -> Unit
) : ListAdapter<FavoriteWisata, FavoriteWisataAdapter.FavoriteViewHolder>(DIFF_CALLBACK) {

    inner class FavoriteViewHolder(private val binding: ItemWisataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wisata: FavoriteWisata) {
            binding.apply {
                // Set text ke view (Sesuaikan ID di XML item_wisata kamu)
                // Contoh: tvItemName, tvItemLokasi, dsb.
                tvNameWisata.text = wisata.namaWisata
                tvDescription.text = wisata.deskripsi

                // Load image menggunakan Glide
                Glide.with(itemView.context)
                    .load(wisata.fotoUrl)
                    .centerCrop()
                    .into(ivImage) // Sesuaikan ID ImageView di XML kamu

                // Handle klik pada item
                root.setOnClickListener {
                    onItemClick(wisata)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemWisataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FavoriteWisata>() {
            override fun areItemsTheSame(oldItem: FavoriteWisata, newItem: FavoriteWisata): Boolean {
                // Membandingkan apakah item ini adalah objek yang sama berdasarkan ID
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FavoriteWisata, newItem: FavoriteWisata): Boolean {
                // Membandingkan apakah seluruh isi kontennya sama
                return oldItem == newItem
            }
        }
    }
}