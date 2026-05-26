package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.MochaPalette
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

/**
 * Pill-shaped category label with a coloured dot.
 *
 * [onContrastBg] = true  → white-translucent pill, used on gradient hero images.
 * [onContrastBg] = false → mantle-coloured pill, used on base-coloured surfaces.
 */
@Composable
fun CategoryPill(
    category: EventCategory,
    onContrastBg: Boolean = false,
) {
    val palette  = LocalCatppuccin.current
    val isDark   = LocalCatppuccin.current == MochaPalette
    val hue      = category.hue(palette)
    // On a hero gradient: white pill in light mode, dark pill in dark mode so the
    // text stays legible and the pill doesn't glare against the mocha background.
    val bgColor  = when {
        onContrastBg && isDark -> Color.Black.copy(alpha = 0.50f)
        onContrastBg           -> Color.White.copy(alpha = 0.92f)
        else                   -> palette.mantle
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(hue)
        )
        Text(
            text       = category.displayName.uppercase(),
            color      = palette.text,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 12.sp,
            letterSpacing = 0.2.sp,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "On contrast bg · Light")
@Preview(name = "On contrast bg · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryPillOnHeroPreview() {
    WhatsHappeningTheme {
        Box(
            Modifier
                .background(LocalCatppuccin.current.mauve)
                .padding(16.dp)
        ) {
            CategoryPill(category = EventCategory.Concert, onContrastBg = true)
        }
    }
}

@Preview(name = "On surface · Light")
@Preview(name = "On surface · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryPillOnSurfacePreview() {
    WhatsHappeningTheme {
        Box(
            Modifier
                .background(LocalCatppuccin.current.base)
                .padding(16.dp)
        ) {
            CategoryPill(category = EventCategory.Concert, onContrastBg = false)
        }
    }
}
