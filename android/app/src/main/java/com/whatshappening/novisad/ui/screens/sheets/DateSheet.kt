@file:OptIn(ExperimentalMaterial3Api::class)

package com.whatshappening.novisad.ui.screens.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.data.MOCK_TODAY
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.MochaPalette
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ── DateSheet ─────────────────────────────────────────────────────────────────

/**
 * Custom calendar date-range picker bottom sheet.
 *
 * Interaction:
 *  - First tap sets the start date; second tap sets the end date (swaps if before start).
 *  - Tapping when a full range is already chosen starts a fresh selection.
 *  - Cells between start and end show a semi-transparent tint band.
 *  - Applying with only a start date treats it as a single-day range.
 *
 * [eventDates] drives the event-dot markers on the calendar.
 */
@Composable
fun DateSheet(
    initialDateFrom: LocalDate?,
    initialDateTo: LocalDate?,
    onPick: (from: LocalDate, to: LocalDate) -> Unit,
    onDismiss: () -> Unit,
    eventDates: Set<LocalDate>,
) {
    var startDate   by remember { mutableStateOf(initialDateFrom) }
    var endDate     by remember { mutableStateOf(initialDateTo) }
    var displayMonth by remember {
        mutableStateOf(YearMonth.from(initialDateFrom ?: LocalDate.now()))
    }
    val today = LocalDate.now()
    val mdFormatter = DateTimeFormatter.ofPattern("MMM d")

    // Live status text shown below the title
    val statusText = when {
        startDate == null -> "Tapni dan za početak"
        endDate == null   -> "Počinje ${startDate!!.format(mdFormatter)} · tapni kraj"
        startDate == endDate -> startDate!!.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        else -> "${startDate!!.format(mdFormatter)} – ${endDate!!.format(mdFormatter)}"
    }

    // Apply button label
    val applyLabel = when {
        startDate == null -> "Odaberi početak"
        endDate == null   -> "Od ${startDate!!.format(mdFormatter)} – odaberi kraj"
        startDate == endDate -> "Primeni ${startDate!!.format(DateTimeFormatter.ofPattern("EEE, MMM d"))}"
        else -> "Primeni ${startDate!!.format(mdFormatter)} – ${endDate!!.format(mdFormatter)}"
    }

    fun handleDayClick(day: LocalDate) {
        when {
            // No selection, or range already complete → start fresh
            startDate == null || (startDate != null && endDate != null) -> {
                startDate = day
                endDate   = null
            }
            // Start set, end not set → finalise range
            day >= startDate!! -> endDate = day
            else -> {
                // Tapped before start → flip
                endDate   = startDate
                startDate = day
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { DateSheetDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Opseg datuma",
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalCatppuccin.current.subtext0,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (startDate != null) {
                        androidx.compose.material3.TextButton(
                            onClick = { startDate = null; endDate = null },
                        ) {
                            Text(
                                "Obriši",
                                style = MaterialTheme.typography.labelMedium,
                                color = LocalCatppuccin.current.subtext1,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LocalCatppuccin.current.mantle)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(18.dp),
                            tint = LocalCatppuccin.current.text,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Month navigation
            MonthNavigator(
                displayMonth = displayMonth,
                onPrevMonth = { displayMonth = displayMonth.minusMonths(1) },
                onNextMonth = { displayMonth = displayMonth.plusMonths(1) },
            )

            Spacer(Modifier.height(12.dp))

            // Weekday header
            WeekdayHeader()

            Spacer(Modifier.height(4.dp))

            // Calendar grid
            MonthGrid(
                month = displayMonth,
                today = today,
                rangeStart = startDate,
                rangeEnd   = endDate,
                eventDates = eventDates,
                onDayClick = ::handleDayClick,
            )

            Spacer(Modifier.height(20.dp))

            // Footer buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Cancel
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalCatppuccin.current.mantle,
                        contentColor = LocalCatppuccin.current.text,
                    ),
                ) {
                    Text("Otkaži", style = MaterialTheme.typography.titleMedium)
                }

                // Apply
                Button(
                    onClick = {
                        startDate?.let { from -> onPick(from, endDate ?: from) }
                    },
                    enabled = startDate != null,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = LocalCatppuccin.current.surface0,
                        disabledContentColor = LocalCatppuccin.current.subtext0,
                    ),
                ) {
                    Text(applyLabel, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── MonthNavigator ────────────────────────────────────────────────────────────

@Composable
private fun MonthNavigator(
    displayMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val palette = LocalCatppuccin.current
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.mantle)
                .clickable(onClick = onPrevMonth),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = palette.text,
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = displayMonth.format(formatter),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.mantle)
                .clickable(onClick = onNextMonth),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = palette.text,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ── WeekdayHeader ─────────────────────────────────────────────────────────────

@Composable
private fun WeekdayHeader() {
    val palette = LocalCatppuccin.current
    // Mon → Sun order (ISO week starts on Monday)
    val days = DayOfWeek.entries.map { dow ->
        dow.getDisplayName(TextStyle.NARROW, Locale.getDefault())
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        days.forEach { label ->
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.subtext0,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── MonthGrid ─────────────────────────────────────────────────────────────────

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    rangeStart: LocalDate?,
    rangeEnd: LocalDate?,
    eventDates: Set<LocalDate>,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstDayOfMonth = month.atDay(1)
    // ISO: Monday = 1, ..., Sunday = 7. Offset so Monday column = 0.
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1  // 0–6
    val daysInMonth = month.lengthOfMonth()

    // Build a flat list: nulls for leading empty cells, then day numbers
    val cells = (0 until startOffset).map { null } + (1..daysInMonth).map { it }
    // Pad to full weeks
    val remainder = cells.size % 7
    val padded = if (remainder == 0) cells else cells + List(7 - remainder) { null }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        padded.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                week.forEach { day ->
                    if (day == null) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val iso = month.atDay(day)
                        val effectiveEnd = rangeEnd ?: rangeStart
                        DayCell(
                            day = day,
                            iso = iso,
                            isEndpoint = iso == rangeStart || iso == rangeEnd,
                            inRange = rangeStart != null && effectiveEnd != null &&
                                    iso > rangeStart && iso < effectiveEnd,
                            isToday = iso == today,
                            hasEvent = iso in eventDates,
                            onClick = { onDayClick(iso) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

// ── DayCell ───────────────────────────────────────────────────────────────────

@Composable
private fun DayCell(
    day: Int,
    iso: LocalDate,
    isEndpoint: Boolean,
    inRange: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current
    val primary = MaterialTheme.colorScheme.primary
    val isDark = LocalCatppuccin.current == MochaPalette

    val cellBg = when {
        isEndpoint -> primary
        inRange    -> primary.copy(alpha = if (isDark) 0.18f else 0.14f)
        else       -> Color.Transparent
    }

    val borderModifier = if (isToday && !isEndpoint) {
        Modifier.border(1.5.dp, primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .then(borderModifier)
            .background(cellBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$day",
                color = when {
                    isEndpoint -> if (isDark) palette.crust else palette.base
                    isToday    -> primary
                    else       -> palette.text
                },
                fontWeight = if (isEndpoint || isToday) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
            )
            if (hasEvent) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isEndpoint) {
                                if (isDark) palette.crust else palette.base
                            } else {
                                primary
                            },
                            shape = CircleShape,
                        )
                )
            }
        }
    }
}

// ── DragHandle (private to DateSheet) ────────────────────────────────────────

@Composable
private fun DateSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .size(width = 44.dp, height = 4.dp)
            .background(LocalCatppuccin.current.surface1, RoundedCornerShape(2.dp)),
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "DateSheet · Light", showBackground = true)
@Composable
private fun DateSheetPreviewLight() {
    WhatsHappeningTheme {
        DateSheet(
            initialDateFrom = MOCK_TODAY,
            initialDateTo   = MOCK_TODAY.plusDays(4),
            onPick = { _, _ -> },
            onDismiss = {},
            eventDates = MOCK_EVENTS.map { it.date }.toSet(),
        )
    }
}

@Preview(
    name = "DateSheet · Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DateSheetPreviewDark() {
    WhatsHappeningTheme {
        DateSheet(
            initialDateFrom = null,
            initialDateTo   = null,
            onPick = { _, _ -> },
            onDismiss = {},
            eventDates = MOCK_EVENTS.map { it.date }.toSet(),
        )
    }
}
