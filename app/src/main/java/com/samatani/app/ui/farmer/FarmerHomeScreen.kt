@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.DemoData
import com.samatani.app.data.OfferStatus
import com.samatani.app.data.OrderStatus
import com.samatani.app.data.RequestStatus
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.Routes
import com.samatani.app.ui.components.OrderCard
import com.samatani.app.ui.components.QuickAction
import com.samatani.app.ui.components.RequestCard
import com.samatani.app.ui.components.SectionHeader
import com.samatani.app.ui.components.StatCard
import com.samatani.app.ui.components.StatusPill
import com.samatani.app.ui.theme.headerBrush
import com.samatani.app.ui.theme.offerStatusColors
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.formatRupiahShort
import com.samatani.app.util.formatPercent
import com.samatani.app.util.formatQty

@Composable
fun FarmerHomeScreen(
    user: User,
    onAddProduct: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onOpenRequest: (String) -> Unit,
    onOpenTab: (String) -> Unit,
    onOpenMarketPrices: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()

    val myProducts = data.products.filter { it.sellerId == user.id }
    val incoming = data.orders.filter { it.sellerId == user.id }
    val newOrders = incoming.filter { it.status == OrderStatus.MENUNGGU }.sortedByDescending { it.createdAt }
    val toShip = incoming.filter { it.status == OrderStatus.DIPROSES }.sortedByDescending { it.updatedAt }
    val pendingOffers = data.offers
        .filter { it.sellerId == user.id && it.status == OfferStatus.MENUNGGU }
        .sortedByDescending { it.createdAt }
    val revenue = incoming.filter { it.status == OrderStatus.SELESAI }.sumOf { it.total }
    val openRequests = data.requests
        .filter { it.status == RequestStatus.TERBUKA && it.buyerId != user.id }
        .sortedByDescending { it.createdAt }
        .take(3)
    val lowStock = myProducts.filter { it.isActive && it.stock in 0 until it.minOrder.coerceAtLeast(1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(headerBrush(), RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            ) {
                Text(
                    "Halo, ${user.name.substringBefore(" ")} 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                Text(
                    "${user.displayName} • ${user.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💰", fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Total penjualan selesai", style = MaterialTheme.typography.labelMedium)
                            Text(formatRupiah(revenue), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Produk aktif", myProducts.count { it.isActive }.toString(), "🌱", Modifier.weight(1f)) {
                        onOpenTab(Routes.MY_PRODUCTS)
                    }
                    StatCard("Pesanan baru", newOrders.size.toString(), "📥", Modifier.weight(1f)) {
                        onOpenTab(Routes.ORDERS)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Tawaran nego", pendingOffers.size.toString(), "🤝", Modifier.weight(1f)) {
                        onOpenTab(Routes.ORDERS)
                    }
                    StatCard("Perlu dikirim", toShip.size.toString(), "🚚", Modifier.weight(1f)) {
                        onOpenTab(Routes.ORDERS)
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Aksi cepat")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction("➕", "Tambah Produk", Modifier.weight(1f), onAddProduct)
                    QuickAction("📢", "Permintaan", Modifier.weight(1f)) { onOpenTab(Routes.REQUESTS) }
                    QuickAction("📈", "Harga Pasar", Modifier.weight(1f), onOpenMarketPrices)
                }
            }
        }

        if (lowStock.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(14.dp),
                    onClick = { onOpenTab(Routes.MY_PRODUCTS) },
                ) {
                    Text(
                        "⚠️ Stok menipis: " + lowStock.joinToString { "${it.name} (${formatQty(it.stock)} ${it.unit})" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        // ---------- Perlu tindakan ----------
        item {
            SectionHeader(
                "Perlu tindakan",
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp),
                actionLabel = "Semua pesanan",
                onAction = { onOpenTab(Routes.ORDERS) },
            )
        }
        if (newOrders.isEmpty() && pendingOffers.isEmpty() && toShip.isEmpty()) {
            item {
                Text(
                    "✅ Semua beres. Belum ada pesanan atau tawaran yang menunggu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        items((newOrders + toShip).take(3), key = { "order-${it.id}" }) { order ->
            OrderCard(
                order = order,
                counterpart = data.user(order.buyerId),
                viewerIsSeller = true,
                onClick = { onOpenOrder(order.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
        items(pendingOffers.take(3), key = { "offer-${it.id}" }) { offer ->
            Card(
                onClick = { onOpenTab(Routes.ORDERS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(offer.emoji, fontSize = 28.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Nego: ${offer.productName}", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${data.user(offer.buyerId)?.displayName ?: "Pembeli"} menawar ${formatRupiah(offer.offeredPrice)}/${offer.unit} " +
                                "(harga Anda ${formatRupiah(offer.listPrice)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    StatusPill(offer.status.label, offerStatusColors(offer.status))
                }
            }
        }

        // ---------- Permintaan pembeli ----------
        item {
            SectionHeader(
                "Permintaan pembeli terbaru",
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp),
                actionLabel = "Lihat semua",
                onAction = { onOpenTab(Routes.REQUESTS) },
            )
        }
        items(openRequests, key = { "req-${it.id}" }) { request ->
            RequestCard(
                request = request,
                buyer = data.user(request.buyerId),
                viewerId = user.id,
                onClick = { onOpenRequest(request.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        // ---------- Harga pasar ----------
        item {
            SectionHeader(
                "Harga pasar (contoh)",
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp),
                actionLabel = "Lihat semua",
                onAction = onOpenMarketPrices,
            )
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            ) {
                DemoData.marketPrices.take(4).forEach { price ->
                    val change = (price.price - price.previousPrice) * 100.0 / price.previousPrice
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(price.emoji, fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(price.commodity, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${formatRupiah(price.price)}/${price.unit}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                when {
                                    change > 0.0 -> "▲ " + formatPercent(change)
                                    change < 0.0 -> "▼ " + formatPercent(change)
                                    else -> "Stabil"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    change > 0.0 -> Color(0xFFC62828)
                                    change < 0.0 -> Color(0xFF2E7D32)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Ringkasan pendapatan singkat untuk dipakai di layar lain bila perlu. */
fun revenueLabel(total: Long): String = formatRupiahShort(total)
