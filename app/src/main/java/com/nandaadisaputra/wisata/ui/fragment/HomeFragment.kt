package com.nandaadisaputra.wisata.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nandaadisaputra.wisata.databinding.FragmentHomeBinding
import com.nandaadisaputra.wisata.ui.activity.AddWisataActivity
import com.nandaadisaputra.wisata.ui.activity.DetailWisataActivity
import com.nandaadisaputra.wisata.ui.adapter.WisataAdapter
import com.nandaadisaputra.wisata.utils.*
import com.nandaadisaputra.wisata.viewmodel.WisataViewModel

// Kelas Fragment untuk halaman utama (Home) yang menampilkan daftar tempat wisata
class HomeFragment : Fragment() {

    // Menyimpan referensi nullable untuk View Binding agar menghindari memory leak
    private var _binding: FragmentHomeBinding? = null
    // Properti non-null binding yang hanya valid digunakan antara onCreateView dan onDestroyView
    private val binding get() = _binding!!

    // Inisialisasi ViewModel terikat pada siklus hidup Fragment menggunakan delegasi by viewModels()
    private val viewModel: WisataViewModel by viewModels()

    // Deklarasi Adapter untuk RecyclerView daftar wisata
    private lateinit var wisataAdapter: WisataAdapter

    // Launcher untuk mendeteksi hasil (result) saat kembali dari AddWisataActivity atau DetailWisataActivity
    private val actionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Jika aktivitas tujuan mengembalikan RESULT_OK, lakukan refresh daftar data wisata
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.fetchWisata(isRefresh = true)
        }
    }

    // Siklus hidup untuk meng-inflate layout Fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Menginisialisasi View Binding untuk layout FragmentHome
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Dipanggil setelah View dibuat sepenuhnya
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Memanggil fungsi-fungsi konfigursi tampilan dan observer
        setupRecyclerView()   // Menyiapkan RecyclerView & adapter
        setupSearchView()     // Menyiapkan fitur pencarian data
        setupSwipeRefresh()   // Menyiapkan fitur tarik-untuk-memuat-ulang (Swipe Refresh)
        setupFab()            // Menyiapkan Floating Action Button (Tambah Wisata)
        setupObservers()      // Mengamati perubahan data/state pada ViewModel

        // Muat data awal jika state data saat ini belum sukses diproses
        if (viewModel.wisataState.value !is UiState.Success) {
            viewModel.fetchWisata(isRefresh = true)
        }
    }

    // Mengonfigurasi RecyclerView beserta Adapter dan fitur scroll listener-nya
    private fun setupRecyclerView() {
        // Inisialisasi adapter beserta callback klik item untuk berpindah ke DetailWisataActivity
        wisataAdapter = WisataAdapter { wisata ->
            val intent = Intent(requireContext(), DetailWisataActivity::class.java).apply {
                putExtra(DetailWisataActivity.EXTRA_WISATA, wisata)
                putExtra(DetailWisataActivity.EXTRA_ID, wisata.id)
            }
            actionLauncher.launch(intent) // Jalankan launcher agar bisa mendeteksi aksi edit/hapus
        }

        binding.rvWisata.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wisataAdapter
            setHasFixedSize(true)

            // Menambahkan extension listener untuk fitur infinite scroll (pagination load more)
            addInfiniteScrollListener(
                isLoading = { viewModel.isLoadingMore },
                onLoadMore = { viewModel.fetchWisata() }
            )

            // Listener untuk menyembunyikan/menampilkan FAB berdasarkan arah scroll layar
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Sembunyikan FAB jika scroll ke bawah, tampilkan jika scroll ke atas
                    if (dy > 0 && binding.fabAddWisata.isShown) {
                        binding.fabAddWisata.hide()
                    } else if (dy < 0 && !binding.fabAddWisata.isShown) {
                        binding.fabAddWisata.show()
                    }
                }
            })
        }
    }

    // Menyiapkan listener aksi klik pada Floating Action Button (FAB) untuk menambah wisata baru
    private fun setupFab() {
        binding.fabAddWisata.setOnClickListener {
            val intent = Intent(requireContext(), AddWisataActivity::class.java)
            actionLauncher.launch(intent)
        }
    }

    // Mengonfigurasi komponen SwipeRefreshLayout saat pengguna menarik layar ke bawah
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Bersihkan kolom pencarian dan lepaskan fokus keyboard
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()

            requireActivity().hideKeyboard()
            // Muat ulang daftar wisata dari awal
            viewModel.fetchWisata(isRefresh = true)
        }
    }

    // Menyiapkan listener pencarian pada SearchView
    private fun setupSearchView() {
        binding.searchView.onQueryTextChanged(
            onSubmit = { query ->
                // Eksekusi pencarian saat pengguna menekan tombol Submit
                if (query.isNotEmpty()) {
                    viewModel.searchWisata(query)
                }
            },
            onChange = { newText ->
                // Reset daftar wisata jika kolom teks pencarian dikosongkan
                if (newText.isEmpty()) {
                    viewModel.searchWisata("")
                }
            }
        )
    }

    // Mengamati (observe) perubahan state dan data pada LiveData yang ada di ViewModel
    private fun setupObservers() {
        // Obserasi state UI utama untuk daftar wisata (Loading, Success, Error)
        observeUiStateFragment(
            liveData = viewModel.wisataState,
            progressBar = binding.progressBar,
            onLoading = {
                // Sembunyikan progress bar tengah jika indikator pagination atau swipe-refresh sedang aktif
                if (viewModel.isLoadingMore || binding.swipeRefresh.isRefreshing) {
                    binding.progressBar.hide()
                }
            },
            onSuccess = { wisataList ->
                binding.swipeRefresh.isRefreshing = false
                val isLoadMoreActive = viewModel.isLoadMore.value ?: false
                // Masukkan data terbaru ke dalam adapter
                wisataAdapter.setData(wisataList, isLoadMoreActive)
            },
            onError = { message ->
                binding.swipeRefresh.isRefreshing = false
                requireContext().showToast(message, Toast.LENGTH_LONG)
            }
        )

        // Memantau penanda pagination/load more untuk menampilkan/menyembunyikan loading di bagian footer adapter
        viewModel.isLoadMore.observe(viewLifecycleOwner) { isLoadMore ->
            val currentState = viewModel.wisataState.value
            if (currentState is UiState.Success) {
                wisataAdapter.setData(currentState.data, isLoadMore)
            }
        }
    }

    // Siklus hidup pembersihan view saat Fragment dihancurkan untuk mencegah Memory Leak
    override fun onDestroyView() {
        binding.rvWisata.adapter = null // Lepas ikatan adapter dari RecyclerView
        _binding = null                 // Clear referensi binding
        super.onDestroyView()
    }
}