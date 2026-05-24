package com.whatshappening.novisad.ui.screens.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.ui.theme.LocalCatppuccin

// ── CategoryMarker ────────────────────────────────────────────────────────────

/**
 * Circular map pin coloured by [category] hue, with a white/crust outer ring.
 *
 * The highlighted pin scales up and gains a drop shadow to visually distinguish
 * the currently focused event.
 *
 * Note: The spec's teardrop shape is built with GenericShape + rotate(-45°) which
 * is fiddly on Android (clip path + border don't compose cleanly). This v1 ships
 * a circle with a ring — visually clean, distinctive enough. The teardrop shape
 * can be added in a polish pass.
 */
@Composable
fun CategoryMarker(
    category: EventCategory,
    highlighted: Boolean,
) {
    val palette = LocalCatppuccin.current
    val isDark = isSystemInDarkTheme()
    val ringColor = if (isDark) palette.crust else Color.White

    val scale by animateFloatAsState(
        targetValue = if (highlighted) 1.25f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        label = "markerScale",
    )

    Box(
        modifier = Modifier
            .size(34.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (highlighted) 8.dp else 2.dp,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .background(category.hue(palette))
            .border(
                width = if (highlighted) 3.dp else 2.dp,
                color = ringColor,
                shape = CircleShape,
            ),
    )
}
