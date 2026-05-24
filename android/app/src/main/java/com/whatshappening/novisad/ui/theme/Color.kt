package com.whatshappening.novisad.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ── Catppuccin Latte (light) ──────────────────────────────────────────────────
val LatteRosewater = Color(0xFFDC8A78)
val LatteFlamingo  = Color(0xFFDD7878)
val LattePink      = Color(0xFFEA76CB)
val LatteMauve     = Color(0xFF8839EF)
val LatteRed       = Color(0xFFD20F39)
val LatteMaroon    = Color(0xFFE64553)
val LattePeach     = Color(0xFFFE640B)
val LatteYellow    = Color(0xFFDF8E1D)
val LatteGreen     = Color(0xFF40A02B)
val LatteTeal      = Color(0xFF179299)
val LatteSky       = Color(0xFF04A5E5)
val LatteSapphire  = Color(0xFF209FB5)
val LatteBlue      = Color(0xFF1E66F5)
val LatteLavender  = Color(0xFF7287FD)
val LatteText      = Color(0xFF4C4F69)
val LatteSubtext1  = Color(0xFF5C5F77)
val LatteSubtext0  = Color(0xFF6C6F85)
val LatteOverlay2  = Color(0xFF7C7F93)
val LatteOverlay1  = Color(0xFF8C8FA1)
val LatteOverlay0  = Color(0xFF9CA0B0)
val LatteSurface2  = Color(0xFFACB0BE)
val LatteSurface1  = Color(0xFFBCC0CC)
val LatteSurface0  = Color(0xFFCCD0DA)
val LatteBase      = Color(0xFFEFF1F5)
val LatteMantle    = Color(0xFFE6E9EF)
val LatteCrust     = Color(0xFFDCE0E8)

// ── Catppuccin Mocha (dark) ───────────────────────────────────────────────────
val MochaRosewater = Color(0xFFF5E0DC)
val MochaFlamingo  = Color(0xFFF2CDCD)
val MochaPink      = Color(0xFFF5C2E7)
val MochaMauve     = Color(0xFFCBA6F7)
val MochaRed       = Color(0xFFF38BA8)
val MochaMaroon    = Color(0xFFEBA0AC)
val MochaPeach     = Color(0xFFFAB387)
val MochaYellow    = Color(0xFFF9E2AF)
val MochaGreen     = Color(0xFFA6E3A1)
val MochaTeal      = Color(0xFF94E2D5)
val MochaSky       = Color(0xFF89DCEB)
val MochaSapphire  = Color(0xFF74C7EC)
val MochaBlue      = Color(0xFF89B4FA)
val MochaLavender  = Color(0xFFB4BEFE)
val MochaText      = Color(0xFFCDD6F4)
val MochaSubtext1  = Color(0xFFBAC2DE)
val MochaSubtext0  = Color(0xFFA6ADC8)
val MochaOverlay2  = Color(0xFF9399B2)
val MochaOverlay1  = Color(0xFF7F849C)
val MochaOverlay0  = Color(0xFF6C7086)
val MochaSurface2  = Color(0xFF585B70)
val MochaSurface1  = Color(0xFF45475A)
val MochaSurface0  = Color(0xFF313244)
val MochaBase      = Color(0xFF1E1E2E)
val MochaMantle    = Color(0xFF181825)
val MochaCrust     = Color(0xFF11111B)

// ── Extended palette container ────────────────────────────────────────────────

@Immutable
data class CatppuccinPalette(
    val rosewater: Color, val flamingo: Color, val pink: Color,
    val mauve: Color, val red: Color, val maroon: Color,
    val peach: Color, val yellow: Color, val green: Color,
    val teal: Color, val sky: Color, val sapphire: Color,
    val blue: Color, val lavender: Color,
    val text: Color, val subtext1: Color, val subtext0: Color,
    val overlay2: Color, val overlay1: Color, val overlay0: Color,
    val surface2: Color, val surface1: Color, val surface0: Color,
    val base: Color, val mantle: Color, val crust: Color,
)

val LattePalette = CatppuccinPalette(
    rosewater = LatteRosewater, flamingo = LatteFlamingo, pink = LattePink,
    mauve = LatteMauve, red = LatteRed, maroon = LatteMaroon,
    peach = LattePeach, yellow = LatteYellow, green = LatteGreen,
    teal = LatteTeal, sky = LatteSky, sapphire = LatteSapphire,
    blue = LatteBlue, lavender = LatteLavender,
    text = LatteText, subtext1 = LatteSubtext1, subtext0 = LatteSubtext0,
    overlay2 = LatteOverlay2, overlay1 = LatteOverlay1, overlay0 = LatteOverlay0,
    surface2 = LatteSurface2, surface1 = LatteSurface1, surface0 = LatteSurface0,
    base = LatteBase, mantle = LatteMantle, crust = LatteCrust,
)

