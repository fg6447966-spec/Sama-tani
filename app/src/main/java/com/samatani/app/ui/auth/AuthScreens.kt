@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samatani.app.data.Role
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.NoteBanner
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.formContentPadding
import com.samatani.app.ui.theme.TaniGreenDark
import com.samatani.app.ui.theme.TaniGreenDeep
import com.samatani.app.ui.theme.TaniLeaf

@Composable
fun AuthFlow() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onRegister = { nav.navigate("register") },
                onLogin = { nav.navigate("login") },
            )
        }
        composable("register") {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onGoLogin = { nav.navigate("login") { popUpTo("welcome") } },
            )
        }
        composable("login") {
            LoginScreen(
                onBack = { nav.popBackStack() },
                onGoRegister = { nav.navigate("register") { popUpTo("welcome") } },
            )
        }
    }
}

@Composable
private fun WelcomeScreen(onRegister: () -> Unit, onLogin: () -> Unit) {
    val repository = LocalRepository.current
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TaniGreenDeep, TaniGreenDark, TaniLeaf))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(36.dp))
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("🌾", fontSize = 56.sp) }
            Spacer(Modifier.height(20.dp))
            Text(
                "Sama Tani",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Pasar hasil bumi langsung dari petani ke pembeli",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))

            FeatureLine("🌱", "Petani menjual hasil panen dengan harga yang adil, tanpa rantai tengkulak panjang.")
            FeatureLine("🚚", "Distributor, toko, dan rumah makan dapat pasokan langsung dari sumbernya.")
            FeatureLine("🤝", "Nego harga, pasang permintaan rutin, dan pantau pesanan dalam satu aplikasi.")

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TaniGreenDark),
            ) { Text("Daftar Akun Baru", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
            ) { Text("Saya sudah punya akun") }

            Spacer(Modifier.height(28.dp))
            Text(
                "Ingin melihat-lihat dulu? Coba akun demo:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DemoChip("🌾 Demo Petani") { repository.loginDemo(Role.PETANI) }
                DemoChip("🏪 Demo Pembeli") { repository.loginDemo(Role.PEMBELI) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureLine(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 20.sp) }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
private fun DemoChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.18f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

/** Validasi data profil. @return pesan error atau null bila valid. */
fun validateProfile(name: String, phone: String, location: String): String? = when {
    name.trim().length < 3 -> "Nama minimal 3 huruf."
    phone.count { it.isDigit() } !in 9..15 -> "Nomor HP tidak valid (9–15 angka)."
    location.trim().length < 3 -> "Lokasi (kabupaten/kota) wajib diisi."
    else -> null
}

@Composable
private fun RegisterScreen(onBack: () -> Unit, onGoLogin: () -> Unit) {
    val repository = LocalRepository.current
    var role by rememberSaveable { mutableStateOf(Role.PETANI) }
    var name by rememberSaveable { mutableStateOf("") }
    var business by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { SamaTopBar("Daftar Akun", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .formContentPadding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text("Saya mendaftar sebagai", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoleCard(
                    emoji = "🌾",
                    title = "Petani / Produsen",
                    subtitle = "Menjual hasil panen",
                    selected = role == Role.PETANI,
                    onClick = { role = Role.PETANI },
                    modifier = Modifier.weight(1f),
                )
                RoleCard(
                    emoji = "🏪",
                    title = "Pembeli / Distributor",
                    subtitle = "Membeli dari petani",
                    selected = role == Role.PEMBELI,
                    onClick = { role = Role.PEMBELI },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                label = { Text("Nama lengkap") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = business,
                onValueChange = { business = it.take(80) },
                label = {
                    Text(if (role == Role.PETANI) "Kelompok tani / nama usaha (opsional)" else "Nama perusahaan / toko (opsional)")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { input -> phone = input.filter { it.isDigit() || it == '+' }.take(16) },
                label = { Text("Nomor HP / WhatsApp") },
                placeholder = { Text("08xxxxxxxxxx") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it.take(80) },
                label = { Text("Kabupaten / Kota, Provinsi") },
                placeholder = { Text("Contoh: Enrekang, Sulawesi Selatan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            val error = errorText
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    errorText = validateProfile(name, phone, location)
                        ?: repository.register(name, business, phone, location, role)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("Daftar & Mulai", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onGoLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Sudah punya akun? Masuk")
            }
            Spacer(Modifier.height(8.dp))
            NoteBanner(
                "Versi awal ini menyimpan data di HP Anda. Verifikasi OTP & sinkron antar-perangkat akan aktif setelah aplikasi terhubung ke server.",
                emoji = "ℹ️",
            )
        }
    }
}

@Composable
private fun RoleCard(
    emoji: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LoginScreen(onBack: () -> Unit, onGoRegister: () -> Unit) {
    val repository = LocalRepository.current
    var phone by rememberSaveable { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { SamaTopBar("Masuk", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .formContentPadding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text("Selamat datang kembali 👋", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Masukkan nomor HP yang Anda pakai saat mendaftar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() || it == '+' }.take(16)
                    errorText = null
                },
                label = { Text("Nomor HP / WhatsApp") },
                placeholder = { Text("08xxxxxxxxxx") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            val error = errorText
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onGoRegister) { Text("Daftar sekarang") }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (!repository.login(phone)) {
                        errorText = "Nomor belum terdaftar. Periksa lagi atau daftar akun baru."
                    }
                },
                enabled = phone.count { it.isDigit() } >= 9,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("Masuk", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onGoRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Belum punya akun? Daftar")
            }
        }
    }
}
