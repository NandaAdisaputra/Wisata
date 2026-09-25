package com.nandaadisaputra.wisata.network

/**
 * Data class untuk request login ke API.
 */
data class AuthRequest(
    val username: String,
    val password: String
)