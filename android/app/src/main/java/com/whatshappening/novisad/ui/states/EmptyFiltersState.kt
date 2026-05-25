package com.whatshappening.novisad.ui.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.ui.theme.Bricolage
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── EmptyFiltersState ─────────────────────────────────────────────────────────

/**
 * Shown in [HomeScreen] when the active filter combination returns zero events.
 *
 * Layout (centred, 20dp horizontal padding):
 *  - 84dp rounded-square icon box with "∅" in Bricolage Bold
 *  - "Nothing matches those filters" headline
 *  - Helper body text
 *  - "Clear filters" pill button
 */
@Composable
fun EmptyFiltersState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current
    val primary = MaterialTheme.colorScheme.primary
    // Bricolage unavailable in the preview renderer — fall back to SansSerif
    val bricolage = if (LocalInspectionMode.current) FontFamily.SansSerif else Bricolage

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = palette.mantle,
            modifier = Modifier.size(84.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "∅",
                    color = primary,
                    fontFamily = bricolage,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Ništa ne odgovara filterima",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Proširi opseg datuma ili odaberi drugu kategoriju.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onClearFilters,
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text("Obriši filtere", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "EmptyFiltersState · Light", showBackground = true)
@Composable
private fun EmptyFiltersStatePreviewLight() {
    WhatsHappeningTheme { EmptyFiltersState(onClearFilters = {}) }
}

@Preview(
    name = "EmptyFiltersState · Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EmptyFiltersStatePreviewDark() {
    WhatsHappeningTheme { EmptyFiltersState(onClearFilters = {}) }
}
