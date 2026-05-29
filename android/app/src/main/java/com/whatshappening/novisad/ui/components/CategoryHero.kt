package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import coil3.compose.AsyncImage
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.theme.Bricolage
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import java.time.LocalDate

/**
 * Gradient placeholder hero that doubles as an image loader.
 *
 * When [photoUrl] is non-null the gradient is visible while Coil fetches the
 * image, which then cross-fades on top.  The date numeral is hidden once a
 * photo is loaded (or call with [showDateNumeral] = false to suppress).
 *
 * Hero height is set by the caller via [modifier]:
 *   - Feed card:   168dp (Comfy) / 132dp (Compact)
 *   - Detail hero: 380dp, cornerRadius = 0.dp
 */
@Composable
fun CategoryHero(
    category: EventCategory,
    date: LocalDate,
    modifier: Modifier = Modifier,
    photoUrl: String? = null,
    cornerRadius: Dp = 0.dp,
    showDateNumeral: Boolean = true,
    numeralFontSize: TextUnit = 180.sp,
) {
    val palette    = LocalCatppuccin.current
    val stops      = category.gradientStops(palette)
    // GoogleFont classes are absent from the preview classpath — fall back to
    // the system sans-serif so the date numeral renders without crashing.
    val bricolage  = if (LocalInspectionMode.current) FontFamily.SansSerif else Bricolage

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = stops,
                    start  = Offset.Zero,
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model              = photoUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        }

        // Radial highlight overlay for depth
        val radialOverlay = remember {
            Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(80f, 120f),
                radius = 400f,
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(radialOverlay)
        )

        // Giant faded date numeral — hidden when a photo is present
        if (showDateNumeral && photoUrl == null) {
            Text(
                text  = "%02d".format(date.dayOfMonth),
                style = TextStyle(
                    fontFamily    = bricolage,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = numeralFontSize,
                    lineHeight    = numeralFontSize,
                    letterSpacing = (-8).sp,
                    color         = Color.White.copy(alpha = 0.18f),
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 18.dp, y = 32.dp),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Concert · Light")
@Preview(name = "Concert · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryHeroConcertPreview() {
    WhatsHappeningTheme {
        CategoryHero(
            category     = EventCategory.Concert,
            date         = MOCK_EVENTS.first().date,
            modifier     = Modifier.fillMaxWidth().height(168.dp),
            cornerRadius = 22.dp,
        )
    }
}

@Preview(name = "All categories · Light", widthDp = 360)
@Composable
private fun CategoryHeroAllPreview() {
    val date = LocalDate.of(2026, 5, 24)
    WhatsHappeningTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(LocalCatppuccin.current.base)
                .padding(12.dp),
        ) {
            EventCategory.entries.forEach { cat ->
                CategoryHero(
                    category     = cat,
                    date         = date,
                    cornerRadius = 16.dp,
                    modifier     = Modifier.fillMaxWidth().height(72.dp),
                )
            }
        }
    }
}
