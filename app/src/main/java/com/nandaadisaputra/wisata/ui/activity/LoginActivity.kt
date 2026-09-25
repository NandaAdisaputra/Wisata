package com.nandaadisaputra.wisata.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.wisata.databinding.ActivityLoginBinding
import com.nandaadisaputra.wisata.network.ApiClient
import com.nandaadisaputra.wisata.network.AuthRequest
import com.nandaadisaputra.wisata.repository.AuthRepository
import com.nandaadisaputra.wisata.utils.SessionManager
import com.nandaadisaputra.wisata.utils.clearInputErrors
import com.nandaadisaputra.wisata.utils.hideKeyboard
import com.nandaadisaputra.wisata.utils.observeUiState
import com.nandaadisaputra.wisata.utils.setOnSingleClickListener
import com.nandaadisaputra.wisata.utils.showToast
import com.nandaadisaputra.wisata.utils.startActivity
import com.nandaadisaputra.wisata.utils.trimmedText
import com.nandaadisaputra.wisata.viewmodel.AuthViewModel
import com.nandaadisaputra.wisata.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: AuthViewModel by viewModels(
        factoryProducer = {
            AuthViewModelFactory(
                AuthRepository(
                    ApiClient.instance,
                    sessionManager
                )
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Cek apakah user sudah login
        if (sessionManager.isLoggedIn()) {

            val role = sessionManager.getRole()

            if (role.equals("admin", ignoreCase = true)) {
                startActivity<AdminWisataActivity>(clearTask = true)
            } else {
                startActivity<MainActivity>(clearTask = true)
            }

            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupActionListeners()
    }

    private fun setupActionListeners() {

        binding.btnLogin.setOnSingleClickListener {

            hideKeyboard()

            val username = binding.edtUsername.trimmedText
            val password = binding.edtPassword.trimmedText

            clearInputErrors(
                binding.edtUsername,
                binding.edtPassword
            )

            when {
                username.isEmpty() -> {
                    binding.edtUsername.error = "Username tidak boleh kosong!"
                    binding.edtUsername.requestFocus()
                }

                password.isEmpty() -> {
                    binding.edtPassword.error = "Password tidak boleh kosong!"
                    binding.edtPassword.requestFocus()
                }

                else -> {
                    viewModel.login(
                        AuthRequest(
                            username = username,
                            password = password
                        )
                    )
                }
            }
        }

        binding.tvGoToRegister.setOnSingleClickListener {
            startActivity<RegisterActivity>()
        }
    }

    private fun setupObservers() {

        observeUiState(
            liveData = viewModel.loginState,
            progressBar = binding.progressBar,

            onLoading = {
                binding.btnLogin.isEnabled = false

                clearInputErrors(
                    binding.edtUsername,
                    binding.edtPassword
                )
            },

            onSuccess = { response ->

                binding.btnLogin.isEnabled = true

                val user = response.data?.user
                val role = user?.role?.trim()

                showToast(response.message)

                if (role.equals("admin", ignoreCase = true)) {

                    startActivity<AdminWisataActivity>(
                        clearTask = true
                    )

                } else {

                    startActivity<MainActivity>(
                        clearTask = true
                    )
                }

                finish()
            },

            onError = { message ->

                binding.btnLogin.isEnabled = true

                showToast(message)
            }
        )
    }
}