@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.BuyRequest
import com.samatani.app.data.Category
import com.samatani.app.data.ImageStore
import com.samatani.app.data.Offer
import com.samatani.app.data.OfferStatus
import com.samatani.app.data.Order
import com.samatani.app.data.Product
import com.samatani.app.data.RequestStatus
import com.samatani.app.data.User
import com.samatani.app.ui.theme.PillGray
import com.samatani.app.ui.theme.PillGreen
import com.samatani.app.ui.theme.categoryTint
import com.samatani.app.ui.theme.offerStatusColors
import com.samatani.app.ui.theme.orderStatusColors
import com.samatani.app.util.formatQty
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.timeAgo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Foto produk bila ada; bila tidak, emoji kategori di atas latar berwarna. */
@Composable
fun ProductThumb(
    imagePath: String?,
    category: Category,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 40.sp,
) {
    val bitmap by produceState<ImageBitmap?>(null, imagePath) {
        value = if (imagePath == null) {
            null
        } else {
            withContext(Dispatchers.IO) { ImageStore.loadThumbnail(imagePath)?.asImageBitmap() }
        }
    }
    Box(
        modifier = modifier.background(categoryTint(category)),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(category.emoji, fontSize = emojiSize)
        }
    }
}

private val CardShape = RoundedCornerShape(16.dp)

@Composable
private fun cardColors() = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)

@Composable
private fun cardElevation() = CardDefaults.cardElevation(defaultElevation = 1.dp)

/** Kartu produk untuk grid pasar. */
@Composable
fun ProductCard(
    product: Product,
    seller: User?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        colors = cardColors(),
        elevation = cardElevation(),
    ) {
        ProductThumb(
            imagePath = product.imagePath,
            category = product.category,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f),
        )
        Column(Modifier.padding(12.dp)) {
            Text(
                product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${formatRupiah(product.price)}/${product.unit}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    product.location.substringBefore(","),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                seller?.displayName ?: "Petani",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Grade ${product.grade} • Stok ${formatQty(product.stock)} ${product.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Kartu ringkas pesanan. */
@Composable
fun OrderCard(
    order: Order,
    counterpart: User?,
    viewerIsSeller: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = cardColors(),
        elevation = cardElevation(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    order.code,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(order.status.label, orderStatusColors(order.status))
            }
            Spacer(Modifier.height(10.dp))
            val first = order.items.firstOrNull()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(first?.emoji ?: "📦", fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (first != null) "${first.name} • ${formatQty(first.quantity)} ${first.unit}" else "-",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (order.items.size > 1) {
                        Text(
                            "+${order.items.size - 1} produk lainnya",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        (if (viewerIsSeller) "Pembeli: " else "Petani: ") + (counterpart?.displayName ?: "-"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    timeAgo(order.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    formatRupiah(order.total),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Kartu nego harga, lengkap dengan tombol aksi sesuai peran. */
@Composable
fun OfferCard(
    offer: Offer,
    counterpart: User?,
    viewerIsSeller: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCounter: () -> Unit,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = cardColors(),
        elevation = cardElevation(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(offer.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(offer.productName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        (if (viewerIsSeller) "Dari: " else "Ke: ") + (counterpart?.displayName ?: "-"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                StatusPill(offer.status.label, offerStatusColors(offer.status))
            }
            Spacer(Modifier.height(10.dp))
            LabelValue("Harga awal", "${formatRupiah(offer.listPrice)}/${offer.unit}")
            LabelValue(
                "Tawaran pembeli",
                "${formatRupiah(offer.offeredPrice)}/${offer.unit}",
                highlight = offer.status == OfferStatus.MENUNGGU,
            )
            val counter = offer.counterPrice
            if (counter != null) {
                LabelValue(
                    "Tawar balik petani",
                    "${formatRupiah(counter)}/${offer.unit}",
                    highlight = offer.status == OfferStatus.DITAWAR_BALIK,
                )
            }
            LabelValue("Jumlah", "${formatQty(offer.quantity)} ${offer.unit}")
            val finalPrice = offer.finalPrice
            if (finalPrice != null) {
                LabelValue("Total disepakati", formatRupiah(finalPrice * offer.quantity), highlight = true)
            }
            if (offer.note.isNotBlank()) {
                Text(
                    "“${offer.note}”",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Text(
                timeAgo(offer.updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            val sellerCanAct = viewerIsSeller && offer.status == OfferStatus.MENUNGGU
            val buyerCanAct = !viewerIsSeller && offer.status == OfferStatus.DITAWAR_BALIK
            if (sellerCanAct || buyerCanAct) {
                Spacer(Modifier.height(12.dp))
                val compact = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), contentPadding = compact) {
                        Text("Tolak", maxLines = 1)
                    }
                    if (sellerCanAct) {
                        OutlinedButton(onClick = onCounter, modifier = Modifier.weight(1f), contentPadding = compact) {
                            Text("Tawar Balik", maxLines = 1)
                        }
                    }
                    Button(onClick = onAccept, modifier = Modifier.weight(1f), contentPadding = compact) {
                        Text("Terima", maxLines = 1)
                    }
                }
            }
            val orderId = offer.orderId
            if (offer.status == OfferStatus.DITERIMA && orderId != null) {
                TextButton(onClick = { onOpenOrder(orderId) }) { Text("Lihat pesanan →") }
            }
        }
    }
}

/** Kartu permintaan pembeli. */
@Composable
fun RequestCard(
    request: BuyRequest,
    buyer: User?,
    viewerId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val myResponse = request.responses.firstOrNull { it.sellerId == viewerId }
    val chosenIsMine = myResponse != null && request.chosenResponseId == myResponse.id
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = cardColors(),
        elevation = cardElevation(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryTint(request.category)),
                    contentAlignment = Alignment.Center,
                ) { Text(request.category.emoji, fontSize = 22.sp) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.commodity, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        buyer?.displayName ?: "Pembeli",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                when {
                    chosenIsMine -> StatusPill("Dipilih 🎉", PillGreen)
                    request.status == RequestStatus.TERBUKA -> StatusPill("Terbuka", PillGreen)
                    else -> StatusPill("Ditutup", PillGray)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoChip("⚖️ ${formatQty(request.quantity)} ${request.unit}")
                InfoChip("💰 ${formatRupiah(request.targetPrice)}/${request.unit}")
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoChip("📍 ${request.location.substringBefore(",")}")
                InfoChip("🔁 ${request.frequency}")
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Dibutuhkan: ${request.neededBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${request.responses.size} penawaran",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (myResponse != null && !chosenIsMine) {
                Text(
                    "✓ Anda sudah mengirim penawaran ${formatRupiah(myResponse.price)}/${request.unit}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

/** Kotak statistik kecil di beranda. */
@Composable
fun StatCard(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        colors = cardColors(),
        elevation = cardElevation(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

/** Tombol pintasan berwarna di beranda. */
@Composable
fun QuickAction(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Kartu info penjual/pembeli dengan tombol WhatsApp & telepon. */
@Composable
fun ContactCard(
    title: String,
    user: User?,
    onWhatsApp: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(title, modifier) {
        if (user == null) {
            Text("Data pengguna tidak ditemukan.", style = MaterialTheme.typography.bodyMedium)
            return@SectionCard
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(user.initials)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (user.businessName.isNotBlank()) {
                    Text(user.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("📍 ${user.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onWhatsApp, modifier = Modifier.weight(1f)) { Text("💬 WhatsApp") }
            OutlinedButton(onClick = onCall, modifier = Modifier.weight(1f)) { Text("📞 Telepon") }
        }
    }
}
