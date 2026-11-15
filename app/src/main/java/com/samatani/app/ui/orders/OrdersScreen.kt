@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samatani.app.data.Offer
import com.samatani.app.data.OfferStatus
import com.samatani.app.data.Role
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.OfferCard
import com.samatani.app.ui.components.OrderCard
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.util.digitsOnly
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.roundPrice

@Composable
fun OrdersScreen(
    user: User,
    onOpenOrder: (String) -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val isSeller = user.role == Role.PETANI
    var tab by rememberSaveable { mutableStateOf(0) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var counterFor by remember { mutableStateOf<Offer?>(null) }

    val myOrders = data.orders
        .filter { if (isSeller) it.sellerId == user.id else it.buyerId == user.id }
        .sortedByDescending { it.updatedAt }
    val myOffers = data.offers
        .filter { if (isSeller) it.sellerId == user.id else it.buyerId == user.id }
        .sortedByDescending { it.updatedAt }
    val activeOrders = myOrders.filter { it.status.isActive }
    val openOffers = myOffers.count { it.status == OfferStatus.MENUNGGU || it.status == OfferStatus.DITAWAR_BALIK }

    Scaffold(topBar = { SamaTopBar(if (isSeller) "Pesanan Masuk" else "Pesanan Saya") }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("Pesanan (${activeOrders.size})") },
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Nego Harga ($openOffers)") },
                )
            }

            if (tab == 0) {
                val shown = if (showHistory) myOrders.filterNot { it.status.isActive } else activeOrders
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !showHistory,
                                onClick = { showHistory = false },
                                label = { Text("Berjalan") },
                            )
                            FilterChip(
                                selected = showHistory,
                                onClick = { showHistory = true },
                                label = { Text("Riwayat") },
                            )
                        }
                    }
                    if (shown.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = if (showHistory) "📜" else "📭",
                                title = if (showHistory) "Belum ada riwayat" else "Belum ada pesanan berjalan",
                                message = if (isSeller) {
                                    "Pesanan dari pembeli akan muncul di sini."
                                } else {
                                    "Pesanan yang Anda buat dari Pasar akan muncul di sini."
                                },
                            )
                        }
                    }
                    items(shown, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            counterpart = data.user(if (isSeller) order.buyerId else order.sellerId),
                            viewerIsSeller = isSeller,
                            onClick = { onOpenOrder(order.id) },
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (myOffers.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "🤝",
                                title = "Belum ada nego harga",
                                message = if (isSeller) {
                                    "Tawaran harga dari pembeli untuk produk Anda akan muncul di sini."
                                } else {
                                    "Buka produk di Pasar lalu tekan \"Nego Harga\" untuk menawar."
                                },
                            )
                        }
                    }
                    items(myOffers, key = { it.id }) { offer ->
                        OfferCard(
                            offer = offer,
                            counterpart = data.user(if (isSeller) offer.buyerId else offer.sellerId),
                            viewerIsSeller = isSeller,
                            onAccept = {
                                repository.acceptOffer(offer.id)
                                repository.showMessage("Harga disepakati. Pesanan otomatis dibuat ✅")
                            },
                            onReject = {
                                repository.rejectOffer(offer.id)
                                repository.showMessage("Tawaran ditolak")
                            },
                            onCounter = { counterFor = offer },
                            onOpenOrder = onOpenOrder,
                        )
                    }
                }
            }
        }
    }

    val countering = counterFor
    if (countering != null) {
        CounterOfferDialog(
            offer = countering,
            onDismiss = { counterFor = null },
            onSubmit = { price ->
                repository.counterOffer(countering.id, price)
                repository.showMessage("Tawar balik ${formatRupiah(price)}/${countering.unit} terkirim")
                counterFor = null
            },
        )
    }
}

@Composable
private fun CounterOfferDialog(
    offer: Offer,
    onDismiss: () -> Unit,
    onSubmit: (Long) -> Unit,
) {
    var priceText by remember {
        mutableStateOf(roundPrice((offer.offeredPrice + offer.listPrice) / 2).toString())
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tawar Balik") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Pembeli menawar ${formatRupiah(offer.offeredPrice)}/${offer.unit}, " +
                        "harga awal Anda ${formatRupiah(offer.listPrice)}/${offer.unit}.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = digitsOnly(it, 10) },
                    label = { Text("Harga tawar balik per ${offer.unit}") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                errorText = when {
                    price == null || price <= offer.offeredPrice ->
                        "Harga harus di atas tawaran pembeli (${formatRupiah(offer.offeredPrice)})."
                    price > offer.listPrice ->
                        "Harga tidak boleh melebihi harga awal (${formatRupiah(offer.listPrice)})."
                    else -> null
                }
                if (errorText == null && price != null) onSubmit(price)
            }) { Text("Kirim") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}
