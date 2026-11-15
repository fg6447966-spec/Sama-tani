@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.requests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.BuyRequest
import com.samatani.app.data.RequestResponse
import com.samatani.app.data.RequestStatus
import com.samatani.app.data.Role
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.Avatar
import com.samatani.app.ui.components.ContactCard
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.LabelValue
import com.samatani.app.ui.components.NoteBanner
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.SectionCard
import com.samatani.app.ui.components.StatusPill
import com.samatani.app.ui.theme.PillGray
import com.samatani.app.ui.theme.PillGreen
import com.samatani.app.ui.theme.categoryTint
import com.samatani.app.util.dialPhone
import com.samatani.app.util.digitsOnly
import com.samatani.app.util.formatQty
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.openWhatsApp
import com.samatani.app.util.timeAgo

@Composable
fun RequestDetailScreen(
    user: User,
    requestId: String,
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val context = LocalContext.current
    val request = data.request(requestId)
    var showRespond by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmClose by remember { mutableStateOf(false) }
    var toChoose by remember { mutableStateOf<RequestResponse?>(null) }

    val isOwner = request != null && request.buyerId == user.id
    val myResponse = request?.responses?.firstOrNull { it.sellerId == user.id }
    val canRespond = request != null && !isOwner && user.role == Role.PETANI && request.status == RequestStatus.TERBUKA

    Scaffold(
        topBar = {
            SamaTopBar("Detail Permintaan", onBack = onBack, actions = {
                if (isOwner) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus permintaan")
                    }
                }
            })
        },
        bottomBar = {
            if (canRespond) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = { showRespond = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(52.dp),
                    ) {
                        Text(
                            if (myResponse == null) "Kirim Penawaran" else "Ubah Penawaran Saya",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else if (isOwner && request?.status == RequestStatus.TERBUKA) {
                Surface(shadowElevation = 8.dp) {
                    OutlinedButton(
                        onClick = { confirmClose = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(48.dp),
                    ) { Text("Tutup Permintaan") }
                }
            }
        },
    ) { padding ->
        if (request == null) {
            EmptyState(
                emoji = "😕",
                title = "Permintaan tidak ditemukan",
                message = "Permintaan ini mungkin sudah dihapus pembeli.",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        val buyer = data.user(request.buyerId)
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RequestHeader(request, buyer)

            SectionCard("Detail kebutuhan") {
                LabelValue("Jumlah", "${formatQty(request.quantity)} ${request.unit}")
                LabelValue("Harga target", "${formatRupiah(request.targetPrice)}/${request.unit}", highlight = true)
                LabelValue("Perkiraan nilai", formatRupiah(request.targetPrice * request.quantity))
                LabelValue("Lokasi kirim", request.location)
                LabelValue("Dibutuhkan", request.neededBy.ifBlank { "-" })
                LabelValue("Frekuensi", request.frequency)
                LabelValue("Diposting", timeAgo(request.createdAt))
                if (request.note.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Catatan pembeli:", style = MaterialTheme.typography.labelLarge)
                    Text(request.note, style = MaterialTheme.typography.bodyMedium)
                }
            }

            val orderId = request.orderId
            if (orderId != null && (isOwner || request.chosenResponseId == myResponse?.id)) {
                NoteBanner(
                    if (isOwner) "Anda sudah memilih penawaran. Pesanan otomatis dibuat." else "Penawaran Anda dipilih! Pesanan otomatis dibuat.",
                    emoji = "🎉",
                )
                Button(onClick = { onOpenOrder(orderId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Lihat Pesanan")
                }
            }

            if (isOwner) {
                Text(
                    "Penawaran dari petani (${request.responses.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (request.responses.isEmpty()) {
                    Text(
                        "Belum ada penawaran. Petani di sekitar akan melihat permintaan Anda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                request.responses.sortedBy { it.price }.forEach { response ->
                    val seller = data.user(response.sellerId)
                    ResponseCard(
                        response = response,
                        seller = seller,
                        unit = request.unit,
                        chosen = request.chosenResponseId == response.id,
                        canChoose = request.status == RequestStatus.TERBUKA,
                        onChoose = { toChoose = response },
                        onWhatsApp = {
                            if (seller != null) {
                                openWhatsApp(
                                    context,
                                    seller,
                                    "Halo ${seller.name}, terima kasih atas penawaran ${request.commodity} di Sama Tani.",
                                )
                            }
                        },
                    )
                }
            } else {
                ContactCard(
                    title = "Pembeli",
                    user = buyer,
                    onWhatsApp = {
                        if (buyer != null) {
                            openWhatsApp(
                                context,
                                buyer,
                                "Halo ${buyer.name}, saya petani di Sama Tani. Saya bisa memenuhi permintaan ${request.commodity} Anda.",
                            )
                        }
                    },
                    onCall = { if (buyer != null) dialPhone(context, buyer) },
                )
                if (myResponse != null) {
                    SectionCard("Penawaran Anda") {
                        LabelValue("Harga", "${formatRupiah(myResponse.price)}/${request.unit}", highlight = true)
                        LabelValue("Jumlah", "${formatQty(myResponse.quantity)} ${request.unit}")
                        if (myResponse.note.isNotBlank()) LabelValue("Catatan", myResponse.note)
                        LabelValue("Dikirim", timeAgo(myResponse.createdAt))
                    }
                }
                if (user.role != Role.PETANI) {
                    NoteBanner("Ganti ke mode Petani di menu Akun untuk mengirim penawaran.")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    // ---------- Dialog ----------
    if (showRespond && request != null) {
        RespondDialog(
            request = request,
            initialPrice = myResponse?.price ?: request.targetPrice,
            initialQuantity = myResponse?.quantity ?: request.quantity,
            onDismiss = { showRespond = false },
            onSubmit = { price, qty, note ->
                repository.respondToRequest(request.id, price, qty, note)
                repository.showMessage("Penawaran terkirim ke ${data.user(request.buyerId)?.displayName ?: "pembeli"} ✅")
                showRespond = false
            },
        )
    }
    val choosing = toChoose
    if (choosing != null && request != null) {
        AlertDialog(
            onDismissRequest = { toChoose = null },
            title = { Text("Pilih penawaran ini?") },
            text = {
                Text(
                    "${data.user(choosing.sellerId)?.displayName ?: "Petani"} • ${formatRupiah(choosing.price)}/${request.unit} " +
                        "untuk ${formatQty(choosing.quantity)} ${request.unit}. Permintaan akan ditutup dan pesanan otomatis dibuat.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    repository.chooseResponse(request.id, choosing.id)
                    repository.showMessage("Penawaran dipilih. Pesanan dibuat ✅")
                    toChoose = null
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { toChoose = null }) { Text("Batal") } },
        )
    }
    if (confirmClose && request != null) {
        AlertDialog(
            onDismissRequest = { confirmClose = false },
            title = { Text("Tutup permintaan?") },
            text = { Text("Petani tidak bisa mengirim penawaran baru setelah permintaan ditutup.") },
            confirmButton = {
                TextButton(onClick = {
                    repository.closeRequest(request.id)
                    confirmClose = false
                }) { Text("Tutup") }
            },
            dismissButton = { TextButton(onClick = { confirmClose = false }) { Text("Batal") } },
        )
    }
    if (confirmDelete && request != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hapus permintaan?") },
            text = { Text("Permintaan dan semua penawaran di dalamnya akan dihapus.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    repository.deleteRequest(request.id)
                    repository.showMessage("Permintaan dihapus")
                    onBack()
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun RequestHeader(request: BuyRequest, buyer: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(categoryTint(request.category), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(request.category.emoji, fontSize = 28.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(request.commodity, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    "oleh ${buyer?.displayName ?: "Pembeli"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                StatusPill(
                    request.status.label,
                    if (request.status == RequestStatus.TERBUKA) PillGreen else PillGray,
                )
            }
        }
    }
}

@Composable
private fun ResponseCard(
    response: RequestResponse,
    seller: User?,
    unit: String,
    chosen: Boolean,
    canChoose: Boolean,
    onChoose: () -> Unit,
    onWhatsApp: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = if (chosen) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(seller?.initials ?: "?", size = 40)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(seller?.displayName ?: "Petani", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "📍 ${seller?.location ?: "-"} • ${timeAgo(response.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (chosen) StatusPill("Dipilih", PillGreen)
            }
            Spacer(Modifier.height(10.dp))
            LabelValue("Harga", "${formatRupiah(response.price)}/$unit", highlight = true)
            LabelValue("Jumlah", "${formatQty(response.quantity)} $unit")
            LabelValue("Total", formatRupiah(response.price * response.quantity))
            if (response.note.isNotBlank()) {
                Text(
                    "“${response.note}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onWhatsApp, modifier = Modifier.weight(1f)) { Text("💬 WhatsApp") }
                if (canChoose) {
                    Button(onClick = onChoose, modifier = Modifier.weight(1f)) { Text("Pilih") }
                }
            }
        }
    }
}

@Composable
private fun RespondDialog(
    request: BuyRequest,
    initialPrice: Long,
    initialQuantity: Int,
    onDismiss: () -> Unit,
    onSubmit: (price: Long, quantity: Int, note: String) -> Unit,
) {
    var priceText by remember { mutableStateOf(initialPrice.toString()) }
    var quantityText by remember { mutableStateOf(initialQuantity.toString()) }
    var note by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kirim Penawaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Pembeli butuh ${formatQty(request.quantity)} ${request.unit} dengan harga target " +
                        "${formatRupiah(request.targetPrice)}/${request.unit}.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = digitsOnly(it, 10) },
                    label = { Text("Harga Anda per ${request.unit}") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = digitsOnly(it, 7) },
                    label = { Text("Jumlah yang sanggup (${request.unit})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(200) },
                    label = { Text("Catatan (opsional)") },
                    placeholder = { Text("Mis. bisa kirim tiap Senin, sudah disortir") },
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
                val price = priceText.toLongOrNull()
                val qty = quantityText.toIntOrNull()
                errorText = when {
                    price == null || price <= 0L -> "Masukkan harga penawaran."
                    qty == null || qty <= 0 -> "Masukkan jumlah yang sanggup Anda penuhi."
                    else -> null
                }
                if (errorText == null && price != null && qty != null) onSubmit(price, qty, note)
            }) { Text("Kirim") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
