@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.DemoData
import com.samatani.app.data.MarketPrice
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.util.formatPercent
import com.samatani.app.util.formatRupiah
import com.samatani.app.util.openUrl

private const val OFFICIAL_PRICE_URL = "https://panelharga.badanpangan.go.id"

@Composable
fun MarketPriceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    val prices = DemoData.marketPrices.filter {
        query.isBlank() || it.commodity.contains(query.trim(), ignoreCase = true)
    }

    Scaffold(topBar = { SamaTopBar("Harga Pasar", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("ℹ️ Data contoh", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Harga di bawah hanya ilustrasi untuk versi awal aplikasi, bukan harga resmi. " +
                                "Untuk harga terkini per provinsi, buka Panel Harga Badan Pangan Nasional.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { openUrl(context, OFFICIAL_PRICE_URL) }) {
                            Text("Buka Panel Harga Bapanas")
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari komoditas…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                )
            }
            items(prices, key = { it.commodity }) { price -> PriceRow(price) }
            if (prices.isEmpty()) {
                item {
                    Text(
                        "Komoditas tidak ditemukan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceRow(price: MarketPrice) {
    val change = if (price.previousPrice > 0) (price.price - price.previousPrice) * 100.0 / price.previousPrice else 0.0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(price.emoji, fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(price.commodity, style = MaterialTheme.typography.titleSmall)
                Text(
                    "per ${price.unit} • ${price.region}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatRupiah(price.price), style = MaterialTheme.typography.titleSmall)
                Text(
                    when {
                        change > 0.0 -> "▲ ${formatPercent(change)}"
                        change < 0.0 -> "▼ ${formatPercent(change)}"
                        else -> "Stabil"
                    },
                    style = MaterialTheme.typography.labelMedium,
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
