package com.nandaadisaputra.wisata.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nandaadisaputra.wisata.databinding.ItemLoadingBinding
import com.nandaadisaputra.wisata.databinding.ItemWisataBinding
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.utils.loadImage
import com.nandaadisaputra.wisata.utils.setOnSingleClickListener

/**
 * Adapter RecyclerView untuk menampilkan daftar tempat wisata
 * menggunakan DiffUtil untuk pembaruan data yang efisien serta menangani indikator footer loading.
 *
 * @param onItemClick Callback lambda yang dipanggil saat salah satu item wisata diklik oleh pengguna.
 */
class WisataAdapter(
    private val onItemClick: (Wisata) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // List yang menampung objek Wisata. Nilai 'null' digunakan sebagai penanda posisi item footer loading.
    private val listWisata = ArrayList<Wisata?>()

    companion object {
        private const val VIEW_TYPE_ITEM = 0    // Penanda tipe tampilan untuk data wisata biasa
        private const val VIEW_TYPE_LOADING = 1 // Penanda tipe tampilan untuk footer loading di bawah
    }

    /**
     * Memperbarui isi data list menggunakan DiffUtil secara efisien tanpa merefresh seluruh item.
     *
     * @param data Daftar objek Wisata yang didapat dari ViewModel.
     * @param isLoadingMore Status penanda apakah data halaman berikutnya sedang dimuat.
     */
    fun setData(data: List<Wisata>, isLoadingMore: Boolean) {
        val newList = ArrayList<Wisata?>(data)

        // Sisipkan item null di bagian paling akhir jika sedang memuat data halaman berikutnya (infinite scroll)
        if (isLoadingMore) {
            newList.add(null)
        }

        // Hitung perbedaan data lama dan data baru menggunakan WisataDiffCallback
        val diffCallback = WisataDiffCallback(listWisata, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Perbarui list internal dan kirimkan pembaruan spesifik ke RecyclerView
        listWisata.clear()
        listWisata.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Menentukan tipe tampilan item berdasarkan keberadaan data (null untuk footer loading).
     */
    override fun getItemViewType(position: Int): Int {
        return if (listWisata[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    /**
     * Membuat instance ViewHolder sesuai dengan tipe tampilan item (WisataViewHolder atau LoadingViewHolder).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_ITEM) {
            // Inflate layout item_wisata.xml
            val binding = ItemWisataBinding.inflate(inflater, parent, false)
            WisataViewHolder(binding)
        } else {
            // Inflate layout item_loading.xml untuk indikator pagination
            val binding = ItemLoadingBinding.inflate(inflater, parent, false)
            LoadingViewHolder(binding)
        }
    }

    /**
     * Menghubungkan (bind) data objek Wisata ke komponen UI pada ViewHolder sesuai posisi item.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WisataViewHolder) {
            val wisata = listWisata[position]
            if (wisata != null) {
                holder.bind(wisata)
            }
        }
    }

    /**
     * Mengembalikan jumlah seluruh item yang ada di dalam list (termasuk item null loading jika ada).
     */
    override fun getItemCount(): Int = listWisata.size

    /**
     * ViewHolder khusus untuk mengelola tampilan item data wisata (item_wisata.xml).
     */
    inner class WisataViewHolder(private val binding: ItemWisataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Memasukkan data properti objek Wisata ke dalam View/UI.
         */
        fun bind(wisata: Wisata) {
            // Menampilkan nama dan deskripsi tempat wisata
            binding.tvNameWisata.text = wisata.namaWisata ?: "Tanpa Nama"
            binding.tvDescription.text = wisata.deskripsi ?: "Tidak ada deskripsi"

            // Mengambil URL foto dari properti fotoUrl atau fallback ke foto
            val imageUrl = wisata.fotoUrl ?: wisata.foto

            // Pemuatan gambar header menggunakan extension function loadImage dari Helper.kt
            binding.ivImage.loadImage(imageUrl)

            // Memasang event klik tunggal pada item (menggunakan setOnSingleClickListener untuk cegah double-click)
            itemView.setOnSingleClickListener {
                onItemClick(wisata)
            }
        }
    }

    /**
     * ViewHolder khusus untuk menampung layout footer loading (item_loading.xml).
     */
    class LoadingViewHolder(binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Callback DiffUtil untuk membandingkan item lama dan item baru secara presisi di background thread.
     */
    private class WisataDiffCallback(
        private val oldList: List<Wisata?>,
        private val newList: List<Wisata?>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        /**
         * Memeriksa apakah dua objek merepresentasikan item yang sama (berdasarkan ID).
         */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return when {
                oldItem == null && newItem == null -> true
                oldItem == null || newItem == null -> false
                else -> oldItem.id == newItem.id
            }
        }

        /**
         * Memeriksa apakah konten/isi dari dua objek identik satu sama lain.
         */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem == newItem
        }
    }
}