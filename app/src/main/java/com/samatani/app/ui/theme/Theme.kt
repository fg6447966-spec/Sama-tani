package com.samatani.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.samatani.app.data.Category
import com.samatani.app.data.OfferStatus
import com.samatani.app.data.OrderStatus

// Warna merek Sama Tani
val TaniGreen = Color(0xFF2E7D32)
val TaniGreenDark = Color(0xFF1B5E20)
val TaniGreenDeep = Color(0xFF173F1A)
val TaniLeaf = Color(0xFF558B2F)
val TaniHarvest = Color(0xFFF9A825)

private val LightColors = lightColorScheme(
    primary = TaniGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF0A3D12),
    secondary = Color(0xFF8A5A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8B0),
    onSecondaryContainer = Color(0xFF3E2A00),
    tertiary = Color(0xFF6D4C41),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEFDCD5),
    onTertiaryContainer = Color(0xFF2B1A14),
    background = Color(0xFFF8FAF4),
    onBackground = Color(0xFF191D17),
    surface = Color(0xFFF8FAF4),
    onSurface = Color(0xFF191D17),
    surfaceVariant = Color(0xFFDEE5D9),
    onSurfaceVariant = Color(0xFF424940),
    outline = Color(0xFF72796F),
    outlineVariant = Color(0xFFC2C9BD),
    inverseSurface = Color(0xFF2E322B),
    inverseOnSurface = Color(0xFFEFF2EA),
    inversePrimary = Color(0xFF8BD68F),
    surfaceDim = Color(0xFFD8DBD3),
    surfaceBright = Color(0xFFF8FAF4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F5EE),
    surfaceContainer = Color(0xFFECEFE8),
    surfaceContainerHigh = Color(0xFFE6E9E2),
    surfaceContainerHighest = Color(0xFFE1E4DC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD68F),
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFFFCA5C),
    onSecondary = Color(0xFF402D00),
    secondaryContainer = Color(0xFF5C4300),
    onSecondaryContainer = Color(0xFFFFE8B0),
    tertiary = Color(0xFFD7BFB5),
    onTertiary = Color(0xFF3B2A23),
    tertiaryContainer = Color(0xFF543F37),
    onTertiaryContainer = Color(0xFFEFDCD5),
    background = Color(0xFF111410),
    onBackground = Color(0xFFE1E4DC),
    surface = Color(0xFF111410),
    onSurface = Color(0xFFE1E4DC),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BD),
    outline = Color(0xFF8C9388),
    outlineVariant = Color(0xFF424940),
    inverseSurface = Color(0xFFE1E4DC),
    inverseOnSurface = Color(0xFF2E322B),
    inversePrimary = TaniGreen,
    surfaceDim = Color(0xFF111410),
    surfaceBright = Color(0xFF373A35),
    surfaceContainerLowest = Color(0xFF0C0F0B),
    surfaceContainerLow = Color(0xFF191D18),
    surfaceContainer = Color(0xFF1D211C),
    surfaceContainerHigh = Color(0xFF282B26),
    surfaceContainerHighest = Color(0xFF323630),
)

private val BaseTypography = Typography()

private val SamaTaniTypography = BaseTypography.copy(
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = BaseTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
)

@Composable
fun SamaTaniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SamaTaniTypography,
        content = content,
    )
}

/** Warna latar header & top bar (selalu gelap agar ikon status bar putih terbaca). */
@Composable
fun headerColor(): Color = if (isSystemInDarkTheme()) TaniGreenDeep else TaniGreen

@Composable
fun headerBrush(): Brush =
    if (isSystemInDarkTheme()) Brush.verticalGradient(listOf(Color(0xFF10301A), TaniGreenDeep))
    else Brush.verticalGradient(listOf(TaniGreenDark, TaniGreen))

/** Warna lembut di belakang emoji/foto produk per kategori. */
fun categoryTint(category: Category): Color = when (category) {
    Category.SAYURAN -> Color(0xFFE3F2DC)
    Category.BUAH -> Color(0xFFFFF1CC)
    Category.CABAI_BAWANG -> Color(0xFFFDE0DC)
    Category.PADI_BIJI -> Color(0xFFF6EBCB)
    Category.UMBI -> Color(0xFFEFE3D6)
    Category.PERKEBUNAN -> Color(0xFFE8DDD5)
    Category.PETERNAKAN -> Color(0xFFFCE9D8)
    Category.PERIKANAN -> Color(0xFFDCEEF8)
    Category.LAINNYA -> Color(0xFFECEFF1)
}

/** Pasangan (latar, teks) untuk label status pesanan. */
fun orderStatusColors(status: OrderStatus): Pair<Color, Color> = when (status) {
    OrderStatus.MENUNGGU -> Color(0xFFFFF1D6) to Color(0xFF8A5A00)
    OrderStatus.DIPROSES -> Color(0xFFE1ECFB) to Color(0xFF1C4F9C)
    OrderStatus.DIKIRIM -> Color(0xFFDDF2EE) to Color(0xFF00695C)
    OrderStatus.SELESAI -> Color(0xFFE1F2E2) to Color(0xFF1E6B24)
    OrderStatus.DITOLAK, OrderStatus.DIBATALKAN -> Color(0xFFFCE3E3) to Color(0xFFB3261E)
}

fun offerStatusColors(status: OfferStatus): Pair<Color, Color> = when (status) {
    OfferStatus.MENUNGGU -> Color(0xFFFFF1D6) to Color(0xFF8A5A00)
    OfferStatus.DITAWAR_BALIK -> Color(0xFFE1ECFB) to Color(0xFF1C4F9C)
    OfferStatus.DITERIMA -> Color(0xFFE1F2E2) to Color(0xFF1E6B24)
    OfferStatus.DITOLAK -> Color(0xFFFCE3E3) to Color(0xFFB3261E)
}

val PillGreen = Color(0xFFE1F2E2) to Color(0xFF1E6B24)
val PillGray = Color(0xFFECEFF1) to Color(0xFF546E7A)
val PillRed = Color(0xFFFCE3E3) to Color(0xFFB3261E)
