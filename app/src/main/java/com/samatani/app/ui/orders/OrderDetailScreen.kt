@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.Order
import com.samatani.app.data.OrderStatus
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.ContactCard
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.LabelValue
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.SectionCard
import com.samatani.app.ui.components.StatusPill
import com.samatani.app.ui.theme.orderStatusColors
import com.samatani.app.util.dialPhone
import com.samatani.app.util.formatDateTime
import com.samatani.app.util.formatQty
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.openWhatsApp

private enum class OrderAction(
    val buttonLabel: String,
    val title: String,
    val message: String,
    val confirmLabel: String,
) {
    ACCEPT("Terima Pesanan", "Terima pesanan ini?", "Stok produk akan dikurangi sesuai jumlah pesanan.", "Terima"),
    REJECT("Tolak", "Tolak pesanan ini?", "Pembeli akan melihat pesanan ini ditolak.", "Tolak"),
    SHIP("Tandai Sudah Dikirim", "Pesanan sudah dikirim?", "Pastikan barang sudah dalam perjalanan ke pembeli.", "Sudah Dikirim"),
    COMPLETE("Pesanan Sudah Diterima", "Barang sudah diterima?", "Konfirmasi bahwa barang sudah sampai dan sesuai pesanan.", "Ya, Sudah Diterima"),
    CANCEL("Batalkan Pesanan", "Batalkan pesanan?", "Pesanan yang dibatalkan tidak bisa dikembalikan.", "Batalkan"),
}

private fun statusDescription(status: OrderStatus, viewerIsSeller: Boolean): String = when (status) {
    OrderStatus.MENUNGGU -> if (viewerIsSeller) "Pembeli menunggu konfirmasi Anda." else "Menunggu petani mengonfirmasi pesanan."
    OrderStatus.DIPROSES -> if (viewerIsSeller) "Siapkan barang lalu tandai sudah dikirim." else "Petani sedang menyiapkan pesanan Anda."
    OrderStatus.DIKIRIM -> if (viewerIsSeller) "Menunggu pembeli mengonfirmasi barang diterima." else "Pesanan dalam perjalanan. Konfirmasi setelah barang sampai."
    OrderStatus.SELESAI -> "Pesanan selesai. Terima kasih telah bertransaksi di Sama Tani!"
    OrderStatus.DITOLAK -> "Pesanan ditolak oleh petani."
    OrderStatus.DIBATALKAN -> "Pesanan dibatalkan oleh pembeli."
}

private fun statusEmoji(status: OrderStatus): String = when (status) {
    OrderStatus.MENUNGGU -> "⏳"
    OrderStatus.DIPROSES -> "📦"
    OrderStatus.DIKIRIM -> "🚚"
    OrderStatus.SELESAI -> "✅"
    OrderStatus.DITOLAK -> "❌"
    OrderStatus.DIBATALKAN -> "🚫"
}

