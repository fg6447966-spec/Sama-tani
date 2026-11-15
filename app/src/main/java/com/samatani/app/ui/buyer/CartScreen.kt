@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.buyer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.Product
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.ProductThumb
import com.samatani.app.ui.components.QuantityStepper
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.util.formatQty
import com.samatani.app.util.formatRupiah

@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onBrowse: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val lines = data.cart.mapNotNull { item -> data.product(item.productId)?.let { it to item.quantity } }
    val available = lines.filter { (p, _) -> p.isActive && p.stock > 0 }
    val total = available.sumOf { (p, qty) -> p.price * qty.coerceAtMost(p.stock) }
    val grouped = lines.groupBy { it.first.sellerId }

    Scaffold(
        topBar = { SamaTopBar("Keranjang") },
        bottomBar = {
            if (available.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Total (${available.size} produk)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                formatRupiah(total),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Button(onClick = onCheckout, modifier = Modifier.height(48.dp)) {
                            Text("Checkout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (lines.isEmpty()) {
            EmptyState(
                emoji = "🛒",
                title = "Keranjang masih kosong",
                message = "Yuk cari hasil panen segar langsung dari petani.",
                modifier = Modifier.padding(padding),
                actionLabel = "Jelajahi Pasar",
                onAction = onBrowse,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                grouped.forEach { (sellerId, group) ->
                    val seller = data.user(sellerId)
                    item(key = "seller-$sellerId") {
                        Text(
                            "🌾 ${seller?.displayName ?: "Petani"} • ${seller?.location?.substringBefore(",") ?: ""}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    items(group, key = { it.first.id }) { (product, qty) ->
                        CartLine(
                            product = product,
                            quantity = qty,
                            onOpen = { onOpenProduct(product.id) },
                            onQuantityChange = { repository.setCartQuantity(product.id, it) },
                            onRemove = { repository.removeFromCart(product.id) },
                        )
                    }
                }
                item {
                    Text(
                        "Ongkos kirim disepakati langsung dengan petani. Pesanan dari beberapa petani otomatis dipisah.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLine(
    product: Product,
    quantity: Int,
    onOpen: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    val unavailable = !product.isActive || product.stock <= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductThumb(
                imagePath = product.imagePath,
                category = product.category,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpen),
                emojiSize = 28.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${formatRupiah(product.price)}/${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(6.dp))
                if (unavailable) {
                    Text(
                        "Sedang tidak tersedia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    val minQty = product.minOrder.coerceAtMost(product.stock).coerceAtLeast(1)
                    QuantityStepper(
                        value = quantity.coerceIn(minQty, product.stock.coerceAtLeast(minQty)),
                        onValueChange = onQuantityChange,
                        min = minQty,
                        max = product.stock,
                        unit = product.unit,
                    )
                    if (quantity > product.stock) {
                        Text(
                            "Stok tinggal ${formatQty(product.stock)} ${product.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus dari keranjang")
            }
        }
    }
}
