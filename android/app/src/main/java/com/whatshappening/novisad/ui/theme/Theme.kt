package com.whatshappening.novisad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Catppuccin Latte ──────────────────────────────────────────────────────────
private val LatteColorScheme = lightColorScheme(
  primary             = LatteMauve,
  onPrimary           = LatteBase,
  primaryContainer    = LatteSurface0,
  onPrimaryContainer  = LatteMauve,

  secondary           = LatteSapphire,
  onSecondary         = LatteBase,
  secondaryContainer  = LatteLavender,
  onSecondaryContainer = LatteBase,

  tertiary            = LattePeach,
  onTertiary          = LatteBase,
  tertiaryContainer   = LatteMantle,
  onTertiaryContainer = LattePeach,

  error               = LatteRed,
  onError             = LatteBase,
  errorContainer      = LatteMantle,
  onErrorContainer    = LatteRed,

  background          = LatteBase,
  onBackground        = LatteText,

  surface             = LatteBase,
  onSurface           = LatteText,
  surfaceVariant      = LatteSurface0,
  onSurfaceVariant    = LatteSubtext0,
  surfaceTint         = LatteMauve,

  outline             = LatteOverlay0,
  outlineVariant      = LatteSurface2,
  scrim               = Color.Black,

  inverseSurface      = MochaSurface0,
  inverseOnSurface    = MochaText,
  inversePrimary      = MochaMauve,
)

// ── Catppuccin Mocha ──────────────────────────────────────────────────────────
private val MochaColorScheme = darkColorScheme(
  primary             = MochaPeach,
  onPrimary           = MochaCrust,
  primaryContainer    = MochaSurface0,
  onPrimaryContainer  = MochaPeach,

  secondary           = MochaBlue,
  onSecondary         = MochaBase,
  secondaryContainer  = MochaPeach,
  onSecondaryContainer = MochaCrust,

  tertiary            = MochaPeach,
  onTertiary          = MochaCrust,
  tertiaryContainer   = MochaSurface2,
  onTertiaryContainer = MochaPeach,

  error               = MochaRed,
  onError             = MochaCrust,
  errorContainer      = MochaSurface1,
  onErrorContainer    = MochaRed,

  background          = MochaBase,
  onBackground        = MochaText,

  surface             = MochaBase,
  onSurface           = MochaText,
  surfaceVariant      = MochaSurface1,
  onSurfaceVariant    = MochaSubtext0,
  surfaceTint         = MochaPeach,
  surfaceContainerHigh = MochaSurface0,

  outline             = MochaOverlay1,
  outlineVariant      = MochaSurface2,
  scrim               = Color.Black,

  inverseSurface      = LatteSurface0,
  inverseOnSurface    = LatteText,
  inversePrimary      = LattePeach,
)

@Composable
fun WhatsHappeningTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) MochaColorScheme else LatteColorScheme,
    typography = Typography,
    content = content,
  )
}
