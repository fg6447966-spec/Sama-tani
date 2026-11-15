@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.farmer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.Category
import com.samatani.app.data.ImageStore
import com.samatani.app.data.Options
import com.samatani.app.data.Product
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.ChipSelector
import com.samatani.app.ui.components.ProductThumb
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.formContentPadding
import com.samatani.app.util.digitsOnly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProductFormScreen(
    user: User,
    productId: String?,
    onBack: () -> Unit,
) {
    val repository = LocalRepository.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val existing = remember(productId) { productId?.let { repository.current.product(it) } }

    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: Category.SAYURAN) }
    var price by rememberSaveable { mutableStateOf(existing?.price?.toString() ?: "") }
    var unit by rememberSaveable { mutableStateOf(existing?.unit ?: "kg") }
    var stock by rememberSaveable { mutableStateOf(existing?.stock?.toString() ?: "") }
    var minOrder by rememberSaveable { mutableStateOf(existing?.minOrder?.toString() ?: "1") }
    var grade by rememberSaveable { mutableStateOf(existing?.grade ?: "A") }
    var harvest by rememberSaveable { mutableStateOf(existing?.harvestInfo ?: "") }
    var location by rememberSaveable { mutableStateOf(existing?.location ?: user.location) }
    var description by rememberSaveable { mutableStateOf(existing?.description ?: "") }
    var imagePath by rememberSaveable { mutableStateOf(existing?.imagePath) }
    var importing by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                val path = withContext(Dispatchers.IO) { ImageStore.importImage(context.applicationContext, uri) }
                importing = false
                if (path != null) imagePath = path else errorText = "Foto gagal dimuat. Coba pilih foto lain."
            }
        }
    }

    fun save() {
        val priceValue = price.toLongOrNull()
        val stockValue = stock.toIntOrNull()
        val minValue = minOrder.toIntOrNull()
        errorText = when {
            name.trim().length < 3 -> "Nama produk minimal 3 huruf."
            priceValue == null || priceValue <= 0L -> "Harga harus diisi dan lebih dari 0."
            stockValue == null -> "Stok wajib diisi (boleh 0)."
            minValue == null || minValue < 1 -> "Minimal pembelian paling sedikit 1."
            location.trim().length < 3 -> "Lokasi wajib diisi."
            else -> null
        }
        if (errorText != null || priceValue == null || stockValue == null || minValue == null) return

        val product = Product(
            id = existing?.id ?: repository.newId(),
            sellerId = existing?.sellerId ?: user.id,
            name = name.trim(),
            category = category,
            price = priceValue,
            unit = unit,
            stock = stockValue,
            minOrder = minValue,
            grade = grade,
            harvestInfo = harvest.trim(),
            location = location.trim(),
            description = description.trim(),
            imagePath = imagePath,
            isActive = existing?.isActive ?: true,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        )
        val oldPath = existing?.imagePath
        if (oldPath != null && oldPath != imagePath) ImageStore.deleteImage(oldPath)
        repository.saveProduct(product)
        repository.showMessage(if (existing == null) "Produk berhasil ditambahkan 🎉" else "Perubahan disimpan")
        onBack()
    }

    Scaffold(
        topBar = { SamaTopBar(if (existing == null) "Tambah Produk" else "Ubah Produk", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { save() },
                    enabled = !importing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp)
                        .height(52.dp),
                ) { Text(if (existing == null) "Simpan & Tampilkan di Pasar" else "Simpan Perubahan", fontWeight = FontWeight.Bold) }
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
            // ---------- Foto ----------
            val currentImage = imagePath
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !importing) {
                        photoPicker.launch(
                            PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                .build(),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                ProductThumb(
                    imagePath = currentImage,
                    category = category,
                    modifier = Modifier.fillMaxSize(),
                    emojiSize = 64.sp,
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ) {
                    Text(
                        when {
                            importing -> "Memuat foto…"
                            currentImage == null -> "📷 Tambah foto produk (opsional)"
                            else -> "📷 Ganti foto"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
            if (currentImage != null) {
                TextButton(onClick = { imagePath = null }) { Text("Hapus foto") }
            }
            Spacer(Modifier.height(16.dp))

            // ---------- Info dasar ----------
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                label = { Text("Nama produk") },
                placeholder = { Text("Contoh: Cabai Merah Keriting") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            FieldLabel("Kategori")
            ChipSelector(
                options = Category.entries.toList(),
                selected = category,
                label = { "${it.emoji} ${it.label}" },
                onSelect = { category = it },
            )
            Spacer(Modifier.height(16.dp))

            // ---------- Harga & satuan ----------
            FieldLabel("Satuan jual")
            ChipSelector(
                options = Options.units,
                selected = unit,
                label = { it },
                onSelect = { unit = it },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = digitsOnly(it, 10) },
                label = { Text("Harga per $unit") },
                prefix = { Text("Rp") },
                suffix = { Text("/$unit") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = digitsOnly(it, 7) },
                    label = { Text("Stok ($unit)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = minOrder,
                    onValueChange = { minOrder = digitsOnly(it, 7) },
                    label = { Text("Min. beli ($unit)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            FieldLabel("Kualitas (grade)")
            ChipSelector(
                options = Options.grades,
                selected = grade,
                label = { "Grade $it" },
                onSelect = { grade = it },
            )
            Spacer(Modifier.height(16.dp))

            // ---------- Detail ----------
            OutlinedTextField(
                value = harvest,
                onValueChange = { harvest = it.take(60) },
                label = { Text("Waktu panen") },
                placeholder = { Text("Contoh: Panen 2 hari lalu / Siap panen 20 Des") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it.take(80) },
                label = { Text("Lokasi barang") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(500) },
                label = { Text("Deskripsi") },
                placeholder = { Text("Varietas, cara tanam, kemasan, jadwal kirim, dll.") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            val error = errorText
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                ) {
                    Text(error, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}
