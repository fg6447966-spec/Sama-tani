@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samatani.app.data.Category
import com.samatani.app.data.Options
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.ChipSelector
import com.samatani.app.ui.components.NoteBanner
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.formContentPadding
import com.samatani.app.util.digitsOnly

@Composable
fun RequestFormScreen(
    user: User,
    onBack: () -> Unit,
) {
    val repository = LocalRepository.current
    var commodity by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(Category.SAYURAN) }
    var quantity by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("kg") }
    var targetPrice by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf(user.location) }
    var neededBy by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf(Options.frequencies.first()) }
    var note by rememberSaveable { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val qty = quantity.toIntOrNull()
        val price = targetPrice.toLongOrNull()
        errorText = when {
            commodity.trim().length < 3 -> "Tulis nama komoditas yang dicari (min. 3 huruf)."
            qty == null || qty <= 0 -> "Jumlah kebutuhan wajib diisi."
            price == null || price <= 0L -> "Harga target wajib diisi."
            location.trim().length < 3 -> "Lokasi pengiriman wajib diisi."
            else -> null
        }
        if (errorText != null || qty == null || price == null) return
        repository.postRequest(
            commodity = commodity,
            category = category,
            quantity = qty,
            unit = unit,
            targetPrice = price,
            location = location,
            neededBy = neededBy.ifBlank { "Secepatnya" },
            frequency = frequency,
            note = note,
        )
        repository.showMessage("Permintaan dipasang. Petani akan segera mengirim penawaran 📢")
        onBack()
    }

    Scaffold(
        topBar = { SamaTopBar("Buat Permintaan", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp)
                        .height(52.dp),
                ) { Text("Pasang Permintaan", fontWeight = FontWeight.Bold) }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .formContentPadding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            NoteBanner(
                "Permintaan Anda akan dilihat petani. Mereka bisa mengirim penawaran harga, lalu Anda pilih yang paling cocok.",
                emoji = "📢",
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = commodity,
                onValueChange = { commodity = it.take(60) },
                label = { Text("Komoditas yang dicari") },
                placeholder = { Text("Contoh: Bawang Merah, Beras Medium") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            FormLabel("Kategori")
            ChipSelector(
                options = Category.entries.toList(),
                selected = category,
                label = { "${it.emoji} ${it.label}" },
                onSelect = { category = it },
            )
            Spacer(Modifier.height(14.dp))
            FormLabel("Satuan")
            ChipSelector(
                options = Options.units,
                selected = unit,
                label = { it },
                onSelect = { unit = it },
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = digitsOnly(it, 7) },
                    label = { Text("Jumlah ($unit)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = { targetPrice = digitsOnly(it, 10) },
                    label = { Text("Harga target/$unit") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it.take(80) },
                label = { Text("Lokasi pengiriman") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = neededBy,
                onValueChange = { neededBy = it.take(40) },
                label = { Text("Dibutuhkan kapan?") },
                placeholder = { Text("Contoh: Minggu depan / Setiap Senin") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            FormLabel("Frekuensi pembelian")
            ChipSelector(
                options = Options.frequencies,
                selected = frequency,
                label = { it },
                onSelect = { frequency = it },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(300) },
                label = { Text("Spesifikasi / catatan (opsional)") },
                placeholder = { Text("Ukuran, kualitas, kemasan, cara bayar…") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            val error = errorText
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}
