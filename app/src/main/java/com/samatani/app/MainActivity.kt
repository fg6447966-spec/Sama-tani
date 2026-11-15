package com.samatani.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.samatani.app.ui.LocalRepository
import com.samatani.app.ui.SamaTaniRoot
import com.samatani.app.ui.theme.SamaTaniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bagian atas setiap layar berwarna hijau, jadi ikon status bar selalu putih.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        val repository = (application as SamaTaniApplication).repository
        setContent {
            SamaTaniTheme {
                CompositionLocalProvider(LocalRepository provides repository) {
                    SamaTaniRoot()
                }
            }
        }
    }
}