val MochaPalette = CatppuccinPalette(
    rosewater = MochaRosewater, flamingo = MochaFlamingo, pink = MochaPink,
    mauve = MochaMauve, red = MochaRed, maroon = MochaMaroon,
    peach = MochaPeach, yellow = MochaYellow, green = MochaGreen,
    teal = MochaTeal, sky = MochaSky, sapphire = MochaSapphire,
    blue = MochaBlue, lavender = MochaLavender,
    text = MochaText, subtext1 = MochaSubtext1, subtext0 = MochaSubtext0,
    overlay2 = MochaOverlay2, overlay1 = MochaOverlay1, overlay0 = MochaOverlay0,
    surface2 = MochaSurface2, surface1 = MochaSurface1, surface0 = MochaSurface0,
    base = MochaBase, mantle = MochaMantle, crust = MochaCrust,
)

val LocalCatppuccin = staticCompositionLocalOf<CatppuccinPalette> {
    error("CatppuccinPalette not provided")
}

// ── M3 colour schemes ─────────────────────────────────────────────────────────

fun latteColorScheme(accent: Color) = lightColorScheme(
    primary                  = accent,
    onPrimary                = LatteBase,
    primaryContainer         = lerp(LatteMantle, accent, 0.18f),
    onPrimaryContainer       = LatteText,
    secondary                = LatteLavender,
    onSecondary              = LatteBase,
    secondaryContainer       = lerp(LatteMantle, LatteLavender, 0.18f),
    onSecondaryContainer     = LatteText,
    tertiary                 = LattePeach,
    onTertiary               = LatteBase,
    tertiaryContainer        = lerp(LatteMantle, LattePeach, 0.18f),
    onTertiaryContainer      = LatteText,
    error                    = LatteRed,
    onError                  = LatteBase,
    errorContainer           = lerp(LatteMantle, LatteRed, 0.12f),
    onErrorContainer         = LatteRed,
    background               = LatteBase,
    onBackground             = LatteText,
    surface                  = LatteBase,
    onSurface                = LatteText,
    surfaceVariant           = LatteMantle,
    onSurfaceVariant         = LatteSubtext1,
    surfaceContainer         = LatteMantle,
    surfaceContainerHigh     = LatteCrust,
    surfaceContainerHighest  = LatteSurface0,
    surfaceContainerLow      = lerp(LatteBase, LatteMantle, 0.5f),
    surfaceContainerLowest   = LatteBase,
    surfaceBright            = LatteSurface0,
    surfaceDim               = LatteCrust,
    outline                  = LatteOverlay1,
    outlineVariant           = LatteSurface1,
    scrim                    = Color.Black,
    inverseSurface           = MochaBase,
    inverseOnSurface         = MochaText,
    inversePrimary           = MochaMauve,
)

fun mochaColorScheme(accent: Color) = darkColorScheme(
    primary                  = accent,
    onPrimary                = MochaCrust,
    primaryContainer         = lerp(MochaSurface0, accent, 0.28f),
    onPrimaryContainer       = MochaText,
    secondary                = MochaLavender,
    onSecondary              = MochaCrust,
    secondaryContainer       = lerp(MochaSurface1, MochaLavender, 0.2f),
    onSecondaryContainer     = MochaText,
    tertiary                 = MochaPeach,
    onTertiary               = MochaCrust,
    tertiaryContainer        = lerp(MochaSurface0, MochaPeach, 0.2f),
    onTertiaryContainer      = MochaText,
    error                    = MochaRed,
    onError                  = MochaCrust,
    errorContainer           = lerp(MochaSurface0, MochaRed, 0.2f),
    onErrorContainer         = MochaRed,
    background               = MochaBase,
    onBackground             = MochaText,
    surface                  = MochaBase,
    onSurface                = MochaText,
    surfaceVariant           = MochaMantle,
    onSurfaceVariant         = MochaSubtext1,
    surfaceContainer         = MochaMantle,
    surfaceContainerHigh     = MochaSurface0,
    surfaceContainerHighest  = MochaSurface1,
    surfaceContainerLow      = lerp(MochaBase, MochaMantle, 0.5f),
    surfaceContainerLowest   = MochaCrust,
    surfaceBright            = MochaSurface1,
    surfaceDim               = MochaMantle,
    outline                  = MochaOverlay0,
    outlineVariant           = MochaSurface1,
    scrim                    = Color.Black,
    inverseSurface           = LatteBase,
    inverseOnSurface         = LatteText,
    inversePrimary           = LatteMauve,
)
