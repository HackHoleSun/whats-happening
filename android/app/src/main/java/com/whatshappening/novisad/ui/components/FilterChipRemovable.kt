package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.MochaPalette
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── FilterChipRemovable ───────────────────────────────────────────────────────

/**
 * Accent-background removable chip shown on Home when a filter is active.
 *
 * Pill with [palette.surface0] background, optional leading [icon], a
 * [text] label (bodySmall, weight 600), and an independent 18dp circular ×
 * close button in [palette.surface1] (Latte) / [palette.surface2] (Mocha).
 */
@Composable
fun FilterChipRemovable(
    text: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    val palette  = LocalCatppuccin.current
    val darkTheme = LocalCatppuccin.current == MochaPalette
    val closeButtonBg = if (darkTheme) palette.surface2 else palette.surface1

    Row(
        modifier = modifier
            .background(palette.surface0, RoundedCornerShape(999.dp))
            .padding(top = 6.dp, bottom = 6.dp, start = 10.dp, end = 4.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Optional leading icon
        if (icon != null) {
            icon()
        }

        // Label
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = palette.text,
        )

        // × remove button — independent touch target
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(closeButtonBg),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick  = onRemove,
                modifier = Modifier.size(20.dp),
            ) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Remove filter",
                    tint               = palette.subtext1,
                    modifier           = Modifier.size(12.dp),
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "FilterChipRemovable · Light")
@Preview(name = "FilterChipRemovable · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterChipRemovablePreview() {
    WhatsHappeningTheme {
        Box(
            Modifier
                .background(LocalCatppuccin.current.base)
                .padding(16.dp)
        ) {
            FilterChipRemovable(
                text     = "Music",
                onRemove = {},
            )
        }
    }
}

@Preview(name = "FilterChipRemovable · With icon · Light")
@Preview(name = "FilterChipRemovable · With icon · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterChipRemovableWithIconPreview() {
    WhatsHappeningTheme {
        Box(
            Modifier
                .background(LocalCatppuccin.current.base)
                .padding(16.dp)
        ) {
            FilterChipRemovable(
                text     = "This Week",
                onRemove = {},
                icon = {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = LocalCatppuccin.current.subtext1,
                    )
                },
            )
        }
    }
}
