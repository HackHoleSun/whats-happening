package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import com.whatshappening.novisad.util.formatDate
import com.whatshappening.novisad.util.formatTime

// ── EventRow ──────────────────────────────────────────────────────────────────

/**
 * Compact card used in Saved, Map carousel, and Search results.
 * 84×84dp hero square with 70sp date numeral, category label row,
 * two-line title, and date/time meta.
 */
@Composable
fun EventRow(
    event: Event,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.mantle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // ── Hero thumbnail ─────────────────────────────────────────────────
            CategoryHero(
                category        = event.category,
                date            = event.date,
                photoUrl        = event.photoUrl,
                cornerRadius    = 14.dp,
                showDateNumeral = event.photoUrl == null,
                numeralFontSize = 70.sp,
                modifier        = Modifier.size(84.dp),
            )

            // ── Content column ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                // Category label — colored dot + uppercase name
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(event.category.hue(palette))
                    )
                    Text(
                        text       = event.category.displayName.uppercase(),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = event.category.hue(palette),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Title
                Text(
                    text     = event.title,
                    style    = MaterialTheme.typography.titleMedium.copy(
                        lineHeight = (MaterialTheme.typography.titleMedium.fontSize.value * 1.2f).sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color    = MaterialTheme.colorScheme.onSurface,
                )

                // Meta — date · dot · time
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (event.date != null) {
                        Text(
                            text  = formatDate(event.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.subtext0,
                        )
                        Dot(palette.surface2)
                    }
                    Text(
                        text  = formatTime(event.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.subtext0,
                    )
                }
            }

            // ── Save button ────────────────────────────────────────────────────
            IconButton(
                onClick  = onToggleSave,
                modifier = Modifier.size(36.dp),
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
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val synthwave = MOCK_EVENTS.first()

@Preview(name = "EventRow · Light")
@Preview(name = "EventRow · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventRowPreview() {
    WhatsHappeningTheme {
        Box(Modifier.background(LocalCatppuccin.current.base).padding(16.dp)) {
            EventRow(
                event        = synthwave,
                saved        = false,
                onClick      = {},
                onToggleSave = {},
            )
        }
    }
}

@Preview(name = "EventRow · Saved · Light")
@Preview(name = "EventRow · Saved · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventRowSavedPreview() {
    WhatsHappeningTheme {
        Box(Modifier.background(LocalCatppuccin.current.base).padding(16.dp)) {
            EventRow(
                event        = synthwave,
                saved        = true,
                onClick      = {},
                onToggleSave = {},
            )
        }
    }
}
