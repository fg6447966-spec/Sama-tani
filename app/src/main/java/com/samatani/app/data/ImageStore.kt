package com.samatani.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Menyimpan foto produk ke penyimpanan internal aplikasi.
 * Foto dari galeri diperkecil (maks 1600 px) dan diputar sesuai EXIF agar hemat ruang.
 */
object ImageStore {
    private const val TAG = "ImageStore"
    private const val MAX_IMPORT_DIMENSION = 1600

    private val cache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    /** Dipanggil dari background thread. @return path file, atau null bila gagal. */
    fun importImage(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_IMPORT_DIMENSION) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return null

            val rotation = resolver.openInputStream(uri)?.use { readRotation(it) } ?: 0
            val bitmap = if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                if (rotated !== decoded) decoded.recycle()
                rotated
            } else {
                decoded
            }

            val dir = File(context.filesDir, "product_images").apply { mkdirs() }
            val file = File(dir, "produk_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) }
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "Gagal mengimpor foto", e)
            null
        }
    }

    private fun readRotation(input: InputStream): Int = try {
        when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: Exception) {
        0
    }

    /** Dipanggil dari background thread. Hasil di-cache di memori. */
    fun loadThumbnail(path: String, maxDimension: Int = 720): Bitmap? {
        cache.get(path)?.let { return it }
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDimension) sample *= 2
            val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
                ?: return null
            cache.put(path, bitmap)
            bitmap
        } catch (e: Exception) {
            Log.w(TAG, "Gagal memuat foto", e)
            null
        }
    }

    fun deleteImage(path: String) {
        cache.remove(path)
        try {
            File(path).delete()
        } catch (e: Exception) {
            Log.w(TAG, "Gagal menghapus foto", e)
        }
    }
}
