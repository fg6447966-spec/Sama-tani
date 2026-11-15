package com.samatani.app.ui

object Routes {
    // Tab utama
    const val FARMER_HOME = "farmer_home"
    const val MY_PRODUCTS = "my_products"
    const val MARKET = "market"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val REQUESTS = "requests"
    const val ACCOUNT = "account"

    // Halaman detail
    const val PRODUCT = "product/{id}"
    const val PRODUCT_FORM = "product_form?id={id}"
    const val CHECKOUT = "checkout"
    const val ORDER = "order/{id}"
    const val REQUEST = "request/{id}"
    const val REQUEST_FORM = "request_form"
    const val MARKET_PRICES = "market_prices"
    const val EDIT_PROFILE = "edit_profile"

    fun product(id: String) = "product/$id"
    fun productForm(id: String? = null) = if (id == null) "product_form" else "product_form?id=$id"
    fun order(id: String) = "order/$id"
    fun request(id: String) = "request/$id"
}
