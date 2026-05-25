package com.whatshappening.novisad.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── SavedEmptyState ───────────────────────────────────────────────────────────

/**
 * Shown in [SavedScreen] when the user has no bookmarked events.
 *
 * Layout (centred, 32dp horizontal padding):
 *  - 80dp rounded-square icon box (bookmark icon in primary colour)
 *  - "Nothing saved yet" headline
 *  - Helper body text
 */
@Composable
fun SavedEmptyState(modifier: Modifier = Modifier) {
    val palette = LocalCatppuccin.current
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.mantle,
            modifier = Modifier.size(80.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Još ništa nije sačuvano",
            style = MaterialTheme.typography.headlineLarge.copy(
                letterSpacing = (-0.6).sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Tapni srce na događaju da ga sačuvaš ovde.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(name = "SavedEmptyState · Light", showBackground = true)
@Composable
private fun SavedEmptyStatePreviewLight() {
    WhatsHappeningTheme { SavedEmptyState() }
}

@Preview(
    name = "SavedEmptyState · Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SavedEmptyStatePreviewDark() {
    WhatsHappeningTheme { SavedEmptyState() }
}
