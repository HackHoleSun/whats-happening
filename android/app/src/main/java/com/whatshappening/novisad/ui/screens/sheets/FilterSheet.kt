@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.whatshappening.novisad.ui.screens.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.whatshappening.novisad.data.DateRange
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.data.EventFilter
import com.whatshappening.novisad.ui.components.SectionLabel
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── FilterSheet ───────────────────────────────────────────────────────────────

/**
 * The main filter bottom sheet. Contains three sections:
 *  - WHEN: 2×2 grid of date-range option cards
 *  - CATEGORIES: FlowRow of category toggle chips
 *  - DISTANCE: Slider capped at 10 km (≥10 = no filter); wired to EventFilter.maxDistanceKm
 *
 * Draft state is held locally so tapping dismiss cancels without committing.
 * [onOpenDatePicker] is called when the user taps "Choose date"; the caller is
 * responsible for swapping sheets while preserving the draft.
 */
@Composable
fun FilterSheet(
    initial: EventFilter,
    onApply: (EventFilter) -> Unit,
    onOpenDatePicker: () -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(initial) { mutableStateOf(initial) }

    SheetScaffold(
        onDismiss = onDismiss,
        title = "Filteri",
        titleTrailing = {
            TextButton(onClick = { draft = EventFilter() }) {
                Text(
                    text = "Obriši sve",
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
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Prikaži događaje", style = MaterialTheme.typography.titleMedium)
            }
        },
    ) {
        // ── KADA ──────────────────────────────────────────────────────────────
        SectionLabel("Kada")
        Spacer(Modifier.height(10.dp))
        WhenGrid(
            draft = draft,
            onDraftChange = { draft = it },
            onOpenDatePicker = onOpenDatePicker,
        )

        Spacer(Modifier.height(24.dp))

        // ── KATEGORIJE ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            SectionLabel("Kategorije")
            val countText = if (draft.categories.isEmpty()) "sve" else "${draft.categories.size}"
            Text(
                text = countText,
                style = MaterialTheme.typography.bodySmall,
                color = LocalCatppuccin.current.subtext0,
            )
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EventCategory.entries.forEach { cat ->
                CategoryToggleChip(
                    category = cat,
                    selected = cat in draft.categories,
                    onClick = {
                        val newCats = if (cat in draft.categories) {
                            draft.categories - cat
                        } else {
                            draft.categories + cat
                        }
                        draft = draft.copy(categories = newCats)
                    },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── UDALJENOST ────────────────────────────────────────────────────────
        SectionLabel("Udaljenost")
        Spacer(Modifier.height(10.dp))
        Slider(
            value = draft.maxDistanceKm,
            onValueChange = { draft = draft.copy(maxDistanceKm = it) },
            valueRange = 0f..10f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = LocalCatppuccin.current.surface0,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "0 km",
                style = MaterialTheme.typography.bodySmall,
                color = LocalCatppuccin.current.subtext0,
            )
            val distLabel = if (draft.maxDistanceKm >= 10f) "Sve"
                            else "Do ${draft.maxDistanceKm.toInt()} km"
            Text(
                distLabel,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = LocalCatppuccin.current.text,
            )
            Text(
                "10 km",
                style = MaterialTheme.typography.bodySmall,
                color = LocalCatppuccin.current.subtext0,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ── WhenGrid — 2×2 option grid ────────────────────────────────────────────────

@Composable
private fun WhenGrid(
    draft: EventFilter,
    onDraftChange: (EventFilter) -> Unit,
    onOpenDatePicker: () -> Unit,
) {
    val today = LocalDate.now()
    val mdFormatter = DateTimeFormatter.ofPattern("MMM d")

    // Build the four options
    data class WhenOption(val id: String, val title: String, val subtitle: String)

    val weekEnd = today.plusDays(6)
    val options = listOf(
        WhenOption(
            id = "today",
            title = "Danas",
            subtitle = today.format(mdFormatter),
        ),
        WhenOption(
            id = "week",
            title = "Ova nedelja",
            subtitle = "${today.format(mdFormatter)} – ${weekEnd.format(mdFormatter)}",
        ),
        WhenOption(
            id = "all",
            title = "Bilo kada",
            subtitle = "Bez filtera datuma",
        ),
        WhenOption(
            id = "date",
            title = "Opseg datuma",
            subtitle = when {
                draft.dateFrom != null && draft.dateTo != null && draft.dateTo != draft.dateFrom ->
                    "${draft.dateFrom.format(DateTimeFormatter.ofPattern("MMM d"))} – ${draft.dateTo.format(DateTimeFormatter.ofPattern("MMM d"))}"
                draft.dateFrom != null ->
                    draft.dateFrom.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                else -> "Odaberi opseg"
            },
        ),
    )

    val selectedId = when (draft.range) {
        DateRange.Today -> "today"
        DateRange.Week  -> "week"
        DateRange.Range -> "date"
        DateRange.All   -> "all"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { opt ->
                    WhenOptionCard(
                        title = opt.title,
                        subtitle = opt.subtitle,
                        selected = opt.id == selectedId,
                        onClick = {
                            when (opt.id) {
                                "today" -> onDraftChange(draft.copy(range = DateRange.Today, dateFrom = null, dateTo = null))
                                "week"  -> onDraftChange(draft.copy(range = DateRange.Week, dateFrom = null, dateTo = null))
                                "all"   -> onDraftChange(draft.copy(range = DateRange.All, dateFrom = null, dateTo = null))
                                "date"  -> onOpenDatePicker()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ── WhenOptionCard ────────────────────────────────────────────────────────────

@Composable
private fun WhenOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) palette.mantle else Color.Transparent,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (selected) primary else palette.surface0,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) primary else LocalCatppuccin.current.text,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.subtext0,
            )
        }
    }
}

// ── CategoryToggleChip ────────────────────────────────────────────────────────

@Composable
private fun CategoryToggleChip(
    category: EventCategory,
    selected: Boolean,
    onClick: () -> Unit,
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
        shape = RoundedCornerShape(999.dp),
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 22dp coloured/check circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.25f) else category.hue(palette)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(12.dp),
                    )
                } else {
                    Text(
                        text = category.glyph,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = fg,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "FilterSheet · Light", showBackground = true)
@Composable
private fun FilterSheetPreviewLight() {
    WhatsHappeningTheme {
        FilterSheet(
            initial = EventFilter(),
            onApply = {},
            onOpenDatePicker = {},
            onDismiss = {},
        )
    }
}

@Preview(
    name = "FilterSheet · Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FilterSheetPreviewDark() {
    WhatsHappeningTheme {
        FilterSheet(
            initial = EventFilter(
                range = DateRange.Week,
                categories = setOf(EventCategory.Music, EventCategory.Tech),
            ),
            onApply = {},
            onOpenDatePicker = {},
            onDismiss = {},
        )
    }
}
