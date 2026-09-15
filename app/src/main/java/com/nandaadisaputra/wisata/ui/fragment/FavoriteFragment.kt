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

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    // Gunakan activityViewModels agar berbagi data dengan scope Activity
    // Ini sangat berguna jika DetailActivity dan FavoriteFragment berada dalam satu Navigation/Activity host
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

    private lateinit var adapter: FavoriteWisataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = FavoriteWisataAdapter { favoriteWisata ->
            val intent = Intent(requireContext(), DetailWisataActivity::class.java)
            intent.putExtra(DetailWisataActivity.EXTRA_ID, favoriteWisata.id)
            startActivity(intent)
        }

        binding.rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorite.adapter = adapter

        // Observe Data dari ViewModel (yang sekarang mengambil dari Repository)
        favoriteViewModel.getAllFavorite().observe(viewLifecycleOwner) { listFavorite ->
            if (listFavorite.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvFavorite.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvFavorite.visibility = View.VISIBLE
                adapter.submitList(listFavorite)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}