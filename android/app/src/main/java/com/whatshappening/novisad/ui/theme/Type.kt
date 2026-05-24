package com.whatshappening.novisad.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.R

// ── Google Fonts provider ─────────────────────────────────────────────────────
//
// Everything here is `lazy` so that TypeKt's <clinit> is a no-op.
// GoogleFont.Provider internally touches PackageManager / GMS which is not
// available in the Compose preview renderer — initialising at class-load time
// causes NoClassDefFoundError: Could not initialize class TypeKt.
// Lazy evaluation defers the work to the first composable access, where the
// full Android environment is present.

private val provider by lazy {
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage   = "com.google.android.gms",
        certificates      = R.array.com_google_android_gms_fonts_certs,
    )
}

// ── Font families ─────────────────────────────────────────────────────────────

val Bricolage: FontFamily by lazy {
    FontFamily(
        Font(GoogleFont("Bricolage Grotesque"), provider, FontWeight.SemiBold),
        Font(GoogleFont("Bricolage Grotesque"), provider, FontWeight.Bold),
        Font(GoogleFont("Bricolage Grotesque"), provider, FontWeight.ExtraBold),
    )
}

val InterFamily: FontFamily by lazy {
    FontFamily(
        Font(GoogleFont("Inter"), provider, FontWeight.Normal),
        Font(GoogleFont("Inter"), provider, FontWeight.Medium),
        Font(GoogleFont("Inter"), provider, FontWeight.SemiBold),
        Font(GoogleFont("Inter"), provider, FontWeight.Bold),
    )
}

// ── Typography scale ──────────────────────────────────────────────────────────

val WhatsHappeningTypography: Typography by lazy {
    Typography(
        displayLarge = TextStyle(
            fontFamily    = Bricolage,
            fontWeight    = FontWeight.Bold,
            fontSize      = 36.sp,
            lineHeight    = 38.sp,
            letterSpacing = (-1.4).sp,
        ),
        displayMedium = TextStyle(
            fontFamily    = Bricolage,
            fontWeight    = FontWeight.Bold,
            fontSize      = 30.sp,
            lineHeight    = 32.sp,
            letterSpacing = (-1.2).sp,
        ),
        displaySmall = TextStyle(
            fontFamily    = Bricolage,
            fontWeight    = FontWeight.Bold,
            fontSize      = 24.sp,
            lineHeight    = 26.sp,
            letterSpacing = (-0.8).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily    = Bricolage,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 22.sp,
            lineHeight    = 26.sp,
            letterSpacing = (-0.4).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily    = Bricolage,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 19.sp,
            lineHeight    = 23.sp,
            letterSpacing = (-0.3).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily    = Bricolage,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 16.sp,
            lineHeight    = 20.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 17.sp,
            lineHeight    = 22.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 14.sp,
            lineHeight    = 20.sp,
            letterSpacing = 0.sp,
        ),
        titleSmall = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.Medium,
            fontSize      = 12.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.Normal,
            fontSize      = 15.sp,
            lineHeight    = 23.sp,
            letterSpacing = 0.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.Normal,
            fontSize      = 13.5.sp,
            lineHeight    = 19.sp,
            letterSpacing = 0.sp,
        ),
        bodySmall = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.Medium,
            fontSize      = 12.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.sp,
        ),
        labelLarge = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 13.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.2.sp,
        ),
        labelMedium = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.Bold,
            fontSize      = 11.sp,
            lineHeight    = 14.sp,
            letterSpacing = 0.6.sp,
        ),
        labelSmall = TextStyle(
            fontFamily    = InterFamily,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 11.sp,
            lineHeight    = 14.sp,
            letterSpacing = 1.0.sp,
        ),
    )
}
