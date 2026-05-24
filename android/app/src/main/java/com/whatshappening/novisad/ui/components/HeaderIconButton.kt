package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── HeaderIconButton ──────────────────────────────────────────────────────────

/**
 * 42dp circular icon button used in screen headers and detail floating chips.
 *
 * Normal variant: [palette.mantle] background, no shadow.
 * Floating variant ([floating] = true): white-translucent 0.92α + soft shadow —
 * intended for use on top of the detail hero gradient.
 */
@Composable
fun HeaderIconButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    floating: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette   = LocalCatppuccin.current
    val darkTheme = isSystemInDarkTheme()

    val bgColor       = if (floating) Color.White.copy(alpha = 0.92f) else palette.mantle
    val shadowAmbient = if (darkTheme) Color.White.copy(0.04f) else Color.Black.copy(0.04f)
    val shadowSpot    = if (darkTheme) Color.Black.copy(0.6f)  else Color.Black.copy(0.20f)

    Box(
        modifier = modifier
            .size(42.dp)
            .then(
                if (floating) {
                    Modifier.shadow(
                        elevation    = 8.dp,
                        shape        = CircleShape,
                        clip         = false,
                        ambientColor = shadowAmbient,
                        spotColor    = shadowSpot,
                    )
                } else Modifier
            )
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick  = onClick,
            modifier = Modifier.size(42.dp),
        ) {
            content()
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "HeaderIconButton · Normal · Light")
@Preview(name = "HeaderIconButton · Normal · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HeaderIconButtonPreview() {
    WhatsHappeningTheme {
        Box(
            Modifier.background(LocalCatppuccin.current.base)
        ) {
            HeaderIconButton(
                onClick            = {},
                contentDescription = "Save",
            ) {
                Icon(
                    imageVector        = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint               = LocalCatppuccin.current.subtext0,
                )
            }
        }
    }
}

@Preview(name = "HeaderIconButton · Floating · Light")
@Preview(name = "HeaderIconButton · Floating · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HeaderIconButtonFloatingPreview() {
    WhatsHappeningTheme {
        Box(
            Modifier.background(LocalCatppuccin.current.mauve)
        ) {
            HeaderIconButton(
                onClick            = {},
                contentDescription = "Save",
                floating           = true,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint               = LocalCatppuccin.current.subtext0,
                )
            }
        }
    }
}
