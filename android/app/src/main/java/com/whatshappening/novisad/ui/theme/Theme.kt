package com.whatshappening.novisad.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun WhatsHappeningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: AccentChoice = AccentChoice.Mauve,
    content: @Composable () -> Unit,
) {
    val palette     = if (darkTheme) MochaPalette else LattePalette
    val accentColor = accent.hue.resolve(darkTheme)
    val scheme      = if (darkTheme) mochaColorScheme(accentColor) else latteColorScheme(accentColor)
    // Google Fonts (ui-text-google-fonts) is absent from the preview renderer's
    // classpath — accessing WhatsHappeningTypography there throws ClassNotFoundException.
    // Use the plain Material default scale for previews; the real typography at runtime.
    val typography  = if (LocalInspectionMode.current) Typography() else WhatsHappeningTypography

    // Status-bar / nav-bar icon appearance
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowInsetsControllerCompat(window, view).apply {
                isAppearanceLightStatusBars     = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalCatppuccin provides palette,
        LocalAccent     provides accent,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = typography,
            shapes      = Shapes,
            content     = content,
        )
    }
}
