package com.nandaadisaputra.wisata.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {

    // SharedPreferences: Tempat menyimpan data sederhana berpasangan Key-Value di penyimpanan internal HP
    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "wisata_session" // Nama file SharedPreferences
        private const val IS_LOGIN = "is_login"       // Key untuk status login (Boolean)
        private const val KEY_TOKEN = "key_token"     // Key untuk menyimpan token JWT/API
        private const val KEY_USER_ID = "key_user_id" // Key untuk menyimpan ID user
        private const val KEY_USERNAME = "key_username" // Key untuk menyimpan username
    }

    /**
     * Memproses dan menyimpan data sesi ketika login berhasil.
     * Menggunakan ekstensi edit {} agar perubahan dilakukan secara atomic dan aman.
     */
    fun saveSession(token: String, userId: Int, username: String,role: String) {
        pref.edit {
            putBoolean(IS_LOGIN, true)
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString("KEY_ROLE", role)
                .apply()
        }
    }
    fun getRole(): String? {
        return pref.getString("KEY_ROLE", "user") // Default ke user jika kosong
    }

    /**
     * Mengecek apakah pengguna sedang dalam keadaan login.
     * Mengembalikan nilai default 'false' jika key IS_LOGIN belum ada.
     */
    fun isLoggedIn(): Boolean = pref.getBoolean(IS_LOGIN, false)

    /**
     * Mengambil token autentikasi yang tersimpan.
     */
    fun getToken(): String? = pref.getString(KEY_TOKEN, null)

    /**
     * Mengambil nama pengguna yang tersimpan.
     */
    fun getUsername(): String? = pref.getString(KEY_USERNAME, null)

    /**
     * Mengambil ID pengguna yang tersimpan.
     * Mengembalikan -1 jika data tidak ditemukan.
     */
    fun getUserId(): Int = pref.getInt(KEY_USER_ID, -1)

    /**
     * Menghapus seluruh data sesi (digunakan saat Logout).
     */
    fun clearSession() {
        pref.edit {
            clear()
        }
    }
}