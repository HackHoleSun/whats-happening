package com.whatshappening.novisad.ui.states

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── LoadingScreen (skeleton) ──────────────────────────────────────────────────

/**
 * Full-screen skeleton shown while event data is loading for the first time.
 *
 * Mirrors the visual structure of [HomeScreen]:
 *  - Header area: title lines, subtitle, segmented-control row
 *  - Three skeleton event cards with image placeholder + text lines
 *
 * Uses [Shimmer] for all placeholder blocks.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val palette = LocalCatppuccin.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Skeleton header ───────────────────────────────────────────────────
        Column(
            Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp),
        ) {
            Shimmer(width = 180.dp, height = 32.dp, radius = 8.dp)
            Spacer(Modifier.height(8.dp))
            Shimmer(width = 140.dp, height = 32.dp, radius = 8.dp)
            Spacer(Modifier.height(14.dp))
            Shimmer(width = 100.dp, height = 14.dp, radius = 6.dp)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Shimmer(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    radius = 999.dp,
                )
                Shimmer(width = 42.dp, height = 42.dp, radius = 999.dp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Skeleton event cards ──────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.mantle),
                ) {
                    // Image placeholder
                    Shimmer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp),
                        radius = 0.dp,
                    )
                    // Text lines
                    Column(Modifier.padding(18.dp)) {
                        Shimmer(
                            modifier = Modifier.fillMaxWidth(0.8f).height(22.dp),
                            radius = 6.dp,
                        )
                        Spacer(Modifier.height(12.dp))
                        Shimmer(
                            modifier = Modifier.fillMaxWidth(0.6f).height(14.dp),
                            radius = 6.dp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Shimmer(
                            modifier = Modifier.fillMaxWidth(0.5f).height(14.dp),
                            radius = 6.dp,
                        )
                    }
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "LoadingScreen · Light", showSystemUi = true)
@Composable
private fun LoadingScreenPreviewLight() {
    WhatsHappeningTheme { LoadingScreen() }
}

@Preview(
    name = "LoadingScreen · Dark",
    showSystemUi = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LoadingScreenPreviewDark() {
    WhatsHappeningTheme { LoadingScreen() }
}
