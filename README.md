# 🌾 Sama Tani

Aplikasi Android **dan website** yang mempertemukan **petani / produsen** dengan **pembeli** seperti distributor, toko, rumah makan, dan hotel. Petani menjual hasil panen langsung, pembeli mendapat pasokan dari sumbernya, dan keduanya bisa nego harga dalam satu aplikasi.

> Versi awal (MVP). Aplikasi Android menyimpan data di HP; versi web bisa memakai pasar bersama yang tersinkron (lihat [Versi web](#versi-web)).

## Fitur

**Untuk petani / produsen**
- Beranda ringkas: produk aktif, pesanan baru, tawaran nego, barang yang perlu dikirim, total penjualan
- Kelola produk: foto dari galeri, kategori, satuan (kg, ton, ikat, butir, …), harga, stok, minimal beli, grade, waktu panen
- Terima / tolak pesanan, tandai dikirim
- Terima, tolak, atau **tawar balik** harga dari pembeli
- Papan **Permintaan Pembeli**: lihat kebutuhan distributor lalu kirim penawaran

**Untuk pembeli / distributor**
- Pasar: cari produk, filter kategori, urutkan harga
- Detail produk + hubungi petani via WhatsApp / telepon
- Keranjang & checkout (pesanan otomatis dipisah per petani)
- **Nego harga** sebelum membeli
- **Pasang permintaan** rutin (mis. "1 ton bawang merah tiap minggu"), pilih penawaran petani terbaik

**Umum**
- Lacak status pesanan: Menunggu → Diproses → Dikirim → Selesai
- Info harga pasar (data contoh) + tautan ke Panel Harga Badan Pangan Nasional
- Satu akun bisa berganti mode Petani ↔ Pembeli
- Tema terang & gelap

## Coba cepat dengan akun demo

Di layar awal, ketuk **Demo Petani** atau **Demo Pembeli**. Data contoh sudah berisi produk, pesanan, nego, dan permintaan. Mitra contoh (akun simulasi) akan **membalas otomatis** dalam beberapa detik, misalnya menerima pesanan, mengirim barang, atau menawar balik, sehingga seluruh alur bisa dicoba di satu HP. Data bisa dikembalikan lewat **Akun → Reset data demo**.

## Versi web

**Buka langsung: https://fg6447966-spec.github.io/Sama-tani/** — bisa dibuka siapa saja di laptop atau HP, tanpa akun.

Folder [`web/`](web/) berisi Sama Tani versi website: **satu file** `web/index.html` (HTML + CSS + JavaScript, tanpa library). Setiap perubahan di `web/` otomatis diterbitkan ke GitHub Pages oleh `.github/workflows/pages.yml`.

Fitur sama dengan aplikasi Android (pasar, keranjang & checkout, nego harga, permintaan rutin, status pesanan, harga pasar, ganti mode Petani ↔ Pembeli, tema terang/gelap), ditambah notifikasi langsung di halaman.

Dua mode penyimpanan:

| Mode | Kapan | Data |
| --- | --- | --- |
| **Pasar bersama** | Dibuka sebagai artifact di claude.ai dalam keadaan masuk | Tersinkron langsung untuk semua orang yang dibagikan. Yang punya akses edit bisa berjualan & memesan; yang hanya melihat bisa menjelajah pasar dan menghubungi petani lewat WhatsApp |
| **Mode demo** | Situs GitHub Pages, file biasa, atau di luar claude.ai | Tersimpan di browser itu saja (`localStorage`), lengkap dengan akun Demo Petani & Demo Pembeli; pengunjung tidak saling melihat data |

Coba lokal: klik dua kali `web/index.html`, atau jalankan `python3 -m http.server` di folder `web/` lalu buka `http://localhost:8000`.

## Cara memasang APK

1. Buka tab **Releases** repo ini → rilis **latest** → unduh `SamaTani.apk` (atau tab **Actions** → build terakhir → artifact `SamaTani-apk`).
2. Buka file APK di HP Android 8.0 ke atas, izinkan *Instal aplikasi tidak dikenal* bila diminta.

APK dibuat otomatis oleh GitHub Actions (`.github/workflows/android.yml`) setiap ada push ke branch `main`.

## Build sendiri

Kebutuhan: Android Studio terbaru (sudah termasuk JDK 17+).

1. **File → Open** → pilih folder proyek ini, tunggu Gradle sync selesai.
2. Tekan **Run ▶** untuk memasang ke HP/emulator, atau **Build → Build APK(s)**.

Lewat terminal:

```bash
./gradlew assembleRelease   # hasil: app/build/outputs/apk/release/app-release.apk
./gradlew assembleDebug     # versi debug
```

## Teknologi

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- Penyimpanan lokal JSON (`org.json`) tanpa library tambahan
- Min. Android 8.0 (API 26), target API 35
- Release build memakai R8 (kecil & cepat)

## Struktur kode

```
app/src/main/java/com/samatani/app/
├── MainActivity.kt, SamaTaniApplication.kt
├── data/        Model, repository (semua logika bisnis), data contoh, penyimpanan foto
├── util/        Format Rupiah/tanggal, WhatsApp/telepon
└── ui/
    ├── auth/      Layar awal, daftar, masuk
    ├── farmer/    Beranda petani, produk saya, form produk
    ├── buyer/     Pasar, detail produk, keranjang, checkout
    ├── orders/    Daftar & detail pesanan, nego harga
    ├── requests/  Permintaan pembeli
    ├── account/   Akun, ubah profil, harga pasar
    ├── components/ Komponen UI bersama
    └── theme/     Warna & tipografi
```

Semua logika ada di `data/SamaTaniRepository.kt`. Untuk menyambungkan ke server, cukup ganti isi kelas ini; layar tidak perlu diubah.

## Langkah berikutnya

1. **Backend** (Firebase / Supabase / REST API) supaya data tersinkron antar-HP dan petani-pembeli benar-benar terhubung
2. Login dengan **OTP** nomor HP
3. **Notifikasi** push untuk pesanan & nego baru
4. Pembayaran (transfer / e-wallet / rekening bersama) dan perhitungan ongkir
5. Peta lokasi kebun & radius pengiriman, rating petani/pembeli
6. ~~Website untuk distributor~~ ✔ sudah ada di `web/` — berikutnya: sambungkan Android ke backend yang sama

## Catatan keamanan

`app/samatani-demo.keystore` adalah **kunci demo** supaya APK bisa langsung di-install dan di-update. Sebelum rilis ke Play Store, buat kunci rahasia sendiri dan jangan simpan di repo.
