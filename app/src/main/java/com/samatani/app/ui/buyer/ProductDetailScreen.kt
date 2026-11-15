@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.buyer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.Product
import com.samatani.app.data.Role
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.ContactCard
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.InfoTile
import com.samatani.app.ui.components.NoteBanner
import com.samatani.app.ui.components.ProductThumb
import com.samatani.app.ui.components.QuantityStepper
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.StatusPill
import com.samatani.app.ui.theme.PillGray
import com.samatani.app.ui.theme.PillRed
import com.samatani.app.util.dialPhone
import com.samatani.app.util.digitsOnly
import com.samatani.app.util.formatQty
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.openWhatsApp
import com.samatani.app.util.roundPrice

@Composable
fun ProductDetailScreen(
    user: User,
    productId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenCart: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val product = data.product(productId)
    if (product == null) {
        Scaffold(topBar = { SamaTopBar("Produk", onBack = onBack) }) { padding ->
            EmptyState(
                emoji = "😕",
                title = "Produk tidak ditemukan",
                message = "Produk ini mungkin sudah dihapus oleh petani.",
                modifier = Modifier.padding(padding),
            )
        }
    } else {
        ProductDetailContent(
            user = user,
            product = product,
            seller = data.user(product.sellerId),
            cartCount = data.cart.size,
            onBack = onBack,
            onEdit = onEdit,
            onOpenCart = onOpenCart,
        )
    }
}

/** Langkah tombol +/- menyesuaikan besar minimal pembelian. */
private fun stepFor(product: Product): Int = when {
    product.minOrder >= 100 -> 50
    product.minOrder >= 20 -> 10
    product.minOrder >= 5 -> 5
    else -> 1
}

@Composable
private fun ProductDetailContent(
    user: User,
    product: Product,
    seller: User?,
    cartCount: Int,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenCart: () -> Unit,
) {
    val repository = LocalRepository.current
    val context = LocalContext.current
    val isMine = product.sellerId == user.id
    val isBuyer = user.role == Role.PEMBELI
    val canBuy = isBuyer && !isMine && product.isActive && product.stock > 0
    val minQty = product.minOrder.coerceAtMost(product.stock).coerceAtLeast(1)
    var quantity by rememberSaveable(product.id) { mutableStateOf(minQty) }
    var showOffer by remember { mutableStateOf(false) }
    val safeQuantity = quantity.coerceIn(minQty, product.stock.coerceAtLeast(minQty))

    Scaffold(
        topBar = {
            SamaTopBar("Detail Produk", onBack = onBack, actions = {
                if (isBuyer) {
                    IconButton(onClick = onOpenCart) {
                        BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.toString()) } }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang")
                        }
                    }
                }
            })
        },
        bottomBar = {
            if (canBuy) {
                Surface(shadowElevation = 8.dp) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Jumlah (${product.unit})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "Subtotal ${formatRupiah(product.price * safeQuantity)}",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                            QuantityStepper(
                                value = safeQuantity,
                                onValueChange = { quantity = it },
                                min = minQty,
                                max = product.stock,
                                step = stepFor(product),
                                unit = product.unit,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { showOffer = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            ) { Text("🤝 Nego Harga") }
                            Button(
                                onClick = {
                                    repository.addToCart(product.id, safeQuantity)
                                    repository.showMessage("${formatQty(safeQuantity)} ${product.unit} ${product.name} masuk keranjang")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            ) {
                                Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Keranjang")
                            }
                        }
                    }
                }
            } else if (isMine) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = { onEdit(product.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(48.dp),
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ubah Produk Ini")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            ProductThumb(
                imagePath = product.imagePath,
                category = product.category,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                emojiSize = 96.sp,
            )
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        "${product.category.emoji} ${product.category.label}",
                        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    StatusPill(
                        "Grade ${product.grade}",
                        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    if (!product.isActive) StatusPill("Disembunyikan", PillGray)
                    if (product.stock <= 0) StatusPill("Stok habis", PillRed)
                }
                Spacer(Modifier.height(10.dp))
                Text(product.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${formatRupiah(product.price)} / ${product.unit}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoTile("📦", "Stok tersedia", "${formatQty(product.stock)} ${product.unit}", Modifier.weight(1f))
                    InfoTile("⚖️", "Minimal beli", "${formatQty(product.minOrder)} ${product.unit}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoTile("🗓️", "Waktu panen", product.harvestInfo.ifBlank { "-" }, Modifier.weight(1f))
                    InfoTile("📍", "Lokasi", product.location, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                ContactCard(
                    title = if (isMine) "Penjual (Anda)" else "Penjual",
                    user = seller,
                    onWhatsApp = {
                        if (seller != null) {
                            openWhatsApp(
                                context,
                                seller,
                                "Halo ${seller.name}, saya tertarik dengan ${product.name} " +
                                    "(${formatRupiah(product.price)}/${product.unit}) di aplikasi Sama Tani.",
                            )
                        }
                    },
                    onCall = { if (seller != null) dialPhone(context, seller) },
                )
                Spacer(Modifier.height(16.dp))
                Text("Deskripsi", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    product.description.ifBlank { "Belum ada deskripsi." },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!isBuyer && !isMine) {
                    Spacer(Modifier.height(16.dp))
                    NoteBanner("Anda sedang di mode Petani. Untuk membeli, ganti ke mode Pembeli di menu Akun.")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showOffer) {
        OfferDialog(
            product = product,
            initialQuantity = safeQuantity,
            onDismiss = { showOffer = false },
            onSubmit = { qty, price, note ->
                repository.makeOffer(product.id, qty, price, note)
                repository.showMessage("Tawaran terkirim. Pantau di menu Pesanan › Nego Harga.")
                showOffer = false
            },
        )
    }
}

@Composable
private fun OfferDialog(
    product: Product,
    initialQuantity: Int,
    onDismiss: () -> Unit,
    onSubmit: (quantity: Int, price: Long, note: String) -> Unit,
) {
    val minQty = product.minOrder.coerceAtMost(product.stock).coerceAtLeast(1)
    var quantityText by remember { mutableStateOf(initialQuantity.toString()) }
    var priceText by remember { mutableStateOf(roundPrice(product.price * 9 / 10).toString()) }
    var note by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nego Harga") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Harga petani: ${formatRupiah(product.price)}/${product.unit}. Ajukan harga yang menurut Anda pantas.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = digitsOnly(it, 7) },
                    label = { Text("Jumlah (${product.unit})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = digitsOnly(it, 10) },
                    label = { Text("Harga tawaran per ${product.unit}") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(200) },
                    label = { Text("Catatan (opsional)") },
                    maxLines = 3,
                )
                val error = errorText
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val qty = quantityText.toIntOrNull()
                val price = priceText.toLongOrNull()
                errorText = when {
                    qty == null || qty < minQty -> "Jumlah minimal ${formatQty(minQty)} ${product.unit}."
                    qty > product.stock -> "Jumlah melebihi stok (${formatQty(product.stock)} ${product.unit})."
                    price == null || price <= 0L -> "Masukkan harga tawaran."
                    price >= product.price -> "Tawaran harus di bawah harga petani. Untuk harga normal, pakai tombol Keranjang."
                    else -> null
                }
                if (errorText == null && qty != null && price != null) onSubmit(qty, price, note)
            }) { Text("Kirim Tawaran") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}
