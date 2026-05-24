package com.whatshappening.novisad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.whatshappening.novisad.ui.screens.home.HomeRoute
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import com.whatshappening.novisad.ui.theme.rememberAccent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val accent = rememberAccent()
            WhatsHappeningTheme(accent = accent) {
                HomeRoute()
            }
        }
    }
}
