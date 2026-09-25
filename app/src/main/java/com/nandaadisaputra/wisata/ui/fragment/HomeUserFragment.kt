package com.nandaadisaputra.wisata.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.viewModels
import com.nandaadisaputra.wisata.databinding.FragmentHomeUserBinding
import com.nandaadisaputra.wisata.ui.adapter.WisataAdapter
import com.nandaadisaputra.wisata.utils.UiState
import com.nandaadisaputra.wisata.utils.addInfiniteScrollListener
import com.nandaadisaputra.wisata.utils.hide
import com.nandaadisaputra.wisata.utils.hideKeyboard
import com.nandaadisaputra.wisata.utils.observeUiStateFragment
import com.nandaadisaputra.wisata.utils.onQueryTextChanged
import com.nandaadisaputra.wisata.utils.showToast
import com.nandaadisaputra.wisata.utils.startDetailWisataActivity
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel

/**
 * HomeFragment bertugas mengelola tampilan daftar tempat wisata,
 * pencarian (SearchView), penyegaran data (SwipeRefreshLayout),
 * pagination (Infinite Scroll), serta memantau data dari WisataViewModel.
 */
class HomeUserFragment : Fragment() {

    // Menggunakan pola backing property _binding untuk mencegah memory leak pada Fragment
    private var _binding: FragmentHomeUserBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel khusus Fragment Lifecycle
    private val viewModel: WisataViewModel by viewModels()

    // Adapter untuk mengelola item RecyclerView
    private lateinit var adapter: WisataAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Konfigurasi awal komponen UI dan observer ViewModel
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        setupObservers()

        // Ambil data halaman pertama jika state masih kosong/belum diisi
        if (viewModel.wisataState.value == null) {
            viewModel.fetchWisata(isRefresh = true)
        }
    }

    /**
     * Mengatur RecyclerView, Adapter, dan Infinite Scroll Listener untuk pagination.
     */
    private fun setupRecyclerView() {
        adapter = WisataAdapter { wisata ->
            requireContext().startDetailWisataActivity(wisata)
        }

        binding.rvWisata.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeUserFragment.adapter

            // Memasang Infinite Scroll untuk pagination otomatis saat scroll mencapai bagian bawah
            addInfiniteScrollListener(
                isLoading = { viewModel.isLoadingMore },
                onLoadMore = { viewModel.fetchWisata() }
            )
        }
    }

    /**
     * Mengatur fungsi SwipeRefreshLayout saat layar ditarik ke bawah (Pull to Refresh).
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()

            // Menutup keyboard melalui Activity host
            requireActivity().hideKeyboard()

            // Panggil ulang data dari halaman awal (isRefresh = true)
            viewModel.fetchWisata(isRefresh = true)
        }
    }

    /**
     * Mengatur fungsi pencarian tempat wisata pada SearchView.
     */
    private fun setupSearchView() {
        binding.searchView.onQueryTextChanged(
            onSubmit = { query ->
                if (query.isNotEmpty()) {
                    viewModel.searchWisata(query)
                }
            },
            onChange = { newText ->
                if (newText.isEmpty()) {
                    viewModel.searchWisata("")
                }
            }
        )
    }

    /**
     * Memantau (observe) aliran data dan status UiState dari WisataViewModel.
     */
    private fun setupObservers() {
        observeUiStateFragment(
            liveData = viewModel.wisataState,
            progressBar = binding.progressBar,
            onLoading = {
                // Sembunyikan ProgressBar jika loading berasal dari SwipeRefresh atau Load More
                if (viewModel.isLoadingMore || binding.swipeRefresh.isRefreshing) {
                    binding.progressBar.hide()
                }
            },
            onSuccess = { wisataList ->
                binding.swipeRefresh.isRefreshing = false

                val isLoadMoreActive = viewModel.isLoadMore.value ?: false
                adapter.setData(wisataList, isLoadMoreActive)
            },
            onError = { message ->
                binding.swipeRefresh.isRefreshing = false
                requireContext().showToast(message, Toast.LENGTH_LONG)
            }
        )

        // Memantau indikator loading footer untuk pagination
        viewModel.isLoadMore.observe(viewLifecycleOwner) { isLoadMore ->
            val currentState = viewModel.wisataState.value
            if (currentState is UiState.Success) {
                adapter.setData(currentState.data, isLoadMore)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Bersihkan binding untuk menghindari memory leak saat view dihancurkan
        _binding = null
    }
}