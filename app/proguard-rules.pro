# Aturan R8 tambahan untuk Sama Tani.
# Aplikasi tidak memakai refleksi (data disimpan dengan org.json secara manual),
# jadi aturan bawaan proguard-android-optimize.txt sudah cukup.

# Pertahankan nomor baris agar laporan crash mudah dibaca.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
