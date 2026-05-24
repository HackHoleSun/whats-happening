package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── MetaTile ──────────────────────────────────────────────────────────────────

/**
 * Detail screen's two-up Date/Time info tiles.
 *
 * Mantle-background card (18dp corner, 14dp padding) showing:
 * - A row of accent-tinted icon + uppercase [label]
 * - The [value] text below in 16sp SemiBold
 */
@Composable
fun MetaTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current
    val accent  = MaterialTheme.colorScheme.primary

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = palette.mantle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier  = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Icon + uppercase label row
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier.padding(0.dp),
                    tint               = accent,
                )
                Text(
                    text  = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Value
            Text(
                text       = value,
                style      = MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color      = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "MetaTile · Date · Light")
@Preview(name = "MetaTile · Date · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MetaTileDatePreview() {
    WhatsHappeningTheme {
        MetaTile(
            icon  = Icons.Outlined.CalendarMonth,
            label = "When",
            value = "Sat · May 24",
        )
    }
}

@Preview(name = "MetaTile · Time · Light")
@Preview(name = "MetaTile · Time · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MetaTileTimePreview() {
    WhatsHappeningTheme {
        MetaTile(
            icon  = Icons.Outlined.Schedule,
            label = "Time",
            value = "22:00 – 04:00",
        )
    }
}
