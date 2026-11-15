package com.samatani.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Mengubah [AppData] ke/dari JSON memakai org.json bawaan Android
 * (tanpa library tambahan dan tanpa refleksi, aman untuk R8).
 */
internal object JsonCodec {
    private const val SCHEMA_VERSION = 1

    fun encode(data: AppData): String {
        val root = JSONObject()
        root.put("version", SCHEMA_VERSION)
        root.put("currentUserId", data.currentUserId ?: JSONObject.NULL)
        root.put("users", data.users.toJsonArray { userToJson(it) })
        root.put("products", data.products.toJsonArray { productToJson(it) })
        root.put("cart", data.cart.toJsonArray { cartToJson(it) })
        root.put("orders", data.orders.toJsonArray { orderToJson(it) })
        root.put("offers", data.offers.toJsonArray { offerToJson(it) })
        root.put("requests", data.requests.toJsonArray { requestToJson(it) })
        return root.toString()
    }

    fun decode(text: String): AppData {
        val root = JSONObject(text)
        return AppData(
            users = root.optJSONArray("users").mapObjects { userFromJson(it) },
            currentUserId = root.stringOrNull("currentUserId"),
            products = root.optJSONArray("products").mapObjects { productFromJson(it) },
            cart = root.optJSONArray("cart").mapObjects { cartFromJson(it) },
            orders = root.optJSONArray("orders").mapObjects { orderFromJson(it) },
            offers = root.optJSONArray("offers").mapObjects { offerFromJson(it) },
            requests = root.optJSONArray("requests").mapObjects { requestFromJson(it) },
        )
    }

    // ---------- User ----------
    private fun userToJson(u: User): JSONObject {
        val o = JSONObject()
        o.put("id", u.id)
        o.put("name", u.name)
        o.put("phone", u.phone)
        o.put("location", u.location)
        o.put("role", u.role.name)
        o.put("businessName", u.businessName)
        o.put("isBot", u.isBot)
        o.put("joinedAt", u.joinedAt)
        return o
    }

    private fun userFromJson(o: JSONObject): User = User(
        id = o.getString("id"),
        name = o.optString("name"),
        phone = o.optString("phone"),
        location = o.optString("location"),
        role = enumOrDefault(o.optString("role"), Role.PEMBELI),
        businessName = o.optString("businessName"),
        isBot = o.optBoolean("isBot", false),
        joinedAt = o.optLong("joinedAt", System.currentTimeMillis()),
    )

    // ---------- Product ----------
    private fun productToJson(p: Product): JSONObject {
        val o = JSONObject()
        o.put("id", p.id)
        o.put("sellerId", p.sellerId)
        o.put("name", p.name)
        o.put("category", p.category.name)
        o.put("price", p.price)
        o.put("unit", p.unit)
        o.put("stock", p.stock)
        o.put("minOrder", p.minOrder)
        o.put("grade", p.grade)
        o.put("harvestInfo", p.harvestInfo)
        o.put("location", p.location)
        o.put("description", p.description)
        o.put("imagePath", p.imagePath ?: JSONObject.NULL)
        o.put("isActive", p.isActive)
        o.put("createdAt", p.createdAt)
        return o
    }

    private fun productFromJson(o: JSONObject): Product = Product(
        id = o.getString("id"),
        sellerId = o.getString("sellerId"),
        name = o.optString("name"),
        category = enumOrDefault(o.optString("category"), Category.LAINNYA),
        price = o.optLong("price", 0L),
        unit = o.optString("unit", "kg"),
        stock = o.optInt("stock", 0),
        minOrder = o.optInt("minOrder", 1),
        grade = o.optString("grade", "A"),
        harvestInfo = o.optString("harvestInfo"),
        location = o.optString("location"),
        description = o.optString("description"),
        imagePath = o.stringOrNull("imagePath"),
        isActive = o.optBoolean("isActive", true),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    )

    // ---------- Cart ----------
    private fun cartToJson(c: CartItem): JSONObject {
        val o = JSONObject()
        o.put("productId", c.productId)
        o.put("quantity", c.quantity)
        return o
    }

    private fun cartFromJson(o: JSONObject): CartItem = CartItem(
        productId = o.getString("productId"),
        quantity = o.optInt("quantity", 1),
    )

    // ---------- Order ----------
    private fun orderItemToJson(i: OrderItem): JSONObject {
        val o = JSONObject()
        o.put("productId", i.productId)
        o.put("name", i.name)
        o.put("emoji", i.emoji)
        o.put("unit", i.unit)
        o.put("quantity", i.quantity)
        o.put("price", i.price)
        return o
    }

    private fun orderItemFromJson(o: JSONObject): OrderItem = OrderItem(
        productId = o.optString("productId"),
        name = o.optString("name"),
        emoji = o.optString("emoji", "📦"),
        unit = o.optString("unit", "kg"),
        quantity = o.optInt("quantity", 0),
        price = o.optLong("price", 0L),
    )

    private fun orderToJson(x: Order): JSONObject {
        val o = JSONObject()
        o.put("id", x.id)
        o.put("code", x.code)
        o.put("buyerId", x.buyerId)
        o.put("sellerId", x.sellerId)
        o.put("items", x.items.toJsonArray { orderItemToJson(it) })
        o.put("shippingMethod", x.shippingMethod)
        o.put("paymentMethod", x.paymentMethod)
        o.put("address", x.address)
        o.put("note", x.note)
        o.put("status", x.status.name)
        o.put("createdAt", x.createdAt)
        o.put("updatedAt", x.updatedAt)
        o.put("source", x.source)
        return o
    }

