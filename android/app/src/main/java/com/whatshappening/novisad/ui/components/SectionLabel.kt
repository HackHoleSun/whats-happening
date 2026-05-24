package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── SectionLabel ──────────────────────────────────────────────────────────────

/**
 * Tiny uppercase section header used throughout the app:
 * "WHEN", "CATEGORIES", "RECENT", "BROWSE BY VIBE", "ABOUT", "ORGANIZER", "PRICE".
 *
 * Renders [text] in `labelMedium` / `onSurfaceVariant`.
 */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "SectionLabel · Light")
@Preview(name = "SectionLabel · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SectionLabelPreview() {
    WhatsHappeningTheme {
        Column(
            Modifier
                .background(LocalCatppuccin.current.base)
                .padding(16.dp)
        ) {
            SectionLabel("When")
            SectionLabel("Categories")
            SectionLabel("Browse by Vibe")
        }
    }
}
