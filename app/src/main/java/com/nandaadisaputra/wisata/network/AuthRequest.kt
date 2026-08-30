package com.nandaadisaputra.wisata.network

/**
 * Data class ini merepresentasikan data yang akan dikirim (POST) ke server.
 * Properti di dalamnya harus sesuai dengan yang dibutuhkan oleh API (username & password).
 */
data class AuthRequest(
    val username: String,
    val password: String
)