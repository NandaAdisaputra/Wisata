package com.nandaadisaputra.wisata.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Mengonversi String biasa menjadi RequestBody dengan media type text/plain
 */
fun String.toPlainRequestBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}

/**
 * Mengonversi Uri gambar lokal menjadi MultipartBody.Part untuk Retrofit Upload
 */
fun Uri.toMultipartBody(context: Context, paramName: String = "foto"): MultipartBody.Part? {
    val file = uriToFile(this, context) ?: return null
    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(paramName, file.name, requestFile)
}

/**
 * Menyimpan data dari Uri ke File Cache sementara
 */
private fun uriToFile(selectedImg: Uri, context: Context): File? {
    return try {
        val contentResolver = context.contentResolver
        val myFile = File.createTempFile("upload_image", ".jpg", context.cacheDir)
        val inputStream = contentResolver.openInputStream(selectedImg) ?: return null
        val outputStream = FileOutputStream(myFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.close()
        inputStream.close()
        myFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}