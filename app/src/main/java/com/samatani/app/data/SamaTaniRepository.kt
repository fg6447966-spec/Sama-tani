package com.samatani.app.data

import android.content.Context
import android.util.Log
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.normalizePhone
import com.samatani.app.util.roundPrice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Sumber data tunggal aplikasi.
 *
 * Versi ini menyimpan semua data di perangkat (file JSON) sehingga aplikasi bisa
 * dipakai tanpa server. Seluruh logika bisnis (pesanan, nego, permintaan) ada di sini,
 * jadi saat nanti disambungkan ke backend (Firebase/Supabase/REST) cukup kelas ini
 * yang diganti, layar-layar tidak perlu diubah.
 *
 * Semua fungsi publik dipanggil dari main thread.
 */
class SamaTaniRepository(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val dataFile = File(appContext.filesDir, "samatani_data.json")
    private val writeLock = Mutex()
    private val botJobs = mutableMapOf<String, Job>()

    private val _data = MutableStateFlow(loadOrSeed())
    val data: StateFlow<AppData> = _data.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)

    /** Pesan singkat (snackbar) untuk pengguna, misalnya saat mitra membalas pesanan. */
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    /** Snapshot data saat ini (untuk nilai awal formulir, bukan untuk observasi). */
    val current: AppData get() = _data.value

    init {
        if (!dataFile.exists()) persist()
        scheduleBotActions()
    }

    // ------------------------------------------------------------------
    // Penyimpanan
    // ------------------------------------------------------------------

    private fun loadOrSeed(): AppData = try {
        if (dataFile.exists()) JsonCodec.decode(dataFile.readText()) else DemoData.create()
    } catch (e: Exception) {
        Log.w(TAG, "Data rusak, memuat ulang data contoh", e)
        DemoData.create()
    }

    private fun persist() {
        scope.launch(Dispatchers.IO) {
            writeLock.withLock {
                try {
                    // Selalu tulis versi terbaru agar urutan penyimpanan tidak tertukar.
                    val json = JsonCodec.encode(_data.value)
                    val tmp = File(dataFile.parentFile, dataFile.name + ".tmp")
                    tmp.writeText(json)
                    if (!tmp.renameTo(dataFile)) {
                        dataFile.writeText(json)
                        tmp.delete()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal menyimpan data", e)
                }
            }
        }
    }

    private inline fun mutate(transform: (AppData) -> AppData) {
        val before = _data.value
        val after = transform(before)
        if (after == before) return
        _data.value = after
        persist()
        scheduleBotActions()
    }

    fun showMessage(message: String) {
        _messages.tryEmit(message)
    }

    fun newId(): String = UUID.randomUUID().toString()

    private fun newOrderCode(existing: List<Order>): String {
        val date = SimpleDateFormat("yyMMdd", Locale.US).format(Date())
        val prefix = "ST-$date-"
        val next = existing.count { it.code.startsWith(prefix) } + 1
        return prefix + next.toString().padStart(3, '0')
    }

    // ------------------------------------------------------------------
    // Akun
    // ------------------------------------------------------------------

    fun findUserByPhone(phone: String): User? {
        val target = normalizePhone(phone)
        if (target.isEmpty()) return null
        return current.users.firstOrNull { normalizePhone(it.phone) == target }
    }

    /** @return pesan error, atau null bila berhasil. */
    fun register(name: String, businessName: String, phone: String, location: String, role: Role): String? {
        if (findUserByPhone(phone) != null) return "Nomor HP sudah terdaftar. Silakan masuk."
        val user = User(
            id = newId(),
            name = name.trim(),
            phone = phone.trim(),
            location = location.trim(),
            role = role,
            businessName = businessName.trim(),
        )
        mutate { it.copy(users = it.users + user, currentUserId = user.id, cart = emptyList()) }
        return null
    }

    fun login(phone: String): Boolean {
        val user = findUserByPhone(phone) ?: return false
        mutate { it.copy(currentUserId = user.id, cart = emptyList()) }
        return true
    }

    fun loginDemo(role: Role) {
        val id = if (role == Role.PETANI) DemoData.DEMO_FARMER_ID else DemoData.DEMO_BUYER_ID
        mutate { d ->
            val users = if (d.user(id) != null) {
                d.users.map { if (it.id == id) it.copy(role = role) else it }
            } else {
                // Akun demo hilang (mis. data lama) -> tambahkan lagi dari data contoh.
                d.users + DemoData.create().users.filter { seed -> d.users.none { it.id == seed.id } }
            }
            d.copy(users = users, currentUserId = id, cart = emptyList())
        }
    }

    fun logout() = mutate { it.copy(currentUserId = null, cart = emptyList()) }

    fun switchRole() = mutate { d ->
        val me = d.currentUser ?: return@mutate d
        val newRole = if (me.role == Role.PETANI) Role.PEMBELI else Role.PETANI
        d.copy(
            users = d.users.map { if (it.id == me.id) it.copy(role = newRole) else it },
            cart = emptyList(),
        )
    }

    /** @return pesan error, atau null bila berhasil. */
    fun updateProfile(name: String, businessName: String, phone: String, location: String): String? {
        val me = current.currentUser ?: return "Anda belum masuk."
        val owner = findUserByPhone(phone)
        if (owner != null && owner.id != me.id) return "Nomor HP sudah dipakai akun lain."
        mutate { d ->
            d.copy(users = d.users.map {
                if (it.id == me.id) {
                    it.copy(name = name.trim(), businessName = businessName.trim(), phone = phone.trim(), location = location.trim())
                } else {
                    it
                }
            })
        }
        return null
    }

    /** Menghapus semua data di perangkat dan memuat ulang data contoh. */
    fun resetAllData() {
        botJobs.values.forEach { it.cancel() }
        botJobs.clear()
        val oldImages = current.products.mapNotNull { it.imagePath }
        scope.launch(Dispatchers.IO) { oldImages.forEach { ImageStore.deleteImage(it) } }
        mutate { DemoData.create() }
    }

    // ------------------------------------------------------------------
    // Produk
    // ------------------------------------------------------------------

    fun saveProduct(product: Product) = mutate { d ->
        val exists = d.products.any { it.id == product.id }
        d.copy(products = if (exists) d.products.map { if (it.id == product.id) product else it } else listOf(product) + d.products)
    }

    fun deleteProduct(productId: String) {
        val path = current.product(productId)?.imagePath
        mutate { d ->
            d.copy(
                products = d.products.filterNot { it.id == productId },
                cart = d.cart.filterNot { it.productId == productId },
            )
        }
        if (path != null) scope.launch(Dispatchers.IO) { ImageStore.deleteImage(path) }
    }

    fun setProductActive(productId: String, active: Boolean) = mutate { d ->
        d.copy(products = d.products.map { if (it.id == productId) it.copy(isActive = active) else it })
    }

    // ------------------------------------------------------------------
    // Keranjang & checkout
    // ------------------------------------------------------------------

    fun addToCart(productId: String, quantity: Int) = mutate { d ->
        val product = d.product(productId) ?: return@mutate d
        val existing = d.cart.firstOrNull { it.productId == productId }
        val newQty = ((existing?.quantity ?: 0) + quantity).coerceAtMost(product.stock).coerceAtLeast(1)
        val cart = if (existing != null) {
            d.cart.map { if (it.productId == productId) it.copy(quantity = newQty) else it }
        } else {
            d.cart + CartItem(productId, newQty)
        }
        d.copy(cart = cart)
    }

    fun setCartQuantity(productId: String, quantity: Int) = mutate { d ->
        val cart = if (quantity <= 0) {
            d.cart.filterNot { it.productId == productId }
        } else {
            d.cart.map { if (it.productId == productId) it.copy(quantity = quantity) else it }
        }
        d.copy(cart = cart)
    }

    fun removeFromCart(productId: String) = setCartQuantity(productId, 0)

    /**
     * Membuat pesanan dari isi keranjang. Pesanan dipisah per petani.
     * @return jumlah pesanan yang dibuat.
     */
    fun checkout(address: String, shippingMethod: String, paymentMethod: String, note: String): Int {
        val d = current
        val me = d.currentUser ?: return 0
        val now = System.currentTimeMillis()
        val lines = d.cart.mapNotNull { item ->
            val product = d.product(item.productId) ?: return@mapNotNull null
            if (!product.isActive || product.stock <= 0) return@mapNotNull null
            product to item.quantity.coerceAtMost(product.stock)
        }
        if (lines.isEmpty()) return 0

        var allOrders = d.orders
        val created = lines.groupBy { it.first.sellerId }.map { (sellerId, group) ->
            val order = Order(
                id = newId(),
                code = newOrderCode(allOrders),
                buyerId = me.id,
                sellerId = sellerId,
                items = group.map { (p, qty) -> OrderItem(p.id, p.name, p.category.emoji, p.unit, qty, p.price) },
                shippingMethod = shippingMethod,
                paymentMethod = paymentMethod,
                address = address.trim(),
                note = note.trim(),
                status = OrderStatus.MENUNGGU,
                createdAt = now,
                updatedAt = now,
            )
            allOrders = listOf(order) + allOrders
            order
        }
        val finalOrders = allOrders
        mutate { it.copy(orders = finalOrders, cart = emptyList()) }
        return created.size
    }

    // ------------------------------------------------------------------
    // Pesanan
    // ------------------------------------------------------------------

    fun acceptOrder(orderId: String) = transitionOrder(orderId, OrderStatus.MENUNGGU, OrderStatus.DIPROSES)
    fun rejectOrder(orderId: String) = transitionOrder(orderId, OrderStatus.MENUNGGU, OrderStatus.DITOLAK)
    fun shipOrder(orderId: String) = transitionOrder(orderId, OrderStatus.DIPROSES, OrderStatus.DIKIRIM)
    fun completeOrder(orderId: String) = transitionOrder(orderId, OrderStatus.DIKIRIM, OrderStatus.SELESAI)
    fun cancelOrder(orderId: String) = transitionOrder(orderId, OrderStatus.MENUNGGU, OrderStatus.DIBATALKAN)

    private fun transitionOrder(orderId: String, from: OrderStatus, to: OrderStatus): Boolean {
        val order = current.order(orderId) ?: return false
        if (order.status != from) return false
        mutate { applyOrderStatus(it, orderId, to) }
        return true
    }

    private fun applyOrderStatus(d: AppData, orderId: String, to: OrderStatus): AppData {
        val order = d.order(orderId) ?: return d
        val now = System.currentTimeMillis()
        var products = d.products
        // Stok berkurang saat petani menerima pesanan.
        if (order.status == OrderStatus.MENUNGGU && to == OrderStatus.DIPROSES) {
            products = products.map { p ->
                val qty = order.items.filter { it.productId == p.id }.sumOf { it.quantity }
                if (qty > 0) p.copy(stock = (p.stock - qty).coerceAtLeast(0)) else p
            }
        }
        return d.copy(
            products = products,
            orders = d.orders.map { if (it.id == orderId) it.copy(status = to, updatedAt = now) else it },
        )
    }

    // ------------------------------------------------------------------
    // Nego harga
    // ------------------------------------------------------------------

    fun makeOffer(productId: String, quantity: Int, price: Long, note: String): Boolean {
        val d = current
        val me = d.currentUser ?: return false
        val p = d.product(productId) ?: return false
        val now = System.currentTimeMillis()
        val offer = Offer(
            id = newId(),
            productId = p.id,
            productName = p.name,
            emoji = p.category.emoji,
            unit = p.unit,
            listPrice = p.price,
            buyerId = me.id,
            sellerId = p.sellerId,
            quantity = quantity,
            offeredPrice = price,
            note = note.trim(),
            status = OfferStatus.MENUNGGU,
            createdAt = now,
            updatedAt = now,
        )
        mutate { it.copy(offers = listOf(offer) + it.offers) }
        return true
    }

    fun counterOffer(offerId: String, price: Long) = mutate { d ->
        d.copy(offers = d.offers.map {
            if (it.id == offerId && it.status == OfferStatus.MENUNGGU) {
                it.copy(counterPrice = price, status = OfferStatus.DITAWAR_BALIK, updatedAt = System.currentTimeMillis())
            } else {
                it
            }
        })
    }

    fun rejectOffer(offerId: String) = mutate { d ->
        d.copy(offers = d.offers.map {
            if (it.id == offerId && (it.status == OfferStatus.MENUNGGU || it.status == OfferStatus.DITAWAR_BALIK)) {
                it.copy(status = OfferStatus.DITOLAK, updatedAt = System.currentTimeMillis())
            } else {
                it
            }
        })
    }

    /** Petani menerima tawaran awal, atau pembeli menerima tawar balik. Pesanan otomatis dibuat. */
    fun acceptOffer(offerId: String) = mutate { applyAcceptOffer(it, offerId) }

    private fun applyAcceptOffer(d: AppData, offerId: String): AppData {
        val offer = d.offer(offerId) ?: return d
        val agreedPrice = when (offer.status) {
            OfferStatus.MENUNGGU -> offer.offeredPrice
            OfferStatus.DITAWAR_BALIK -> offer.counterPrice ?: offer.offeredPrice
            else -> return d
        }
        val now = System.currentTimeMillis()
        val buyer = d.user(offer.buyerId)
        val order = Order(
            id = newId(),
            code = newOrderCode(d.orders),
            buyerId = offer.buyerId,
            sellerId = offer.sellerId,
            items = listOf(OrderItem(offer.productId, offer.productName, offer.emoji, offer.unit, offer.quantity, agreedPrice)),
            shippingMethod = "Disepakati bersama",
            paymentMethod = "Sesuai kesepakatan",
            address = buyer?.location ?: "-",
            note = offer.note,
            status = OrderStatus.DIPROSES,
            createdAt = now,
            updatedAt = now,
            source = "Hasil nego harga",
        )
        val products = d.products.map {
            if (it.id == offer.productId) it.copy(stock = (it.stock - offer.quantity).coerceAtLeast(0)) else it
        }
        return d.copy(
            products = products,
            orders = listOf(order) + d.orders,
            offers = d.offers.map {
                if (it.id == offerId) it.copy(status = OfferStatus.DITERIMA, finalPrice = agreedPrice, orderId = order.id, updatedAt = now) else it
            },
        )
    }

    // ------------------------------------------------------------------
    // Permintaan pembeli
    // ------------------------------------------------------------------

    fun postRequest(
        commodity: String,
        category: Category,
        quantity: Int,
        unit: String,
        targetPrice: Long,
        location: String,
        neededBy: String,
        frequency: String,
        note: String,
    ) {
        val me = current.currentUser ?: return
        val request = BuyRequest(
            id = newId(),
            buyerId = me.id,
            commodity = commodity.trim(),
            category = category,
            quantity = quantity,
            unit = unit,
            targetPrice = targetPrice,
            location = location.trim(),
            neededBy = neededBy.trim(),
            frequency = frequency,
            note = note.trim(),
            status = RequestStatus.TERBUKA,
            createdAt = System.currentTimeMillis(),
        )
        mutate { it.copy(requests = listOf(request) + it.requests) }
    }

    fun respondToRequest(requestId: String, price: Long, quantity: Int, note: String) {
        val me = current.currentUser ?: return
        val response = RequestResponse(newId(), me.id, price, quantity, note.trim(), System.currentTimeMillis())
        mutate { d ->
            d.copy(requests = d.requests.map { r ->
                if (r.id == requestId && r.status == RequestStatus.TERBUKA) {
                    r.copy(responses = r.responses.filterNot { it.sellerId == me.id } + response)
                } else {
                    r
                }
            })
        }
    }

    /** Pembeli memilih penawaran petani: permintaan ditutup dan pesanan dibuat. */
    fun chooseResponse(requestId: String, responseId: String) = mutate { applyChooseResponse(it, requestId, responseId) }

    private fun applyChooseResponse(d: AppData, requestId: String, responseId: String): AppData {
        val request = d.request(requestId) ?: return d
        if (request.status != RequestStatus.TERBUKA) return d
        val response = request.responses.firstOrNull { it.id == responseId } ?: return d
        val now = System.currentTimeMillis()
        val order = Order(
            id = newId(),
            code = newOrderCode(d.orders),
            buyerId = request.buyerId,
            sellerId = response.sellerId,
            items = listOf(OrderItem("", request.commodity, request.category.emoji, request.unit, response.quantity, response.price)),
            shippingMethod = "Disepakati bersama",
            paymentMethod = "Sesuai kesepakatan",
            address = request.location,
            note = response.note,
            status = OrderStatus.DIPROSES,
            createdAt = now,
            updatedAt = now,
            source = "Dari permintaan pembeli",
        )
        return d.copy(
            orders = listOf(order) + d.orders,
            requests = d.requests.map {
                if (it.id == requestId) it.copy(status = RequestStatus.DITUTUP, chosenResponseId = responseId, orderId = order.id) else it
            },
        )
    }

    fun closeRequest(requestId: String) = mutate { d ->
        d.copy(requests = d.requests.map { if (it.id == requestId) it.copy(status = RequestStatus.DITUTUP) else it })
    }

    fun deleteRequest(requestId: String) = mutate { d ->
        d.copy(requests = d.requests.filterNot { it.id == requestId })
    }

    // ------------------------------------------------------------------
    // Simulasi mitra (akun bot)
    // ------------------------------------------------------------------

    private fun scheduleBotActions() {
        val d = _data.value
        val bots = d.users.filter { it.isBot }.map { it.id }.toSet()
        if (bots.isEmpty()) return

        for (o in d.orders) {
            when {
                o.sellerId in bots && o.status == OrderStatus.MENUNGGU ->
                    schedule("order-accept-${o.id}", 4_000L) { botAcceptOrder(o.id) }
                o.sellerId in bots && o.status == OrderStatus.DIPROSES ->
                    schedule("order-ship-${o.id}", 9_000L) { botShipOrder(o.id) }
                o.buyerId in bots && o.status == OrderStatus.DIKIRIM ->
                    schedule("order-done-${o.id}", 7_000L) { botCompleteOrder(o.id) }
            }
        }
        for (f in d.offers) {
            when {
                f.sellerId in bots && f.status == OfferStatus.MENUNGGU ->
                    schedule("offer-seller-${f.id}", 5_000L) { botRespondToOffer(f.id) }
                f.buyerId in bots && f.status == OfferStatus.DITAWAR_BALIK ->
                    schedule("offer-buyer-${f.id}", 5_000L) { botAnswerCounter(f.id) }
            }
        }
        for (r in d.requests) {
            if (r.buyerId in bots && r.status == RequestStatus.TERBUKA && r.responses.any { it.sellerId !in bots }) {
                schedule("request-${r.id}", 8_000L) { botChooseResponse(r.id) }
            }
        }
    }

    private fun schedule(key: String, delayMillis: Long, action: () -> Unit) {
        if (botJobs.containsKey(key)) return
        botJobs[key] = scope.launch {
            delay(delayMillis)
            botJobs.remove(key)
            action()
        }
    }

    private fun notifyParties(vararg userIds: String, message: String) {
        val me = current.currentUserId ?: return
        if (me in userIds) showMessage(message)
    }

    private fun botAcceptOrder(orderId: String) {
        val o = current.order(orderId) ?: return
        if (o.status != OrderStatus.MENUNGGU) return
        mutate { applyOrderStatus(it, orderId, OrderStatus.DIPROSES) }
        notifyParties(o.buyerId, message = "✅ ${nameOf(o.sellerId)} menerima pesanan ${o.code}")
    }

    private fun botShipOrder(orderId: String) {
        val o = current.order(orderId) ?: return
        if (o.status != OrderStatus.DIPROSES) return
        mutate { applyOrderStatus(it, orderId, OrderStatus.DIKIRIM) }
        notifyParties(o.buyerId, message = "🚚 Pesanan ${o.code} sedang dikirim oleh ${nameOf(o.sellerId)}")
    }

    private fun botCompleteOrder(orderId: String) {
        val o = current.order(orderId) ?: return
        if (o.status != OrderStatus.DIKIRIM) return
        mutate { applyOrderStatus(it, orderId, OrderStatus.SELESAI) }
        notifyParties(o.sellerId, message = "🎉 ${nameOf(o.buyerId)} mengonfirmasi pesanan ${o.code} sudah diterima")
    }

    private fun botRespondToOffer(offerId: String) {
        val f = current.offer(offerId) ?: return
        if (f.status != OfferStatus.MENUNGGU) return
        val seller = nameOf(f.sellerId)
        // Petani simulasi setuju bila tawaran >= 92% harga awal, selain itu menawar balik.
        if (f.offeredPrice * 100 >= f.listPrice * 92) {
            mutate { applyAcceptOffer(it, offerId) }
            notifyParties(f.buyerId, message = "🤝 $seller menyetujui tawaran ${formatRupiah(f.offeredPrice)}/${f.unit}. Pesanan dibuat.")
        } else {
            val counter = roundPrice((f.offeredPrice + f.listPrice) / 2).coerceAtMost(f.listPrice)
            mutate { d ->
                d.copy(offers = d.offers.map {
                    if (it.id == offerId) it.copy(status = OfferStatus.DITAWAR_BALIK, counterPrice = counter, updatedAt = System.currentTimeMillis()) else it
                })
            }
            notifyParties(f.buyerId, message = "💬 $seller menawar balik ${formatRupiah(counter)}/${f.unit} untuk ${f.productName}")
        }
    }

    private fun botAnswerCounter(offerId: String) {
        val f = current.offer(offerId) ?: return
        if (f.status != OfferStatus.DITAWAR_BALIK) return
        val counter = f.counterPrice ?: return
        val buyer = nameOf(f.buyerId)
        // Pembeli simulasi setuju bila tawar balik paling tinggi 12% di atas tawarannya.
        if (counter * 100 <= f.offeredPrice * 112) {
            mutate { applyAcceptOffer(it, offerId) }
            notifyParties(f.sellerId, message = "🤝 $buyer setuju di harga ${formatRupiah(counter)}/${f.unit}. Pesanan dibuat.")
        } else {
            mutate { d ->
                d.copy(offers = d.offers.map {
                    if (it.id == offerId) it.copy(status = OfferStatus.DITOLAK, updatedAt = System.currentTimeMillis()) else it
                })
            }
            notifyParties(f.sellerId, message = "❌ $buyer menolak tawar balik untuk ${f.productName}")
        }
    }

    private fun botChooseResponse(requestId: String) {
        val d = current
        val r = d.request(requestId) ?: return
        if (r.status != RequestStatus.TERBUKA) return
        val bots = d.users.filter { it.isBot }.map { it.id }.toSet()
        val chosen = r.responses.filter { it.sellerId !in bots }.minByOrNull { it.price } ?: return
        mutate { applyChooseResponse(it, requestId, chosen.id) }
        notifyParties(chosen.sellerId, message = "🎉 ${nameOf(r.buyerId)} memilih penawaran Anda untuk \"${r.commodity}\". Pesanan dibuat.")
    }

    private fun nameOf(userId: String): String = current.user(userId)?.displayName ?: "Mitra"

    private companion object {
        const val TAG = "SamaTaniRepository"
    }
}
