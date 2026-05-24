package com.whatshappening.novisad

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val app = remember { context.applicationContext as App }
    val prefs = app.userPreferences
    val scope = rememberCoroutineScope()

    // Collect persisted preferences
    val themeOverride by prefs.themeOverride.collectAsState(initial = ThemeOverride.System)
    val accent        by prefs.accent.collectAsState(initial = AccentChoice.Mauve)

    // Resolve effective dark-mode flag
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeOverride) {
        ThemeOverride.System -> systemDark
        ThemeOverride.Light  -> false
        ThemeOverride.Dark   -> true
    }

    // Build the controller that all composables can access via LocalUserPrefs
    val controller = UserPreferencesController(
        themeOverride = rememberUpdatedState(themeOverride),
        accent        = rememberUpdatedState(accent),
        onToggleTheme = { scope.launch { prefs.toggleThemeOverride() } },
        onSetAccent   = { choice -> scope.launch { prefs.setAccent(choice) } },
    )

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