@Composable
fun OrderDetailScreen(
    user: User,
    orderId: String,
    onBack: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val context = LocalContext.current
    val order = data.order(orderId)
    var pendingAction by remember { mutableStateOf<OrderAction?>(null) }

    val viewerIsSeller = order != null && order.sellerId == user.id
    val actions: List<Pair<OrderAction, Boolean>> = when {
        order == null -> emptyList()
        viewerIsSeller && order.status == OrderStatus.MENUNGGU -> listOf(OrderAction.REJECT to false, OrderAction.ACCEPT to true)
        viewerIsSeller && order.status == OrderStatus.DIPROSES -> listOf(OrderAction.SHIP to true)
        order.buyerId == user.id && order.status == OrderStatus.MENUNGGU -> listOf(OrderAction.CANCEL to false)
        order.buyerId == user.id && order.status == OrderStatus.DIKIRIM -> listOf(OrderAction.COMPLETE to true)
        else -> emptyList()
    }

    Scaffold(
        topBar = { SamaTopBar("Detail Pesanan", onBack = onBack) },
        bottomBar = {
            if (actions.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        actions.forEach { (action, primary) ->
                            if (primary) {
                                Button(
                                    onClick = { pendingAction = action },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                ) { Text(action.buttonLabel, maxLines = 1) }
                            } else {
                                OutlinedButton(
                                    onClick = { pendingAction = action },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                ) { Text(action.buttonLabel, maxLines = 1) }
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (order == null) {
            EmptyState(
                emoji = "😕",
                title = "Pesanan tidak ditemukan",
                message = "Pesanan ini mungkin sudah dihapus.",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        val counterpart = data.user(if (viewerIsSeller) order.buyerId else order.sellerId)
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StatusHeader(order, viewerIsSeller)
            ContactCard(
                title = if (viewerIsSeller) "Pembeli" else "Petani",
                user = counterpart,
                onWhatsApp = {
                    if (counterpart != null) {
                        openWhatsApp(
                            context,
                            counterpart,
                            "Halo ${counterpart.name}, saya menghubungi terkait pesanan ${order.code} di Sama Tani.",
                        )
                    }
                },
                onCall = { if (counterpart != null) dialPhone(context, counterpart) },
            )
            SectionCard("🧾 Rincian barang") {
                order.items.forEach { item ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.emoji, fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${formatQty(item.quantity)} ${item.unit} × ${formatRupiah(item.price)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(formatRupiah(item.subtotal), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                LabelValue("Ongkos kirim", "Disepakati bersama")
                LabelValue("Total barang", formatRupiah(order.total), highlight = true)
            }
            SectionCard("🚚 Pengiriman & pembayaran") {
                LabelValue("Kode pesanan", order.code)
                if (order.source.isNotBlank()) LabelValue("Asal pesanan", order.source)
                LabelValue("Pengiriman", order.shippingMethod)
                LabelValue("Alamat", order.address)
                LabelValue("Pembayaran", order.paymentMethod)
                if (order.note.isNotBlank()) LabelValue("Catatan", order.note)
                LabelValue("Dibuat", formatDateTime(order.createdAt))
                LabelValue("Diperbarui", formatDateTime(order.updatedAt))
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    val action = pendingAction
    if (action != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(action.title) },
            text = { Text(action.message) },
            confirmButton = {
                TextButton(onClick = {
                    val ok = when (action) {
                        OrderAction.ACCEPT -> repository.acceptOrder(orderId)
                        OrderAction.REJECT -> repository.rejectOrder(orderId)
                        OrderAction.SHIP -> repository.shipOrder(orderId)
                        OrderAction.COMPLETE -> repository.completeOrder(orderId)
                        OrderAction.CANCEL -> repository.cancelOrder(orderId)
                    }
                    if (ok) {
                        repository.showMessage(
                            when (action) {
                                OrderAction.ACCEPT -> "Pesanan diterima. Siapkan barangnya ya!"
                                OrderAction.REJECT -> "Pesanan ditolak"
                                OrderAction.SHIP -> "Pesanan ditandai dikirim 🚚"
                                OrderAction.COMPLETE -> "Pesanan selesai. Terima kasih! 🎉"
                                OrderAction.CANCEL -> "Pesanan dibatalkan"
                            },
                        )
                    }
                    pendingAction = null
                }) { Text(action.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) { Text("Kembali") }
            },
        )
    }
}

@Composable
private fun StatusHeader(order: Order, viewerIsSeller: Boolean) {
    val steps = listOf(OrderStatus.MENUNGGU, OrderStatus.DIPROSES, OrderStatus.DIKIRIM, OrderStatus.SELESAI)
    val currentIndex = steps.indexOf(order.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(statusEmoji(order.status), fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    StatusPill(order.status.label, orderStatusColors(order.status))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        statusDescription(order.status, viewerIsSeller),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (currentIndex >= 0) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    steps.forEachIndexed { index, step ->
                        val done = index <= currentIndex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(
                                        if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (done) "✓" else "${index + 1}",
                                    color = if (done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                when (step) {
                                    OrderStatus.MENUNGGU -> "Dipesan"
                                    OrderStatus.DIPROSES -> "Diproses"
                                    OrderStatus.DIKIRIM -> "Dikirim"
                                    else -> "Selesai"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
