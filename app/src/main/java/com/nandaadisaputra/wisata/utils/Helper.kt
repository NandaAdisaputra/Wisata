package com.nandaadisaputra.wisata.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nandaadisaputra.wisata.model.Wisata
import com.nandaadisaputra.wisata.ui.activity.DetailWisataActivity
import java.text.NumberFormat
import java.util.Locale
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.nandaadisaputra.wisata.ui.activity.DetailWisataUserActivity

// ============================================================================
// 1. VIEW & VISIBILITY EXTENSIONS
// ============================================================================

/**
 * Menampilkan View (View.VISIBLE)
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Menyembunyikan View (View.GONE)
 */
fun View.hide() {
    visibility = View.GONE
}

// ============================================================================
// 2. DIALOG & TOAST EXTENSIONS
// ============================================================================

/**
 * Menampilkan Toast secara singkat dan fleksibel.
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Menampilkan AlertDialog informasi standar.
 */
fun Context.showAlertDialog(
    title: String,
    message: String,
    positiveButtonText: String = "OK",
    onPositiveClick: (() -> Unit)? = null
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick?.invoke()
            dialog.dismiss()
        }
        .setCancelable(true)
        .show()
}

/**
 * Menampilkan AlertDialog konfirmasi (opsi Ya/Batal).
 */
fun Context.showConfirmationDialog(
    title: String,
    message: String,
    positiveButtonText: String = "Ya",
    negativeButtonText: String = "Batal",
    onPositiveClick: () -> Unit
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick()
            dialog.dismiss()
        }
        .setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(true)
        .show()
}

// ============================================================================
// 3. GLIDE IMAGE EXTENSION
// ============================================================================

/**
 * Memuat gambar menggunakan Glide.
 */
fun ImageView.loadImage(url: String?) {
    Glide.with(this.context)
        .load(url)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

// ============================================================================
// 4. FORMATTER EXTENSION
// ============================================================================

/**
 * Mengubah angka integer ke format mata uang Rupiah (IDR).
 */
fun Int?.toRupiahFormat(): String {
    return if (this != null && this > 0) {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        formatRupiah.format(this)
    } else {
        "Gratis / Bebas Biaya"
    }
}

// ============================================================================
// 5. INTENT & PARCELABLE EXTENSIONS
// ============================================================================

/**
 * Navigasi perpindahan ke DetailWisataActivity.
 */
fun Context.startDetailWisataActivity(wisata: Wisata) {
    val intent = Intent(this, DetailWisataUserActivity::class.java).apply {
        putExtra(DetailWisataActivity.EXTRA_WISATA, wisata)
        putExtra(DetailWisataActivity.EXTRA_ID, wisata.id)
    }
    startActivity(intent)
}

/**
 * Mengambil Parcelable Extra dari Intent dengan aman lintas versi Android (Tiramisu Compat).
 */
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key) as? T
    }
}

// ============================================================================
// 6. UTILITY / KEYBOARD EXTENSION
// ============================================================================

/**
 * Menyembunyikan keyboard virtual dari layar.
 */
fun Activity.hideKeyboard() {
    val view = currentFocus ?: View(this)
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

// ============================================================================
// 7. CLICK LISTENER EXTENSION (Pencegah Double-Click / Spam Click)
// ============================================================================

/**
 * Extension function untuk menangani aksi klik (OnClickListener) pada View
 * sekaligus mencegah double-click / spam click secara tidak disengaja.
 *
 * @param debounceTime Waktu jeda minimal antar-klik dalam milidetik (default: 600ms).
 * @param action Lambda yang akan dieksekusi saat View berhasil diklik.
 */
fun View.setOnSingleClickListener(debounceTime: Long = 600L, action: (View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            lastClickTime = SystemClock.elapsedRealtime()
            action(v)
        }
    })
}

// ============================================================================
// 8. ACTION BAR & STRING HELPER EXTENSIONS
// ============================================================================

/**
 * Extension function untuk mengonfigurasi Title dan Tombol Back (HomeAsUp) pada ActionBar.
 */
fun AppCompatActivity.setupActionBar(title: String, showBackButton: Boolean = true) {
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(showBackButton)
        this.title = title
    }
}

/**
 * Extension function untuk memberikan nilai default jika String ber-value null atau kosong (blank).
 */
