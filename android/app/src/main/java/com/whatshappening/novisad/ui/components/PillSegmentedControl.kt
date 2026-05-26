package com.whatshappening.novisad.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.MochaPalette
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── PillSegmentedControl ──────────────────────────────────────────────────────

/**
 * Pill-shaped segmented control used for "Today / This Week / All" on Home.
 *
 * [options] is a list of (id, label) pairs. The selected pill animates its
 * background from transparent to [palette.base] with a soft drop shadow.
 */
@Composable
fun PillSegmentedControl(
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette  = LocalCatppuccin.current
    val darkTheme = LocalCatppuccin.current == MochaPalette

    val shadowAmbient = if (darkTheme) Color.White.copy(0.04f) else Color.Black.copy(0.04f)
    val shadowSpot    = if (darkTheme) Color.Black.copy(0.6f)  else Color.Black.copy(0.20f)

    val pillShape = RoundedCornerShape(999.dp)

    Row(
        modifier = modifier
            .background(palette.mantle, pillShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        options.forEach { (id, label) ->
            val isSelected = id == selectedId

            // Animate background and text colour on selection change
            val bgColor by animateColorAsState(
                targetValue   = if (isSelected) palette.base else Color.Transparent,
                animationSpec = tween(200),
                label         = "pill_bg_$id",
            )
            val textColor by animateColorAsState(
                targetValue   = if (isSelected) palette.text else palette.subtext0,
                animationSpec = tween(200),
                label         = "pill_text_$id",
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation    = 4.dp,
                                shape        = pillShape,
                                clip         = false,
                                ambientColor = shadowAmbient,
                                spotColor    = shadowSpot,
                            )
                        } else Modifier
                    )
                    .clip(pillShape)
                    .background(bgColor, pillShape)
                    .clickable { onSelected(id) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val sampleOptions = listOf("today" to "Today", "week" to "This Week", "all" to "All")

@Preview(name = "PillSegmentedControl · Light")
@Preview(name = "PillSegmentedControl · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PillSegmentedControlPreview() {
    WhatsHappeningTheme {
        Box(
            Modifier
                .background(LocalCatppuccin.current.base)
                .padding(16.dp)
        ) {
            PillSegmentedControl(
                options    = sampleOptions,
                selectedId = "today",
                onSelected = {},
                modifier   = Modifier.fillMaxWidth(),
            )
        }
    }
}
