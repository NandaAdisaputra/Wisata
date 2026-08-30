package com.nandaadisaputra.wisata.utils

/**
 * Sealed class ini digunakan untuk membungkus status dari proses pengambilan data.
 * Sangat berguna agar Activity tahu kapan harus memunculkan animasi loading,
 * menampilkan data sukses, atau menampilkan pesan error.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}