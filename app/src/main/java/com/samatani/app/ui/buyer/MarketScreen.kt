@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.Category
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.ProductCard
import com.samatani.app.ui.theme.headerBrush

private enum class SortOption(val label: String) {
    TERBARU("Terbaru"),
    TERMURAH("Termurah"),
    TERMAHAL("Termahal"),
}

@Composable
fun MarketScreen(
    user: User,
    onOpenProduct: (String) -> Unit,
    onOpenCart: () -> Unit,
    onOpenMarketPrices: () -> Unit,
    onCreateRequest: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<Category?>(null) }
    var sort by rememberSaveable { mutableStateOf(SortOption.TERBARU) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val products = remember(data.products, query, category, sort, user.id) {
        val q = query.trim()
        val filtered = data.products.filter { p ->
            p.isActive && p.stock > 0 && p.sellerId != user.id &&
                (category == null || p.category == category) &&
                (q.isEmpty() ||
                    p.name.contains(q, ignoreCase = true) ||
                    p.location.contains(q, ignoreCase = true) ||
                    p.category.label.contains(q, ignoreCase = true))
        }
        when (sort) {
            SortOption.TERBARU -> filtered.sortedByDescending { it.createdAt }
            SortOption.TERMURAH -> filtered.sortedBy { it.price }
            SortOption.TERMAHAL -> filtered.sortedByDescending { it.price }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ---------- Header ----------
        Column(
            Modifier
                .fillMaxWidth()
                .background(headerBrush(), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Halo, ${user.name.substringBefore(" ")} 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                        "Mau beli hasil tani apa hari ini?",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                }
                IconButton(onClick = onOpenCart) {
                    BadgedBox(badge = {
                        if (data.cart.isNotEmpty()) Badge { Text(data.cart.size.toString()) }
                    }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang", tint = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            val fieldText = Color(0xFF191D17)
            val fieldHint = Color(0xFF6B7266)
            TextField(
                value = query,
                onValueChange = { query = it.take(50) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                placeholder = { Text("Cari cabai, beras, atau lokasi…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = fieldText,
                    unfocusedTextColor = fieldText,
                    cursorColor = Color(0xFF2E7D32),
                    focusedPlaceholderColor = fieldHint,
                    unfocusedPlaceholderColor = fieldHint,
                    focusedLeadingIconColor = fieldHint,
                    unfocusedLeadingIconColor = fieldHint,
                    focusedTrailingIconColor = fieldHint,
                    unfocusedTrailingIconColor = fieldHint,
                ),
            )
        }

        // ---------- Isi ----------
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = category == null,
                            onClick = { category = null },
                            label = { Text("Semua") },
                        )
                    }
                    items(Category.entries.size) { index ->
                        val c = Category.entries[index]
                        FilterChip(
                            selected = category == c,
                            onClick = { category = if (category == c) null else c },
                            label = { Text("${c.emoji} ${c.label}") },
                        )
                    }
                }
            }

            if (query.isBlank() && category == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PromoCard("📈", "Harga Pasar", "Cek harga komoditas", Modifier.weight(1f), onOpenMarketPrices)
                        PromoCard("📢", "Butuh rutin?", "Pasang permintaan", Modifier.weight(1f), onCreateRequest)
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${products.size} produk tersedia",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        TextButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(sort.label)
                        }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        sort = option
                                        sortMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        emoji = "🔍",
                        title = "Produk tidak ditemukan",
                        message = "Coba kata kunci atau kategori lain, atau pasang permintaan agar petani menghubungi Anda.",
                        actionLabel = "Pasang Permintaan",
                        onAction = onCreateRequest,
                    )
                }
            }

            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    seller = data.user(product.sellerId),
                    onClick = { onOpenProduct(product.id) },
                )
            }
        }
    }
}

@Composable
private fun PromoCard(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
