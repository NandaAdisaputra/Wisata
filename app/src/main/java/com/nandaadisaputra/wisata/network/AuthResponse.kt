package com.nandaadisaputra.wisata.network

/**
 * Response dari API auth.php
 */
data class AuthResponse(
    val code: Int,
    val status: String,
    val message: String,
    val data: AuthData?
)

/**
 * Data login yang berada di dalam object "data".
 */
data class AuthData(
    val user: UserData?,
    val token: String?
)

/**
 * Data user.
 */
data class UserData(
    val id: Int,
    val username: String,
    val role: String
)