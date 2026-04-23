package com.whatshappening.novisad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.whatshappening.novisad.ui.EventsScreen
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhatsHappeningTheme {
                EventsScreen()
            }
        }
    }
}
