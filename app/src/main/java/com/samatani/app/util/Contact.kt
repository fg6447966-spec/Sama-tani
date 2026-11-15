package com.samatani.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.samatani.app.data.User

private const val DEMO_NOTICE = "Ini akun contoh, nomornya tidak aktif. Fitur ini berfungsi untuk akun asli."

/** Membuka chat WhatsApp dengan pesan awal. */
fun openWhatsApp(context: Context, user: User, message: String) {
    if (user.isDemo) {
        Toast.makeText(context, DEMO_NOTICE, Toast.LENGTH_LONG).show()
        return
    }
    val uri = Uri.parse("https://wa.me/${normalizePhone(user.phone)}?text=${Uri.encode(message)}")
    startSafely(context, Intent(Intent.ACTION_VIEW, uri), "Tidak ada aplikasi untuk membuka WhatsApp.")
}

/** Membuka aplikasi telepon dengan nomor terisi (tanpa izin CALL_PHONE). */
fun dialPhone(context: Context, user: User) {
    if (user.isDemo) {
        Toast.makeText(context, DEMO_NOTICE, Toast.LENGTH_LONG).show()
        return
    }
    startSafely(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone}")), "Tidak ada aplikasi telepon.")
}

fun openUrl(context: Context, url: String) {
    startSafely(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)), "Tidak ada browser untuk membuka tautan.")
}

private fun startSafely(context: Context, intent: Intent, errorMessage: String) {
    try {
        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
