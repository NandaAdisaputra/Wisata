package com.nandaadisaputra.wisata.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nandaadisaputra.wisata.databinding.FragmentProfileBinding

import android.content.Intent
import androidx.fragment.app.viewModels
import com.nandaadisaputra.wisata.network.ApiClient
import com.nandaadisaputra.wisata.repository.AuthRepository
import com.nandaadisaputra.wisata.ui.activity.LoginActivity
import com.nandaadisaputra.wisata.utils.SessionManager
import com.nandaadisaputra.wisata.viewmodel.AuthViewModel
import com.nandaadisaputra.wisata.viewmodel.AuthViewModelFactory

class ProfileFragment : Fragment() {

    // View Binding untuk mengakses elemen layout tanpa findViewById
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel menggunakan Factory Pattern
    private val viewModel: AuthViewModel by viewModels {
        val sessionManager = SessionManager(requireContext())
        val repository = AuthRepository(ApiClient.instance, sessionManager)
        AuthViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout XML dan dapatkan objek binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Event Listener ketika tombol "Keluar" diklik
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        // Mengamati (Observe) perubahan status logout dari ViewModel
        viewModel.logoutState.observe(viewLifecycleOwner) { isLoggedOut ->
            if (isLoggedOut) {
                // Buat intent untuk berpindah ke LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    // Flag ini membersihkan tumpukan halaman (backstack) agar user tidak bisa 'back' ke ProfileFragment
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish() // Tutup Host Activity
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hindari memory leak dengan mengosongkan _binding saat view dihancurkan
        _binding = null
    }
}