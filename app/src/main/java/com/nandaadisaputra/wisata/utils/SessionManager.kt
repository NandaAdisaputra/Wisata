package com.nandaadisaputra.wisata.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {

    // SharedPreferences untuk menyimpan data sesi pengguna
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "wisata_session"

        // Key SharedPreferences
        private const val IS_LOGIN = "is_login"
        private const val KEY_TOKEN = "key_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_ROLE = "key_role"
    }

    /**
     * Menyimpan data sesi ketika login berhasil.
     */
    fun saveSession(
        token: String,
        userId: Int,
        username: String,
        role: String
    ) {
        pref.edit {
            putBoolean(IS_LOGIN, true)
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role)
        }
    }

    /**
     * Mengambil role pengguna.
     */
    fun getRole(): String? {
        return pref.getString(KEY_ROLE, "user")
    }

    /**
     * Mengecek apakah pengguna sudah login.
     */
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(IS_LOGIN, false)
    }

    /**
     * Mengambil token.
     */
    fun getToken(): String? {
        return pref.getString(KEY_TOKEN, null)
    }

    /**
     * Mengambil username.
     */
    fun getUsername(): String? {
        return pref.getString(KEY_USERNAME, null)
    }

    /**
     * Mengambil ID user.
     */
    fun getUserId(): Int {
        return pref.getInt(KEY_USER_ID, -1)
    }

    /**
     * Menghapus seluruh data sesi.
     */
    fun clearSession() {
        pref.edit {
            clear()
        }
    }
}