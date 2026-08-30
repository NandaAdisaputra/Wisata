package com.nandaadisaputra.wisata.network

/**
 * Data class ini merepresentasikan balasan (response) dari server.
 * Nama variabel harus sama persis dengan key JSON yang dihasilkan oleh auth.php.
 */
data class AuthResponse(
    val code: Int,
    val status: String,
    val message: String,
    val user: UserData?, // Menggunakan '?' karena saat register, data user mungkin null
    val token: String?   // Menggunakan '?' karena token hanya ada saat login sukses
)

data class UserData(
    val id: Int,
    val username: String
)