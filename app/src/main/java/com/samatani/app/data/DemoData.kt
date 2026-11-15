package com.samatani.app.data

/**
 * Data contoh supaya aplikasi langsung terasa hidup saat pertama dibuka.
 * Dua akun demo (petani & pembeli) bisa dipakai masuk dari layar awal.
 * Akun "bot" adalah mitra simulasi yang otomatis membalas pesanan dan tawaran.
 */
object DemoData {
    const val DEMO_FARMER_ID = "demo-petani"
    const val DEMO_BUYER_ID = "demo-pembeli"

    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    fun create(now: Long = System.currentTimeMillis()): AppData {
        val users = listOf(
            User(DEMO_FARMER_ID, "Darto Wibowo", "0800-0000-0001", "Batu, Jawa Timur", Role.PETANI, "Kelompok Tani Sumber Rejeki", joinedAt = now - 60 * DAY),
            User(DEMO_BUYER_ID, "Rudi Hartono", "0800-0000-0002", "Surabaya, Jawa Timur", Role.PEMBELI, "CV Segar Jaya Distribusi", joinedAt = now - 45 * DAY),
            // Petani mitra (simulasi)
            User("bot-siti", "Siti Aminah", "0800-0000-0011", "Brebes, Jawa Tengah", Role.PETANI, "Tani Makmur Brebes", isBot = true, joinedAt = now - 120 * DAY),
            User("bot-made", "I Made Sudarsana", "0800-0000-0012", "Tabanan, Bali", Role.PETANI, "Subak Bedugul Lestari", isBot = true, joinedAt = now - 90 * DAY),
            User("bot-hasan", "Hasan Basri", "0800-0000-0013", "Enrekang, Sulawesi Selatan", Role.PETANI, "Gapoktan Enrekang Hijau", isBot = true, joinedAt = now - 80 * DAY),
            User("bot-andi", "Andi Baso", "0800-0000-0014", "Sidenreng Rappang, Sulawesi Selatan", Role.PETANI, "Lumbung Padi Sidrap", isBot = true, joinedAt = now - 70 * DAY),
            User("bot-yusuf", "Yusuf Palimbong", "0800-0000-0015", "Toraja Utara, Sulawesi Selatan", Role.PETANI, "Koperasi Kopi Toraja", isBot = true, joinedAt = now - 50 * DAY),
            // Pembeli mitra (simulasi)
            User("bot-resto", "Daeng Rahman", "0800-0000-0021", "Makassar, Sulawesi Selatan", Role.PEMBELI, "RM Daeng Rahman", isBot = true, joinedAt = now - 100 * DAY),
            User("bot-toko", "Lina Kusuma", "0800-0000-0022", "Denpasar, Bali", Role.PEMBELI, "Toko Sayur Segar Lina", isBot = true, joinedAt = now - 75 * DAY),
            User("bot-hotel", "Budi Santoso", "0800-0000-0023", "Malang, Jawa Timur", Role.PEMBELI, "Hotel Nusantara Catering", isBot = true, joinedAt = now - 40 * DAY),
        )

        val products = listOf(
            // Produk akun Demo Petani
            Product("p-apel", DEMO_FARMER_ID, "Apel Manalagi Batu", Category.BUAH, 18_000, "kg", 800, 20, "A", "Panen 2 hari lalu", "Batu, Jawa Timur",
                "Apel manalagi manis dan renyah, dipetik langsung dari kebun di lereng Gunung Panderman. Sudah disortir ukuran sedang-besar.", createdAt = now - 2 * DAY),
            Product("p-kentang", DEMO_FARMER_ID, "Kentang Granola", Category.UMBI, 12_500, "kg", 1_500, 50, "A", "Siap panen minggu depan", "Batu, Jawa Timur",
                "Kentang granola dataran tinggi, kulit mulus, cocok untuk restoran dan kentang goreng.", createdAt = now - 5 * DAY),
            Product("p-wortel", DEMO_FARMER_ID, "Wortel Lokal", Category.SAYURAN, 8_000, "kg", 600, 25, "B", "Baru panen", "Batu, Jawa Timur",
                "Wortel segar, sudah dicuci. Grade B (ukuran campur).", createdAt = now - 1 * DAY),
            // Produk petani mitra
            Product("p-bawang-brebes", "bot-siti", "Bawang Merah Brebes", Category.CABAI_BAWANG, 32_000, "kg", 2_000, 50, "Super", "Panen 3 hari lalu, sudah dijemur", "Brebes, Jawa Tengah",
                "Bawang merah asli Brebes, umbi besar dan kering. Bisa kirim rutin tiap minggu.", createdAt = now - 3 * DAY),
            Product("p-cabai", "bot-siti", "Cabai Merah Keriting", Category.CABAI_BAWANG, 38_000, "kg", 450, 10, "A", "Petik pagi ini", "Brebes, Jawa Tengah",
                "Cabai merah keriting segar, pedas, tangkai utuh.", createdAt = now - 6 * HOUR),
            Product("p-kubis", "bot-made", "Kubis Bedugul", Category.SAYURAN, 6_000, "kg", 1_200, 50, "A", "Panen kemarin", "Tabanan, Bali",
                "Kubis padat dari dataran tinggi Bedugul. Pengiriman ke Denpasar setiap hari.", createdAt = now - 1 * DAY),
            Product("p-stroberi", "bot-made", "Stroberi Bedugul", Category.BUAH, 45_000, "kg", 80, 2, "Super", "Petik pagi ini", "Tabanan, Bali",
                "Stroberi manis ukuran besar, dikemas dalam boks 250 gram.", createdAt = now - 8 * HOUR),
            Product("p-bawang-enrekang", "bot-hasan", "Bawang Merah Enrekang", Category.CABAI_BAWANG, 30_000, "kg", 1_800, 50, "A", "Panen 4 hari lalu", "Enrekang, Sulawesi Selatan",
                "Bawang merah varietas Bima dari Enrekang, aroma kuat, tahan simpan.", createdAt = now - 4 * DAY),
            Product("p-tomat", "bot-hasan", "Tomat Sayur", Category.SAYURAN, 9_000, "kg", 700, 20, "B", "Panen kemarin", "Enrekang, Sulawesi Selatan",
                "Tomat sayur merah merata, cocok untuk pasar dan rumah makan.", createdAt = now - 1 * DAY),
            Product("p-telur", "bot-hasan", "Telur Ayam Kampung", Category.PETERNAKAN, 2_500, "butir", 3_000, 30, "A", "Diambil setiap pagi", "Enrekang, Sulawesi Selatan",
                "Telur ayam kampung dari peternakan rakyat, dikemas tray isi 30.", createdAt = now - 2 * DAY),
            Product("p-beras", "bot-andi", "Beras Premium Sidrap", Category.PADI_BIJI, 13_500, "kg", 10_000, 100, "Super", "Giling minggu ini", "Sidenreng Rappang, Sulawesi Selatan",
                "Beras premium pulen dari lumbung padi Sulawesi Selatan. Kemasan karung 25 kg dan 50 kg.", createdAt = now - 3 * DAY),
            Product("p-gabah", "bot-andi", "Gabah Kering Panen", Category.PADI_BIJI, 6_500, "kg", 20_000, 1_000, "A", "Panen raya", "Sidenreng Rappang, Sulawesi Selatan",
                "Gabah kering panen untuk penggilingan. Kadar air sekitar 20%.", createdAt = now - 2 * DAY),
            Product("p-bandeng", "bot-andi", "Ikan Bandeng Segar", Category.PERIKANAN, 32_000, "kg", 300, 10, "A", "Panen tambak hari ini", "Pinrang, Sulawesi Selatan",
                "Bandeng tambak ukuran 3-4 ekor per kg, dikirim dengan es.", createdAt = now - 10 * HOUR),
            Product("p-kopi", "bot-yusuf", "Kopi Arabika Toraja (green bean)", Category.PERKEBUNAN, 120_000, "kg", 500, 10, "A", "Panen musim ini", "Toraja Utara, Sulawesi Selatan",
                "Green bean arabika Toraja proses full wash, ketinggian 1.500 mdpl.", createdAt = now - 7 * DAY),
        )

        val orders = listOf(
            // Masuk ke Demo Petani
            Order("o-1", "ST-DEMO-001", "bot-resto", DEMO_FARMER_ID,
                listOf(OrderItem("p-kentang", "Kentang Granola", Category.UMBI.emoji, "kg", 50, 12_500)),
                "Ekspedisi / kurir", "Transfer bank", "Jl. Somba Opu No. 12, Makassar", "Mohon dikemas karung 25 kg.",
                OrderStatus.MENUNGGU, now - 30 * MINUTE, now - 30 * MINUTE),
            Order("o-2", "ST-DEMO-002", "bot-hotel", DEMO_FARMER_ID,
                listOf(OrderItem("p-apel", "Apel Manalagi Batu", Category.BUAH.emoji, "kg", 40, 18_000)),
                "Dikirim oleh petani", "Tempo sesuai kesepakatan", "Hotel Nusantara, Jl. Ijen, Malang", "",
                OrderStatus.DIPROSES, now - 1 * DAY, now - 20 * HOUR),
            Order("o-3", "ST-DEMO-003", DEMO_BUYER_ID, DEMO_FARMER_ID,
                listOf(OrderItem("p-wortel", "Wortel Lokal", Category.SAYURAN.emoji, "kg", 100, 8_000)),
                "Dikirim oleh petani", "Transfer bank", "Gudang CV Segar Jaya, Surabaya", "",
                OrderStatus.SELESAI, now - 5 * DAY, now - 4 * DAY),
            // Dibuat Demo Pembeli
            Order("o-4", "ST-DEMO-004", DEMO_BUYER_ID, "bot-siti",
                listOf(OrderItem("p-bawang-brebes", "Bawang Merah Brebes", Category.CABAI_BAWANG.emoji, "kg", 100, 32_000)),
                "Ekspedisi / kurir", "Transfer bank", "Gudang CV Segar Jaya, Surabaya", "",
                OrderStatus.DIKIRIM, now - 2 * DAY, now - 1 * DAY),
            Order("o-5", "ST-DEMO-005", DEMO_BUYER_ID, "bot-made",
                listOf(OrderItem("p-kubis", "Kubis Bedugul", Category.SAYURAN.emoji, "kg", 200, 6_000)),
                "Ekspedisi / kurir", "Bayar di tempat (COD)", "Gudang CV Segar Jaya, Surabaya", "",
                OrderStatus.SELESAI, now - 7 * DAY, now - 6 * DAY),
        )

        val offers = listOf(
            Offer("f-1", "p-apel", "Apel Manalagi Batu", Category.BUAH.emoji, "kg", 18_000, "bot-toko", DEMO_FARMER_ID,
                30, 15_500, note = "Kalau cocok, kami ambil rutin tiap minggu.", status = OfferStatus.MENUNGGU,
                createdAt = now - 2 * HOUR, updatedAt = now - 2 * HOUR),
            Offer("f-2", "p-bawang-enrekang", "Bawang Merah Enrekang", Category.CABAI_BAWANG.emoji, "kg", 30_000, DEMO_BUYER_ID, "bot-hasan",
                300, 27_000, counterPrice = 28_500, note = "Untuk stok gudang Surabaya.", status = OfferStatus.DITAWAR_BALIK,
                createdAt = now - 3 * HOUR, updatedAt = now - 1 * HOUR),
        )

        val requests = listOf(
            BuyRequest("r-1", "bot-resto", "Cabai Rawit Merah", Category.CABAI_BAWANG, 50, "kg", 40_000, "Makassar, Sulawesi Selatan",
                "Setiap Senin", "Mingguan", "Cabai segar, tidak busuk, dikirim pagi hari.", RequestStatus.TERBUKA, now - 5 * HOUR,
                responses = listOf(RequestResponse("rr-1", "bot-hasan", 41_000, 50, "Bisa kirim tiap Senin pagi dari Enrekang.", now - 3 * HOUR))),
            BuyRequest("r-2", "bot-hotel", "Kentang Granola", Category.UMBI, 300, "kg", 12_000, "Malang, Jawa Timur",
                "Akhir bulan ini", "Bulanan", "Ukuran sedang-besar untuk kentang goreng.", RequestStatus.TERBUKA, now - 1 * DAY),
            BuyRequest("r-3", "bot-toko", "Sayuran Daun Campur", Category.SAYURAN, 100, "ikat", 3_000, "Denpasar, Bali",
                "Besok pagi", "Sekali beli", "Sawi, bayam, dan kangkung. Daun segar tidak layu.", RequestStatus.TERBUKA, now - 4 * HOUR),
            BuyRequest("r-4", DEMO_BUYER_ID, "Bawang Merah", Category.CABAI_BAWANG, 1_000, "kg", 29_000, "Surabaya, Jawa Timur",
                "Minggu depan", "Mingguan", "Umbi kering, ukuran sedang. Pembayaran transfer setelah barang diterima.", RequestStatus.TERBUKA, now - 1 * DAY,
                responses = listOf(
                    RequestResponse("rr-2", "bot-siti", 31_000, 1_000, "Bisa kirim rutin tiap minggu, sudah termasuk sortir.", now - 20 * HOUR),
                    RequestResponse("rr-3", "bot-hasan", 29_500, 800, "Stok 800 kg siap, sisanya minggu berikutnya.", now - 10 * HOUR),
                )),
        )

        return AppData(
            users = users,
            currentUserId = null,
            products = products,
            cart = emptyList(),
            orders = orders,
            offers = offers,
            requests = requests,
        )
    }