fun String?.orDefault(defaultValue: String = "-"): String {
    return if (this.isNullOrBlank()) defaultValue else this
}

/**
 * Extension function generic untuk mengamati LiveData<UiState<T>> sekaligus
 * menangani visibility ProgressBar, Toast Error, serta Callback kustom secara otomatis.
 *
 * @param liveData Aliran data LiveData yang membawa objek UiState<T>.
 * @param progressBar View indikator pemuatan data (opsional).
 * @param onLoading Callback opsional saat status UiState.Loading aktif.
 * @param onError Callback opsional saat terjadi UiState.Error (default: menampilkan Toast).
 * @param onSuccess Callback yang wajib diisi untuk menangani data saat UiState.Success.
 */
fun <T> AppCompatActivity.observeUiState(
    liveData: LiveData<UiState<T>>,
    progressBar: View? = null,
    onLoading: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    onSuccess: (T) -> Unit
) {
    liveData.observe(this) { state ->
        when (state) {
            is UiState.Loading -> {
                progressBar?.show()
                onLoading?.invoke()
            }
            is UiState.Success -> {
                progressBar?.hide()
                onSuccess(state.data)
            }
            is UiState.Error -> {
                progressBar?.hide()
                if (onError != null) {
                    onError(state.message)
                } else {
                    showToast(state.message, Toast.LENGTH_LONG)
                }
            }
        }
    }
}
/**
 * Extension function observeUiState khusus untuk Fragment.
 * Menggunakan viewLifecycleOwner untuk menghindari memory leak.
 */
fun <T> Fragment.observeUiStateFragment(
    liveData: LiveData<UiState<T>>,
    progressBar: View? = null,
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    liveData.observe(viewLifecycleOwner) { state ->
        when (state) {
            is UiState.Loading -> {
                progressBar?.show()
                onLoading()
            }
            is UiState.Success -> {
                progressBar?.hide()
                onSuccess(state.data)
            }
            is UiState.Error -> {
                progressBar?.hide()
                onError(state.message)
            }
        }
    }
}
// ============================================================================
// 9. RECYCLERVIEW & SEARCHVIEW EXTENSION FUNCTIONS
// ============================================================================

/**
 * Extension function untuk menangani Infinite Scrolling / Pagination pada RecyclerView.
 *
 * @param isLoading Lambda penguji apakah status memuat data sedang berjalan.
 * @param onLoadMore Callback yang dipanggil saat scroll pengguna menyentuh bagian terbawah.
 */
fun RecyclerView.addInfiniteScrollListener(
    isLoading: () -> Boolean,
    onLoadMore: () -> Unit
) {
    val layoutManager = this.layoutManager as? LinearLayoutManager ?: return

    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if (!isLoading()) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    onLoadMore()
                }
            }
        }
    })
}

/**
 * Extension function untuk menangani event perubahan teks dan tombol submit pada SearchView.
 */
inline fun SearchView.onQueryTextChanged(
    crossinline onSubmit: (String) -> Unit = {},
    crossinline onChange: (String) -> Unit = {}
) {
    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            onSubmit(query.orEmpty())
            clearFocus() // Otomatis menutup keyboard setelah tombol search ditekan
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            onChange(newText.orEmpty())
            return true
        }
    })
}
// ============================================================================
// EDITTEXT & FORM HELPER EXTENSIONS
// ============================================================================

/**
 * Extension property untuk mengambil teks dari EditText yang sudah di-trim.
 */
val EditText.trimmedText: String
    get() = text?.toString()?.trim().orEmpty()

/**
 * Membersihkan pesan error dari beberapa EditText sekaligus.
 */
fun clearInputErrors(vararg editTexts: EditText) {
    editTexts.forEach { it.error = null }
}

// ============================================================================
// NAVIGATION EXTENSION
// ============================================================================

/**
 * Extension function generic untuk navigasi berpindah Activity.
 *
 * @param clearTask Jika true, akan menambahkan flag CLEAR_TASK & NEW_TASK (misal: saat login berhasil).
 * @param intentBuilder Lambda opsional untuk menambahkan Intent Extras.
 */
inline fun <reified T : Activity> Context.startActivity(
    clearTask: Boolean = false,
    intentBuilder: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java).apply {
        if (clearTask) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intentBuilder()
    }
    startActivity(intent)
}