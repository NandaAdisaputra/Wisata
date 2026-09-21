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

// Adapter RecyclerView untuk menampilkan daftar tempat wisata dengan efisiensi DiffUtil serta penanganan footer indikator loading (pagination)
class WisataAdapter(
    // Callback lambda yang dipanggil saat salah satu item wisata diklik oleh pengguna
    private val onItemClick: (Wisata) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // List internal yang menampung objek Wisata. Nilai 'null' digunakan sebagai penanda posisi footer loading
    private val listWisata = ArrayList<Wisata?>()

    // Companion object untuk mendefinisikan konstanta jenis tampilan (View Type)
    companion object {
        private const val VIEW_TYPE_ITEM = 0    // Penanda tipe tampilan untuk item data wisata biasa
        private const val VIEW_TYPE_LOADING = 1 // Penanda tipe tampilan untuk item indikator loading di bagian paling bawah
    }

    // Memperbarui isi data list menggunakan DiffUtil secara efisien tanpa merefresh seluruh elemen item
    fun setData(data: List<Wisata>, isLoadingMore: Boolean) {
        val newList = ArrayList<Wisata?>(data)

        // Sisipkan item null di bagian paling akhir jika sedang memuat data halaman berikutnya (infinite scroll)
        if (isLoadingMore) {
            newList.add(null)
        }

        // Membandingkan data lama dan data baru menggunakan WisataDiffCallback
        val diffCallback = WisataDiffCallback(listWisata, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Perbarui daftar internal dan kirim pembaruan spesifik ke RecyclerView
        listWisata.clear()
        listWisata.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    // Menentukan tipe tampilan item berdasarkan ada/tidaknya data (mengembalikan VIEW_TYPE_LOADING jika data bernilai null)
    override fun getItemViewType(position: Int): Int {
        return if (listWisata[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    // Membuat instance ViewHolder sesuai dengan tipe tampilan item (WisataViewHolder atau LoadingViewHolder)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_ITEM) {
            // Meng-inflate layout item_wisata.xml untuk item data biasa
            val binding = ItemWisataBinding.inflate(inflater, parent, false)
            WisataViewHolder(binding)
        } else {
            // Meng-inflate layout item_loading.xml untuk item indikator loading/pagination
            val binding = ItemLoadingBinding.inflate(inflater, parent, false)
            LoadingViewHolder(binding)
        }
    }

    // Menghubungkan (bind) data objek Wisata ke komponen UI pada ViewHolder sesuai posisi item pada list
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WisataViewHolder) {
            val wisata = listWisata[position]
            if (wisata != null) {
                holder.bind(wisata)
            }
        }
    }

    // Mengembalikan jumlah total seluruh item di dalam list (termasuk item null loading jika ada)
    override fun getItemCount(): Int = listWisata.size

    // ViewHolder khusus untuk mengelola dan memasangkan data ke tampilan item wisata (item_wisata.xml)
    inner class WisataViewHolder(private val binding: ItemWisataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Mengikat properti objek Wisata ke elemen UI pada layout
        fun bind(wisata: Wisata) {
            // Menampilkan nama dan deskripsi tempat wisata, dilengkapi nilai default fallback jika null
            binding.tvNameWisata.text = wisata.namaWisata ?: "Tanpa Nama"
            binding.tvDescription.text = wisata.deskripsi ?: "Tidak ada deskripsi"

            // Mengambil URL foto utama atau fallback ke properti foto alternatif
            val imageUrl = wisata.fotoUrl ?: wisata.foto

            // Memuat gambar ke ImageView menggunakan fungsi ekstensi loadImage
            binding.ivImage.loadImage(imageUrl)

            // Memasang event klik tunggal pada item (menggunakan setOnSingleClickListener untuk mencegah double-click)
            itemView.setOnSingleClickListener {
                onItemClick(wisata)
            }
        }
    }

    // ViewHolder khusus untuk menampung layout footer indikator loading (item_loading.xml)
    class LoadingViewHolder(binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Callback DiffUtil untuk membandingkan perbedaan item lama dan item baru secara efisien di background thread
    private class WisataDiffCallback(
        private val oldList: List<Wisata?>,
        private val newList: List<Wisata?>
    ) : DiffUtil.Callback() {

        // Mengembalikan ukuran list lama
        override fun getOldListSize(): Int = oldList.size

        // Mengembalikan ukuran list baru
        override fun getNewListSize(): Int = newList.size

        // Memeriksa apakah dua objek merepresentasikan item yang sama (berdasarkan ID unik)
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return when {
                oldItem == null && newItem == null -> true
                oldItem == null || newItem == null -> false
                else -> oldItem.id == newItem.id
            }
        }

        // Memeriksa apakah seluruh konten/isi dari dua objek identik satu sama lain
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem == newItem
        }
    }
}