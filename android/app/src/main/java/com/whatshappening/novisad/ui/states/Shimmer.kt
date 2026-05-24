package com.whatshappening.novisad.ui.states

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin

// ── Shimmer ───────────────────────────────────────────────────────────────────

/**
 * A skeleton placeholder that plays an animated left-to-right sweep gradient.
 *
 * Usage:
 * ```
 * Shimmer(width = 180.dp, height = 32.dp, radius = 8.dp)
 * Shimmer(modifier = Modifier.fillMaxWidth().height(168.dp), radius = 0.dp)
 * ```
 *
 * When [width] is [Dp.Unspecified] the composable sizes itself via [modifier].
 */
@Composable
fun Shimmer(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp,
    radius: Dp = 6.dp,
) {
    val palette = LocalCatppuccin.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
        ),
        label = "shimmerOffset",
    )

    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
            .height(height)
            .clip(RoundedCornerShape(radius))
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(
                            palette.surface0,
                            palette.surface1,
                            palette.surface0,
                        ),
                        start = Offset(size.width * offset, 0f),
                        end = Offset(size.width * (offset + 1f), 0f),
                    )
                )
            },
    )
}