    private fun orderFromJson(o: JSONObject): Order = Order(
        id = o.getString("id"),
        code = o.optString("code"),
        buyerId = o.optString("buyerId"),
        sellerId = o.optString("sellerId"),
        items = o.optJSONArray("items").mapObjects { orderItemFromJson(it) },
        shippingMethod = o.optString("shippingMethod"),
        paymentMethod = o.optString("paymentMethod"),
        address = o.optString("address"),
        note = o.optString("note"),
        status = enumOrDefault(o.optString("status"), OrderStatus.MENUNGGU),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
        source = o.optString("source"),
    )

    // ---------- Offer ----------
    private fun offerToJson(f: Offer): JSONObject {
        val o = JSONObject()
        o.put("id", f.id)
        o.put("productId", f.productId)
        o.put("productName", f.productName)
        o.put("emoji", f.emoji)
        o.put("unit", f.unit)
        o.put("listPrice", f.listPrice)
        o.put("buyerId", f.buyerId)
        o.put("sellerId", f.sellerId)
        o.put("quantity", f.quantity)
        o.put("offeredPrice", f.offeredPrice)
        o.put("counterPrice", f.counterPrice ?: JSONObject.NULL)
        o.put("finalPrice", f.finalPrice ?: JSONObject.NULL)
        o.put("note", f.note)
        o.put("status", f.status.name)
        o.put("createdAt", f.createdAt)
        o.put("updatedAt", f.updatedAt)
        o.put("orderId", f.orderId ?: JSONObject.NULL)
        return o
    }

    private fun offerFromJson(o: JSONObject): Offer = Offer(
        id = o.getString("id"),
        productId = o.optString("productId"),
        productName = o.optString("productName"),
        emoji = o.optString("emoji", "📦"),
        unit = o.optString("unit", "kg"),
        listPrice = o.optLong("listPrice", 0L),
        buyerId = o.optString("buyerId"),
        sellerId = o.optString("sellerId"),
        quantity = o.optInt("quantity", 0),
        offeredPrice = o.optLong("offeredPrice", 0L),
        counterPrice = o.longOrNull("counterPrice"),
        finalPrice = o.longOrNull("finalPrice"),
        note = o.optString("note"),
        status = enumOrDefault(o.optString("status"), OfferStatus.MENUNGGU),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
        orderId = o.stringOrNull("orderId"),
    )

    // ---------- BuyRequest ----------
    private fun responseToJson(r: RequestResponse): JSONObject {
        val o = JSONObject()
        o.put("id", r.id)
        o.put("sellerId", r.sellerId)
        o.put("price", r.price)
        o.put("quantity", r.quantity)
        o.put("note", r.note)
        o.put("createdAt", r.createdAt)
        return o
    }

    private fun responseFromJson(o: JSONObject): RequestResponse = RequestResponse(
        id = o.getString("id"),
        sellerId = o.optString("sellerId"),
        price = o.optLong("price", 0L),
        quantity = o.optInt("quantity", 0),
        note = o.optString("note"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    )

    private fun requestToJson(r: BuyRequest): JSONObject {
        val o = JSONObject()
        o.put("id", r.id)
        o.put("buyerId", r.buyerId)
        o.put("commodity", r.commodity)
        o.put("category", r.category.name)
        o.put("quantity", r.quantity)
        o.put("unit", r.unit)
        o.put("targetPrice", r.targetPrice)
        o.put("location", r.location)
        o.put("neededBy", r.neededBy)
        o.put("frequency", r.frequency)
        o.put("note", r.note)
        o.put("status", r.status.name)
        o.put("createdAt", r.createdAt)
        o.put("responses", r.responses.toJsonArray { responseToJson(it) })
        o.put("chosenResponseId", r.chosenResponseId ?: JSONObject.NULL)
        o.put("orderId", r.orderId ?: JSONObject.NULL)
        return o
    }

    private fun requestFromJson(o: JSONObject): BuyRequest = BuyRequest(
        id = o.getString("id"),
        buyerId = o.optString("buyerId"),
        commodity = o.optString("commodity"),
        category = enumOrDefault(o.optString("category"), Category.LAINNYA),
        quantity = o.optInt("quantity", 0),
        unit = o.optString("unit", "kg"),
        targetPrice = o.optLong("targetPrice", 0L),
        location = o.optString("location"),
        neededBy = o.optString("neededBy"),
        frequency = o.optString("frequency"),
        note = o.optString("note"),
        status = enumOrDefault(o.optString("status"), RequestStatus.TERBUKA),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
        responses = o.optJSONArray("responses").mapObjects { responseFromJson(it) },
        chosenResponseId = o.stringOrNull("chosenResponseId"),
        orderId = o.stringOrNull("orderId"),
    )
}

// ---------- Helper ----------
private inline fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray {
    val array = JSONArray()
    for (item in this) array.put(transform(item))
    return array
}

private inline fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    val result = ArrayList<T>(length())
    for (i in 0 until length()) {
        val obj = optJSONObject(i) ?: continue
        result.add(transform(obj))
    }
    return result
}

private fun JSONObject.stringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null

private fun JSONObject.longOrNull(key: String): Long? =
    if (has(key) && !isNull(key)) getLong(key) else null

private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default
