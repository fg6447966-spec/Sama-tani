package com.samatani.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

val IndonesianLocale: Locale = Locale.forLanguageTag("id-ID")

/** 15000 -> "Rp15.000" */
fun formatRupiah(value: Long): String =
    "Rp" + NumberFormat.getNumberInstance(IndonesianLocale).format(value)

/** 1250000 -> "Rp1,3 jt" (untuk ringkasan yang sempit). */
fun formatRupiahShort(value: Long): String = when {
    value >= 1_000_000_000L -> "Rp" + oneDecimal(value / 1_000_000_000.0) + " M"
    value >= 1_000_000L -> "Rp" + oneDecimal(value / 1_000_000.0) + " jt"
    value >= 10_000L -> "Rp" + oneDecimal(value / 1_000.0) + " rb"
    else -> formatRupiah(value)
}

private fun oneDecimal(value: Double): String {
    val format = NumberFormat.getNumberInstance(IndonesianLocale)
    format.maximumFractionDigits = 1
    format.minimumFractionDigits = 0
    return format.format(value)
}

fun formatQty(value: Int): String = NumberFormat.getNumberInstance(IndonesianLocale).format(value)

fun formatPercent(value: Double): String {
    val format = NumberFormat.getNumberInstance(IndonesianLocale)
    format.maximumFractionDigits = 1
    format.minimumFractionDigits = 1
    return format.format(abs(value)) + "%"
}

fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", IndonesianLocale).format(Date(timestamp))

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy", IndonesianLocale).format(Date(timestamp))

fun timeAgo(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - timestamp).coerceAtLeast(0L) / 60_000L
    return when {
        minutes < 1 -> "baru saja"
        minutes < 60 -> "$minutes menit lalu"
        minutes < 60 * 24 -> "${minutes / 60} jam lalu"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)} hari lalu"
        else -> formatDate(timestamp)
    }
}

/** Menyamakan format nomor HP Indonesia: 0812..., +62812..., 812... -> 62812... */
fun normalizePhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
        digits.startsWith("62") -> digits
        digits.startsWith("0") -> "62" + digits.drop(1)
        digits.startsWith("8") -> "62$digits"
        else -> digits
    }
}

/** Membulatkan harga: kelipatan Rp500 untuk harga >= Rp10.000, selain itu kelipatan Rp50. */
fun roundPrice(value: Long): Long {
    val step = if (value >= 10_000L) 500L else 50L
    return ((value + step / 2) / step) * step
}

fun digitsOnly(text: String, maxLength: Int = 12): String = text.filter { it.isDigit() }.take(maxLength)
