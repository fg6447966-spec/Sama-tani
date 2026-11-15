package com.samatani.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import com.samatani.app.data.SamaTaniRepository
import com.samatani.app.ui.auth.AuthFlow

val LocalRepository = staticCompositionLocalOf<SamaTaniRepository> {
    error("SamaTaniRepository belum disediakan")
}

/** Titik masuk UI: tampilkan alur daftar/masuk atau aplikasi utama sesuai status login. */
@Composable
fun SamaTaniRoot() {
    val repository = LocalRepository.current
    val data by repository.data.collectAsState()
    val user = data.currentUser
    if (user == null) {
        AuthFlow()
    } else {
        // Ganti akun/peran -> navigasi dibuat ulang dari awal.
        key(user.id, user.role) {
            MainFlow(user = user)
        }
    }
}
