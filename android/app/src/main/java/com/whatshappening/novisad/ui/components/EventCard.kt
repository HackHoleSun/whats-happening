package com.whatshappening.novisad.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.MochaPalette
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import com.whatshappening.novisad.util.formatDate
import com.whatshappening.novisad.util.formatDistance
import com.whatshappening.novisad.util.formatTime

// ── Card density ──────────────────────────────────────────────────────────────

enum class CardDensity { Comfy, Compact }

// ── EventCard ─────────────────────────────────────────────────────────────────

/**
 * The primary feed card. 168dp hero (Comfy) / 132dp (Compact), mantle background,
 * category-coloured gradient hero with faded date numeral, save heart, and a two-
 * line title followed by date/time and location/distance meta rows.
 */
@Composable
fun EventCard(
    event: Event,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    density: CardDensity = CardDensity.Comfy,
) {
    val palette   = LocalCatppuccin.current
    val darkTheme = palette == MochaPalette
    val accent    = MaterialTheme.colorScheme.primary

    // Save button press → scale-down feedback
    val saveInteraction = remember { MutableInteractionSource() }
    val isSavePressed   by saveInteraction.collectIsPressedAsState()
    val saveScale       by animateFloatAsState(
        targetValue   = if (isSavePressed) 0.92f else 1f,
        animationSpec = tween(120),
        label         = "saveScale",
    )

    val heroHeight = if (density == CardDensity.Comfy) 168.dp else 132.dp
    val bodyPad    = if (density == CardDensity.Comfy) 18.dp  else 14.dp
    val titleStyle = if (density == CardDensity.Comfy)
        MaterialTheme.typography.headlineLarge
    else
        MaterialTheme.typography.headlineMedium

    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        colors    = CardDefaults.cardColors(containerColor = palette.mantle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        // ── Hero ──────────────────────────────────────────────────────────────
        Box {
            CategoryHero(
                category     = event.category,
                date         = event.date,
                photoUrl     = event.photoUrl,
                modifier     = Modifier.fillMaxWidth().height(heroHeight),
            )

            // Category pill — top-start
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                CategoryPill(category = event.category, onContrastBg = true)
            }

            // Save heart — top-end
            val saveBg = if (darkTheme) palette.surface1 else Color.White.copy(alpha = 0.92f)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .graphicsLayer { scaleX = saveScale; scaleY = saveScale }
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(saveBg)
            ) {
                IconButton(
                    onClick           = onToggleSave,
                    interactionSource = saveInteraction,
                    modifier          = Modifier.size(38.dp),
                ) {
                    Icon(
                        imageVector        = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (saved) "Remove from saved" else "Save event",
                        tint               = if (saved) palette.red else palette.subtext0,
                        modifier           = Modifier.size(18.dp),
                    )
                }
            }
        }

        // ── Body ──────────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(bodyPad)) {
            // Title
            Text(
                text      = event.title,
                style     = titleStyle.copy(lineBreak = LineBreak.Heading),
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis,
                color     = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(10.dp))

            // Meta row 1 — date · dot · time
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = palette.subtext0,
                    )
                    Text(
                        text  = formatDate(event.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.subtext1,
                    )
                }
                Dot(palette.surface2)
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = palette.subtext0,
                    )
                    Text(
                        text  = formatTime(event.startTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.subtext1,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Meta row 2 — location · dot · distance
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier           = Modifier.size(14.dp),
                    tint               = palette.subtext0,
                )
                Text(
                    text     = event.location,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = palette.subtext0,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (event.distanceKm != null) {
                    Dot(palette.surface2)
                    Text(
                        text       = formatDistance(event.distanceKm),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = accent,
                    )
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** 3dp filled dot separator. */
@Composable
internal fun Dot(color: Color) {
    Box(
        Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val synthwave = MOCK_EVENTS.first()

@Preview(name = "Comfy · Light")
@Preview(name = "Comfy · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventCardComfyPreview() {
    WhatsHappeningTheme {
        Box(Modifier.background(LocalCatppuccin.current.base).padding(16.dp)) {
            EventCard(
                event        = synthwave,
                saved        = false,
                onClick      = {},
                onToggleSave = {},
            )
        }
    }
}

@Preview(name = "Comfy · Saved · Light")
@Preview(name = "Comfy · Saved · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventCardSavedPreview() {
    WhatsHappeningTheme {
        Box(Modifier.background(LocalCatppuccin.current.base).padding(16.dp)) {
            EventCard(
                event        = synthwave,
                saved        = true,
                onClick      = {},
                onToggleSave = {},
            )
        }
    }
}

@Preview(name = "Compact · Light")
@Preview(name = "Compact · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventCardCompactPreview() {
    WhatsHappeningTheme {
        Box(Modifier.background(LocalCatppuccin.current.base).padding(16.dp)) {
            EventCard(
                event        = synthwave,
                saved        = false,
                onClick      = {},
                onToggleSave = {},
                density      = CardDensity.Compact,
            )
        }
    }
}
