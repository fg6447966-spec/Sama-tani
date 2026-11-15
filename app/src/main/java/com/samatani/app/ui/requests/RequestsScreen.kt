@file:OptIn(ExperimentalMaterial3Api::class)

package com.samatani.app.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samatani.app.data.BuyRequest
import com.samatani.app.data.Category
import com.samatani.app.data.RequestStatus
import com.samatani.app.data.Role
import com.samatani.app.data.User
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.components.EmptyState
import com.samatani.app.ui.components.NoteBanner
import com.samatani.app.ui.components.RequestCard
import com.samatani.app.ui.components.SamaTopBar

@Composable
fun RequestsScreen(
    user: User,
    onOpenRequest: (String) -> Unit,
    onCreateRequest: () -> Unit,
) {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val isBuyer = user.role == Role.PEMBELI
    var category by rememberSaveable { mutableStateOf<Category?>(null) }

    val filtered: List<BuyRequest> = if (isBuyer) {
        data.requests.filter { it.buyerId == user.id }
    } else {
        data.requests.filter { r ->
            r.buyerId != user.id &&
                (r.status == RequestStatus.TERBUKA || r.responses.any { it.sellerId == user.id }) &&
                (category == null || r.category == category)
        }
    }
    // Yang masih terbuka di atas, lalu yang terbaru.
    val list = filtered.sortedWith(
        compareBy<BuyRequest> { it.status != RequestStatus.TERBUKA }.thenByDescending { it.createdAt },
    )

    Scaffold(
        topBar = { SamaTopBar(if (isBuyer) "Permintaan Saya" else "Permintaan Pembeli") },
        floatingActionButton = {
            if (isBuyer) {
                ExtendedFloatingActionButton(
                    onClick = onCreateRequest,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Buat Permintaan") },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NoteBanner(
                    if (isBuyer) {
                        "Butuh pasokan rutin? Pasang permintaan, petani akan mengirim penawaran harga untuk Anda pilih."
                    } else {
                        "Pembeli dan distributor sedang mencari hasil panen berikut. Kirim penawaran terbaik Anda!"
                    },
                    emoji = if (isBuyer) "📢" else "🌾",
                )
            }
            if (!isBuyer) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = category == null,
                                onClick = { category = null },
                                label = { Text("Semua") },
                            )
                        }
                        items(Category.entries.toList()) { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = if (category == c) null else c },
                                label = { Text("${c.emoji} ${c.label}") },
                            )
                        }
                    }
                }
            }
            if (list.isEmpty()) {
                item {
                    EmptyState(
                        emoji = if (isBuyer) "📝" else "🔎",
                        title = if (isBuyer) "Belum ada permintaan" else "Belum ada permintaan yang cocok",
                        message = if (isBuyer) {
                            "Tekan \"Buat Permintaan\" untuk memberi tahu petani apa yang Anda butuhkan."
                        } else {
                            "Coba kategori lain atau cek lagi nanti."
                        },
                        actionLabel = if (isBuyer) "Buat Permintaan" else null,
                        onAction = if (isBuyer) onCreateRequest else null,
                    )
                }
            }
            items(list, key = { it.id }) { request ->
                RequestCard(
                    request = request,
                    buyer = data.user(request.buyerId),
                    viewerId = user.id,
                    onClick = { onOpenRequest(request.id) },
                )
            }
        }
    }
}
