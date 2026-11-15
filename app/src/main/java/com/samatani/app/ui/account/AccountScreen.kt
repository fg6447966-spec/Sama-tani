@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.samatani.app.BuildConfig
import com.samatani.app.data.OrderStatus
import com.samatani.app.data.RequestStatus
import com.samatani.app.data.Role
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.auth.validateProfile
import com.samatani.app.ui.components.LabelValue
import com.samatani.app.ui.components.MenuRow
import com.samatani.app.ui.components.NoteBanner
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.SectionCard
import com.samatani.app.ui.components.StatusPill
import com.samatani.app.ui.components.formContentPadding
import com.samatani.app.ui.theme.headerBrush
import com.samatani.app.util.formatDate
import com.samatani.app.util.formatRupiahShort

@Composable
fun AccountScreen(
    user: User,
    onEditProfile: () -> Unit,
    onOpenMarketPrices: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val isFarmer = user.role == Role.PETANI
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var confirmSwitch by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val sold = data.orders.filter { it.sellerId == user.id && it.status == OrderStatus.SELESAI }
    val bought = data.orders.filter { it.buyerId == user.id && it.status == OrderStatus.SELESAI }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // ---------- Header ----------
        Column(
            Modifier
                .fillMaxWidth()
                .background(headerBrush(), RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    user.initials,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1B5E20),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(user.name, style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
            if (user.businessName.isNotBlank()) {
                Text(
                    user.businessName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(10.dp))
            StatusPill(
                if (isFarmer) "🌾 Mode Petani / Produsen" else "🏪 Mode Pembeli / Distributor",
                Color.White.copy(alpha = 0.2f) to Color.White,
            )
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (user.isDemo) {
                NoteBanner(
                    "Anda memakai akun demo. Mitra contoh akan membalas pesanan & nego secara otomatis supaya Anda bisa mencoba alurnya.",
                    emoji = "🧪",
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isFarmer) {
                    MiniStat("Produk", data.products.count { it.sellerId == user.id }.toString(), Modifier.weight(1f))
                    MiniStat("Terjual", sold.size.toString(), Modifier.weight(1f))
                    MiniStat("Pendapatan", formatRupiahShort(sold.sumOf { it.total }), Modifier.weight(1f))
                } else {
                    MiniStat("Pesanan", data.orders.count { it.buyerId == user.id }.toString(), Modifier.weight(1f))
                    MiniStat(
                        "Permintaan",
                        data.requests.count { it.buyerId == user.id && it.status == RequestStatus.TERBUKA }.toString(),
                        Modifier.weight(1f),
                    )
                    MiniStat("Belanja", formatRupiahShort(bought.sumOf { it.total }), Modifier.weight(1f))
                }
            }

            SectionCard("Info akun") {
                LabelValue("Nomor HP", user.phone)
                LabelValue("Lokasi", user.location)
                LabelValue("Bergabung", formatDate(user.joinedAt))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                MenuRow(Icons.Filled.Edit, "Ubah profil", onClick = onEditProfile)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MenuRow(
                    Icons.Filled.SwapHoriz,
                    if (isFarmer) "Ganti ke mode Pembeli" else "Ganti ke mode Petani",
                    subtitle = "Satu akun bisa menjual sekaligus membeli",
                    onClick = { confirmSwitch = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MenuRow(Icons.Filled.TrendingUp, "Harga pasar", subtitle = "Contoh harga & tautan harga resmi", onClick = onOpenMarketPrices)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MenuRow(Icons.Filled.Info, "Tentang Sama Tani", onClick = { showAbout = true })
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MenuRow(
                    Icons.Filled.Refresh,
                    "Reset data demo",
                    subtitle = "Hapus data di HP ini & muat ulang data contoh",
                    onClick = { confirmReset = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MenuRow(Icons.AutoMirrored.Filled.ExitToApp, "Keluar", danger = true, onClick = { confirmLogout = true })
            }

            Text(
                "Sama Tani versi ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmSwitch) {
        ConfirmDialog(
            title = if (isFarmer) "Ganti ke mode Pembeli?" else "Ganti ke mode Petani?",
            message = if (isFarmer) {
                "Anda akan melihat Pasar, Keranjang, dan Permintaan Saya. Produk Anda tetap tersimpan."
            } else {
                "Anda akan melihat Beranda petani, Produk Saya, dan Permintaan Pembeli. Keranjang akan dikosongkan."
            },
            confirmLabel = "Ganti",
            onConfirm = {
                confirmSwitch = false
                repository.switchRole()
            },
            onDismiss = { confirmSwitch = false },
        )
    }
    if (confirmReset) {
        ConfirmDialog(
            title = "Reset semua data?",
            message = "Semua akun, produk, dan pesanan di HP ini akan dihapus lalu diganti data contoh. Tindakan ini tidak bisa dibatalkan.",
            confirmLabel = "Reset",
            danger = true,
            onConfirm = {
                confirmReset = false
                repository.resetAllData()
            },
            onDismiss = { confirmReset = false },
        )
    }
    if (confirmLogout) {
        ConfirmDialog(
            title = "Keluar dari akun?",
            message = "Data Anda tetap tersimpan di HP ini. Masuk lagi dengan nomor HP yang sama.",
            confirmLabel = "Keluar",
            danger = true,
            onConfirm = {
                confirmLogout = false
                repository.logout()
            },
            onDismiss = { confirmLogout = false },
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Tentang Sama Tani") },
            text = {
                Text(
                    "Sama Tani mempertemukan petani & produsen dengan pembeli seperti distributor, toko, dan rumah makan. " +
                        "Jual hasil panen, nego harga, pasang permintaan rutin, dan pantau pesanan dalam satu aplikasi.\n\n" +
                        "Versi ${BuildConfig.VERSION_NAME} • data tersimpan di perangkat.",
                )
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Tutup") } },
        )
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp,
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmLabel,
                    color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
fun EditProfileScreen(user: User, onBack: () -> Unit) {
    val repository = LocalRepository.current
    var name by rememberSaveable { mutableStateOf(user.name) }
    var business by rememberSaveable { mutableStateOf(user.businessName) }
    var phone by rememberSaveable { mutableStateOf(user.phone) }
    var location by rememberSaveable { mutableStateOf(user.location) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { SamaTopBar("Ubah Profil", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .formContentPadding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                label = { Text("Nama lengkap") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = business,
                onValueChange = { business = it.take(80) },
                label = { Text(if (user.role == Role.PETANI) "Kelompok tani / nama usaha" else "Nama perusahaan / toko") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { input -> phone = input.filter { it.isDigit() || it == '+' || it == '-' }.take(18) },
                label = { Text("Nomor HP / WhatsApp") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it.take(80) },
                label = { Text("Kabupaten / Kota, Provinsi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            val error = errorText
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = {
                    val problem = validateProfile(name, phone, location)
                        ?: repository.updateProfile(name, business, phone, location)
                    errorText = problem
                    if (problem == null) {
                        repository.showMessage("Profil diperbarui")
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("Simpan", fontWeight = FontWeight.Bold) }
        }
    }
}