    /**
     * Contoh harga rata-rata untuk ilustrasi tampilan. BUKAN harga resmi;
     * untuk harga terkini gunakan Panel Harga Badan Pangan Nasional.
     */
    val marketPrices: List<MarketPrice> = listOf(
        MarketPrice("Beras Medium", "🍚", "kg", 13_500, 13_450, "Rata-rata nasional"),
        MarketPrice("Beras Premium", "🌾", "kg", 15_350, 15_400, "Rata-rata nasional"),
        MarketPrice("Gabah Kering Panen", "🌾", "kg", 6_700, 6_650, "Tingkat petani"),
        MarketPrice("Jagung Pipilan Kering", "🌽", "kg", 6_800, 6_750, "Tingkat petani"),
        MarketPrice("Cabai Merah Keriting", "🌶️", "kg", 41_500, 39_000, "Rata-rata nasional"),
        MarketPrice("Cabai Rawit Merah", "🌶️", "kg", 45_300, 48_000, "Rata-rata nasional"),
        MarketPrice("Bawang Merah", "🧅", "kg", 42_150, 41_500, "Rata-rata nasional"),
        MarketPrice("Bawang Putih Bonggol", "🧄", "kg", 38_000, 38_500, "Rata-rata nasional"),
        MarketPrice("Kentang", "🥔", "kg", 14_000, 13_500, "Rata-rata nasional"),
        MarketPrice("Tomat", "🍅", "kg", 11_000, 12_500, "Rata-rata nasional"),
        MarketPrice("Kubis", "🥬", "kg", 7_000, 7_000, "Rata-rata nasional"),
        MarketPrice("Wortel", "🥕", "kg", 12_000, 11_000, "Rata-rata nasional"),
        MarketPrice("Telur Ayam Ras", "🥚", "kg", 30_350, 30_000, "Rata-rata nasional"),
        MarketPrice("Daging Ayam Ras", "🍗", "kg", 39_050, 39_500, "Rata-rata nasional"),
        MarketPrice("Kopi Arabika (green bean)", "☕", "kg", 115_000, 112_000, "Tingkat petani"),
    )
}
