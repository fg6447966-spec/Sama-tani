@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.buyer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samatani.app.data.Options
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.ChoiceList
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.SectionCard
import com.samatani.app.ui.components.formContentPadding
import com.samatani.app.util.formatQty
import com.samatani.app.util.formatRupiah

@Composable
fun CheckoutScreen(
    user: User,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    var address by rememberSaveable { mutableStateOf(user.location) }
    var shipping by rememberSaveable { mutableStateOf(Options.shippingMethods.first()) }
    var payment by rememberSaveable { mutableStateOf(Options.paymentMethods.first()) }
    var note by rememberSaveable { mutableStateOf("") }
    var placed by remember { mutableStateOf(false) }

    val lines = data.cart.mapNotNull { item ->
        val product = data.product(item.productId) ?: return@mapNotNull null
        if (!product.isActive || product.stock <= 0) return@mapNotNull null
        product to item.quantity.coerceAtMost(product.stock)
    }
    val grouped = lines.groupBy { it.first.sellerId }
    val total = lines.sumOf { (p, qty) -> p.price * qty }

    Scaffold(
        topBar = { SamaTopBar("Checkout", onBack = onBack) },
        bottomBar = {
            if (lines.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Total belanja",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                formatRupiah(total),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Button(
                            onClick = {
                                val count = repository.checkout(address, shipping, payment, note)
                                if (count > 0) {
                                    placed = true
                                    repository.showMessage(
                                        if (count > 1) "$count pesanan dibuat (dipisah per petani) ✅" else "Pesanan berhasil dibuat ✅",
                                    )
                                    onDone()
                                }
                            },
                            enabled = address.isNotBlank(),
                            modifier = Modifier.height(48.dp),
                        ) { Text("Buat Pesanan", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        },
    ) { padding ->
        if (lines.isEmpty()) {
            if (placed) {
                EmptyState(
                    emoji = "✅",
                    title = "Pesanan dibuat",
                    message = "Membuka daftar pesanan…",
                    modifier = Modifier.padding(padding),
                )
            } else {
                EmptyState(
                    emoji = "🛒",
                    title = "Tidak ada produk untuk dipesan",
                    message = "Keranjang kosong atau produknya sudah tidak tersedia.",
                    modifier = Modifier.padding(padding),
                    actionLabel = "Kembali",
                    onAction = onBack,
                )
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .formContentPadding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard("📍 Alamat pengiriman") {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.take(200) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nama jalan, desa/kelurahan, kecamatan, kota") },
                    minLines = 2,
                )
            }
            SectionCard("🚚 Pengiriman") {
                ChoiceList(Options.shippingMethods, shipping) { shipping = it }
            }
            SectionCard("💳 Pembayaran") {
                ChoiceList(Options.paymentMethods, payment) { payment = it }
            }
            SectionCard("🧾 Ringkasan pesanan") {
                grouped.forEach { (sellerId, group) ->
                    val seller = data.user(sellerId)
                    Text(
                        "🌾 ${seller?.displayName ?: "Petani"}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )
                    group.forEach { (product, qty) ->
                        Row(Modifier.padding(vertical = 2.dp)) {
                            Text(
                                "${product.name} × ${formatQty(qty)} ${product.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(formatRupiah(product.price * qty), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
                Row {
                    Text("Total", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(formatRupiah(total), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ongkos kirim disepakati langsung dengan petani." +
                        if (grouped.size > 1) " Pesanan Anda akan dipisah menjadi ${grouped.size} pesanan (satu per petani)." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SectionCard("📝 Catatan untuk petani (opsional)") {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(300) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Mis. minta dikemas karung 25 kg, kirim pagi hari") },
                    minLines = 2,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
