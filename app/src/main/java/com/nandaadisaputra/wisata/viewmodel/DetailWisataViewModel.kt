package com.nandaadisaputra.wisata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.repository.WisataRepository
import com.nandaadisaputra.wisata.utils.UiState
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola logika bisnis dan status UI pada layar Detail Wisata.
 */
class DetailWisataViewModel : ViewModel() {

    // Instance repository untuk melakukan pemanggilan API detail
    private val repository = WisataRepository()

    // LiveData privat yang menampung kondisi UiState (Loading, Success, Error)
    private val _detailState = MutableLiveData<UiState<Wisata>>()
    // LiveData publik yang diamati oleh DetailWisataActivity
    val detailState: LiveData<UiState<Wisata>> = _detailState

    /**
     * Memanggil API detail tempat wisata dari repository berdasarkan ID.
     * @param id Unique ID dari tempat wisata yang dipilih.
     */
    fun fetchDetailWisata(id: Int) {
        viewModelScope.launch {
            // Tampilkan status Loading sebelum request API berjalan
            _detailState.value = UiState.Loading

            // Panggil API dari repository yang sudah langsung mengembalikan UiState<Wisata>
            _detailState.value = repository.getDetailWisata(id)
        }
    }
}