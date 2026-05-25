@file:OptIn(ExperimentalMaterial3Api::class)

package com.whatshappening.novisad.ui.screens.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── CategorySheet ─────────────────────────────────────────────────────────────

/**
 * Full-screen category picker shown as a bottom sheet.
 * Renders all 9 categories in a 2-column grid of [CategoryGridCard]s.
 *
 * Draft state is local; applying commits to the caller via [onApply].
 * Dismissing without applying discards any changes.
 */
@Composable
fun CategorySheet(
    initial: Set<EventCategory>,
    onApply: (Set<EventCategory>) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(initial) { mutableStateOf(initial) }

    val applyLabel = when {
        draft.isEmpty()        -> "Prikaži sve"
        draft.size == 1        -> "Primeni 1 kategoriju"
        draft.size in 2..4     -> "Primeni ${draft.size} kategorije"
        else                   -> "Primeni ${draft.size} kategorija"
    }

    SheetScaffold(
        onDismiss = onDismiss,
        title = "Kategorije",
        titleTrailing = {
            TextButton(onClick = { draft = emptySet() }) {
                Text(
                    text = "Poništi",
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalCatppuccin.current.subtext1,
                )
            }
        },
        primaryAction = {
            Button(
                onClick = { onApply(draft); onDismiss() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(applyLabel, style = MaterialTheme.typography.titleMedium)
            }
        },
    ) {
        // 2-column grid — chunked by 2 since LazyVerticalGrid can't be inside a
        // verticalScroll, and we only have 9 items so a manual layout is fine.
        EventCategory.entries.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowCategories.forEach { cat ->
                    CategoryGridCard(
                        category = cat,
                        selected = cat in draft,
                        onClick = {
                            draft = if (cat in draft) draft - cat else draft + cat
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                // If odd number in row, fill remaining space
                if (rowCategories.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            // Row spacing
            Box(Modifier.heightIn(min = 10.dp))
        }
    }
}

// ── CategoryGridCard ──────────────────────────────────────────────────────────

@Composable
private fun CategoryGridCard(
    category: EventCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current
    val isDark = isSystemInDarkTheme()
    val bg = if (selected) category.hue(palette) else palette.mantle
    val fg = if (selected) {
        if (isDark) palette.crust else palette.base
    } else {
        palette.text
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp),
        shape = RoundedCornerShape(18.dp),
        color = bg,
    ) {
        Box {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 38dp glyph square
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) Color.White.copy(alpha = 0.25f) else category.hue(palette)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = category.glyph,
                        color = fg,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = fg,
                )
            }

            // Selection checkmark in top-right corner
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "CategorySheet · Light", showBackground = true)
@Composable
private fun CategorySheetPreviewLight() {
    WhatsHappeningTheme {
        CategorySheet(
            initial = setOf(EventCategory.Music, EventCategory.Tech),
            onApply = {},
            onDismiss = {},
        )
    }
}

@Preview(
    name = "CategorySheet · Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CategorySheetPreviewDark() {
    WhatsHappeningTheme {
        CategorySheet(
            initial = emptySet(),
            onApply = {},
            onDismiss = {},
        )
    }
}
