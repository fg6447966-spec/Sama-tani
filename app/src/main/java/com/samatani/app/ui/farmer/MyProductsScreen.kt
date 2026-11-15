@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samatani.app.data.Product
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.ProductThumb
import com.samatani.app.ui.components.SamaTopBar
import com.samatani.app.ui.components.StatusPill
import com.samatani.app.ui.theme.PillGray
import com.samatani.app.ui.theme.PillGreen
import com.samatani.app.ui.theme.PillRed
import com.samatani.app.util.formatQty
import com.samatani.app.util.formatRupiah

@Composable
fun MyProductsScreen(
    user: User,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onOpen: (String) -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val products = data.products
        .filter { it.sellerId == user.id }
        .sortedWith(compareByDescending<Product> { it.isActive }.thenByDescending { it.createdAt })
    var toDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = { SamaTopBar("Produk Saya") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Tambah Produk") },
            )
        },
    ) { padding ->
        if (products.isEmpty()) {
            EmptyState(
                emoji = "🌱",
                title = "Belum ada produk",
                message = "Tambahkan hasil panen pertama Anda agar pembeli dan distributor bisa menemukannya.",
                modifier = Modifier.padding(padding),
                actionLabel = "Tambah Produk",
                onAction = onAdd,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        "${products.count { it.isActive }} produk aktif dari ${products.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(products, key = { it.id }) { product ->
                    MyProductRow(
                        product = product,
                        onClick = { onOpen(product.id) },
                        onEdit = { onEdit(product.id) },
                        onToggle = {
                            repository.setProductActive(product.id, !product.isActive)
                            repository.showMessage(
                                if (product.isActive) "Produk disembunyikan dari pasar" else "Produk tampil lagi di pasar",
                            )
                        },
                        onDelete = { toDelete = product },
                    )
                }
            }
        }
    }

    val deleting = toDelete
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Hapus produk?") },
            text = { Text("\"${deleting.name}\" akan dihapus dari daftar jual Anda. Pesanan yang sudah ada tidak terpengaruh.") },
            confirmButton = {
                TextButton(onClick = {
                    repository.deleteProduct(deleting.id)
                    repository.showMessage("Produk dihapus")
                    toDelete = null
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun MyProductRow(
    product: Product,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductThumb(
                imagePath = product.imagePath,
                category = product.category,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp)),
                emojiSize = 32.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${formatRupiah(product.price)}/${product.unit}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Stok ${formatQty(product.stock)} ${product.unit} • Min. ${formatQty(product.minOrder)} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                when {
                    !product.isActive -> StatusPill("Disembunyikan", PillGray)
                    product.stock <= 0 -> StatusPill("Stok habis", PillRed)
                    else -> StatusPill("Tampil di pasar", PillGreen)
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Menu produk")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Ubah") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(if (product.isActive) "Sembunyikan" else "Tampilkan") },
                        leadingIcon = {
                            Icon(
                                if (product.isActive) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onToggle()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}
