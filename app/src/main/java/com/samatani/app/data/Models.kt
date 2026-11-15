package com.samatani.app.data

/** Peran pengguna di Sama Tani. Satu akun bisa berganti peran kapan saja. */
enum class Role(val label: String) {
    PETANI("Petani / Produsen"),
    PEMBELI("Pembeli / Distributor"),
}

enum class Category(val label: String, val emoji: String) {
    SAYURAN("Sayuran", "🥦"),
    BUAH("Buah", "🍌"),
    CABAI_BAWANG("Cabai & Bawang", "🌶️"),
    PADI_BIJI("Beras & Biji-bijian", "🌾"),
    UMBI("Umbi-umbian", "🥔"),
    PERKEBUNAN("Kopi & Perkebunan", "☕"),
    PETERNAKAN("Peternakan", "🥚"),
    PERIKANAN("Perikanan", "🐟"),
    LAINNYA("Lainnya", "📦"),
}

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val location: String,
    val role: Role,
    val businessName: String = "",
    /** Akun contoh yang otomatis membalas pesanan/tawaran (simulasi mitra). */
    val isBot: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis(),
) {
    val displayName: String get() = businessName.ifBlank { name }

    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }

    /** Akun demo bawaan aplikasi; nomor teleponnya tidak aktif. */
    val isDemo: Boolean get() = id.startsWith("demo-") || id.startsWith("bot-")
}

data class Product(
    val id: String,
    val sellerId: String,
    val name: String,
    val category: Category,
    /** Harga per satuan dalam Rupiah. */
    val price: Long,
    val unit: String,
    val stock: Int,
    val minOrder: Int,
    val grade: String,
    val harvestInfo: String,
    val location: String,
    val description: String,
    val imagePath: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

data class CartItem(
    val productId: String,
    val quantity: Int,
)

enum class OrderStatus(val label: String) {
    MENUNGGU("Menunggu Konfirmasi"),
    DIPROSES("Diproses"),
    DIKIRIM("Dikirim"),
    SELESAI("Selesai"),
    DITOLAK("Ditolak"),
    DIBATALKAN("Dibatalkan");

    val isActive: Boolean get() = this == MENUNGGU || this == DIPROSES || this == DIKIRIM
}

data class OrderItem(
    val productId: String,
    val name: String,
    val emoji: String,
    val unit: String,
    val quantity: Int,
    val price: Long,
) {
    val subtotal: Long get() = price * quantity
}

data class Order(
    val id: String,
    val code: String,
    val buyerId: String,
    val sellerId: String,
    val items: List<OrderItem>,
    val shippingMethod: String,
    val paymentMethod: String,
    val address: String,
    val note: String,
    val status: OrderStatus,
    val createdAt: Long,
    val updatedAt: Long,
    /** Terisi bila pesanan berasal dari nego harga atau permintaan pembeli. */
    val source: String = "",
) {
    val total: Long get() = items.sumOf { it.subtotal }
}

enum class OfferStatus(val label: String) {
    MENUNGGU("Menunggu Petani"),
    DITAWAR_BALIK("Ditawar Balik"),
    DITERIMA("Disepakati"),
    DITOLAK("Ditolak"),
}

/** Nego harga dari pembeli untuk satu produk. */
data class Offer(
    val id: String,
    val productId: String,
    val productName: String,
    val emoji: String,
    val unit: String,
    val listPrice: Long,
    val buyerId: String,
    val sellerId: String,
    val quantity: Int,
    val offeredPrice: Long,
    val counterPrice: Long? = null,
    val finalPrice: Long? = null,
    val note: String = "",
    val status: OfferStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val orderId: String? = null,
)

enum class RequestStatus(val label: String) {
    TERBUKA("Terbuka"),
    DITUTUP("Ditutup"),
}

/** Penawaran petani untuk sebuah permintaan pembeli. */
data class RequestResponse(
    val id: String,
    val sellerId: String,
    val price: Long,
    val quantity: Int,
    val note: String,
    val createdAt: Long,
)

/** Kebutuhan yang dipasang pembeli/distributor, misalnya "butuh 1 ton bawang merah tiap minggu". */
data class BuyRequest(
    val id: String,
    val buyerId: String,
    val commodity: String,
    val category: Category,
    val quantity: Int,
    val unit: String,
    val targetPrice: Long,
    val location: String,
    val neededBy: String,
    val frequency: String,
    val note: String,
    val status: RequestStatus,
    val createdAt: Long,
    val responses: List<RequestResponse> = emptyList(),
    val chosenResponseId: String? = null,
    val orderId: String? = null,
)

data class MarketPrice(
    val commodity: String,
    val emoji: String,
    val unit: String,
    val price: Long,
    val previousPrice: Long,
    val region: String,
)

/** Seluruh data aplikasi. Bersifat immutable; setiap perubahan menghasilkan salinan baru. */
data class AppData(
    val users: List<User> = emptyList(),
    val currentUserId: String? = null,
    val products: List<Product> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val orders: List<Order> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val requests: List<BuyRequest> = emptyList(),
) {
    val currentUser: User? get() = users.firstOrNull { it.id == currentUserId }

    fun user(id: String?): User? = users.firstOrNull { it.id == id }
    fun product(id: String?): Product? = products.firstOrNull { it.id == id }
    fun order(id: String?): Order? = orders.firstOrNull { it.id == id }
    fun offer(id: String?): Offer? = offers.firstOrNull { it.id == id }
    fun request(id: String?): BuyRequest? = requests.firstOrNull { it.id == id }
}

object Options {
    val units = listOf("kg", "kuintal", "ton", "ikat", "buah", "butir", "karung", "liter", "ekor", "tandan")
    val grades = listOf("Super", "A", "B", "C")
    val shippingMethods = listOf("Dikirim oleh petani", "Ambil sendiri di lokasi", "Ekspedisi / kurir")
    val paymentMethods = listOf("Transfer bank", "Bayar di tempat (COD)", "Tempo sesuai kesepakatan")
    val frequencies = listOf("Sekali beli", "Mingguan", "Bulanan")
}
