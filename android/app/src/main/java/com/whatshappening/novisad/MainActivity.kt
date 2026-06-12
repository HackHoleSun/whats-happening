package com.whatshappening.novisad

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.whatshappening.novisad.nav.AppNav
import com.whatshappening.novisad.prefs.LocalUserPrefs
import com.whatshappening.novisad.prefs.ThemeOverride
import com.whatshappening.novisad.prefs.UserPreferencesController
import com.whatshappening.novisad.ui.theme.AccentChoice
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Read persisted theme/accent before the first frame so dark-theme
        // users don't get a light flash while DataStore loads. The prefs file
        // is tiny, so this blocks for single-digit milliseconds at most.
        val app = application as App
        val initialTheme  = runBlocking { app.userPreferences.themeOverride.first() }
        val initialAccent = runBlocking { app.userPreferences.accent.first() }

        setContent {
            AppRoot(initialTheme, initialAccent)
        }
    }
}

@Composable
private fun AppRoot(initialTheme: ThemeOverride, initialAccent: AccentChoice) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as App }
    val prefs = app.userPreferences
    val scope = rememberCoroutineScope()

    // Collect persisted preferences, seeded with the synchronously read values
    val themeOverride by prefs.themeOverride.collectAsState(initial = initialTheme)
    val accent        by prefs.accent.collectAsState(initial = initialAccent)

    // Resolve effective dark-mode flag
    val darkTheme = themeOverride == ThemeOverride.Dark

    // Build the controller that all composables can access via LocalUserPrefs.
    // rememberUpdatedState keeps the State instances stable, so the controller
    // itself only needs to be created once.
    val themeState  = rememberUpdatedState(themeOverride)
    val accentState = rememberUpdatedState(accent)
    val controller = remember {
        UserPreferencesController(
            themeOverride = themeState,
            accent        = accentState,
            onToggleTheme = { scope.launch { prefs.toggleThemeOverride() } },
            onSetAccent   = { choice -> scope.launch { prefs.setAccent(choice) } },
        )
    }

    CompositionLocalProvider(LocalUserPrefs provides controller) {
        WhatsHappeningTheme(darkTheme = darkTheme, accent = accent) {
            // Sync status-bar / nav-bar icon colours to match the active theme
            val view = LocalView.current
            SideEffect {
                val window = (view.context as Activity).window
                WindowInsetsControllerCompat(window, view).apply {
                    isAppearanceLightStatusBars     = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            AppNav()
        }
    }
}
