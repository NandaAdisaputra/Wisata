package com.nandaadisaputra.wisata.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nandaadisaputra.wisata.databinding.FragmentFavoriteBinding
import com.nandaadisaputra.wisata.ui.activity.DetailWisataActivity
import com.nandaadisaputra.wisata.ui.adapter.FavoriteWisataAdapter
import com.nandaadisaputra.wisata.viewmodel.FavoriteViewModel

// Kelas Fragment untuk menampilkan tampilan daftar tempat wisata favorit pengguna
class FavoriteFragment : Fragment() {

    // Variabel backing property untuk mengelola View Binding (bisa bernilai null saat view dihancurkan)
    private var _binding: FragmentFavoriteBinding? = null
    // Property getter non-null untuk mempermudah akses komponen UI pada layout FragmentFavoriteBinding
    private val binding get() = _binding!!

    // Menggunakan delegate activityViewModels() agar instance FavoriteViewModel dibagikan (shared) dengan Activity yang menaunginya
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

    // Variabel lateinit untuk menampung instance FavoriteWisataAdapter
    private lateinit var adapter: FavoriteWisataAdapter

    // Meng-inflate layout XML fragment_favorite dan mengembalikan root View-nya
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Mengatur logika UI setelah View pada Fragment berhasil dibuat sepenuhnya
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi adapter RecyclerView beserta penanganan aksi klik pada item
        adapter = FavoriteWisataAdapter { favoriteWisata ->
            // Membuat intent untuk berpindah ke DetailWisataActivity
            val intent = Intent(requireContext(), DetailWisataActivity::class.java)
            // Memasukkan data ID wisata ke dalam Intent Extra
            intent.putExtra(DetailWisataActivity.EXTRA_ID, favoriteWisata.id)
            // Menjalankan activity detail
            startActivity(intent)
        }

        // Mengatur LayoutManager agar tampilan item RecyclerView tersusun secara linier vertikal
        binding.rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        // Mengoperasikan adapter ke RecyclerView
        binding.rvFavorite.adapter = adapter

        // Mengamati (observe) data tempat wisata favorit dari LiveData secara real-time
        favoriteViewModel.getAllFavorite().observe(viewLifecycleOwner) { listFavorite ->
            // Mengatur visibilitas UI jika daftar favorit kosong (empty state)
            if (listFavorite.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvFavorite.visibility = View.GONE
            } else {
                // Sembunyikan pesan kosong, tampilkan RecyclerView, dan perbarui list data di adapter
                binding.tvEmptyState.visibility = View.GONE
                binding.rvFavorite.visibility = View.VISIBLE
                adapter.submitList(listFavorite)
            }
        }
    }

    // Membebaskan referensi _binding saat View dari Fragment dihancurkan untuk mencegah kebocoran memori (memory leak)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}