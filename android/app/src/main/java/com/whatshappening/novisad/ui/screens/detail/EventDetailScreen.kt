package com.whatshappening.novisad.ui.screens.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.components.CategoryHero
import com.whatshappening.novisad.ui.components.CategoryPill
import com.whatshappening.novisad.ui.components.MetaTile
import com.whatshappening.novisad.ui.components.SectionLabel
import com.whatshappening.novisad.ui.theme.Bricolage
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import com.whatshappening.novisad.util.formatDayOfWeek
import com.whatshappening.novisad.util.formatDistance
import com.whatshappening.novisad.util.formatTimeRange
import java.time.format.DateTimeFormatter

// ── EventDetailRoute — stateful wrapper ───────────────────────────────────────

@Composable
fun EventDetailRoute(
    eventId: String,
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = viewModel(
        factory = EventDetailViewModel.factory(eventId),
    ),
) {
    val event by viewModel.event.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val context = LocalContext.current

    event?.let { ev ->
        EventDetailScreen(
            event    = ev,
            saved    = saved,
            onBack   = onBack,
            onToggleSave    = viewModel::toggleSaved,
            onShare         = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, ev.title)
                    putExtra(Intent.EXTRA_TEXT, "${ev.title} — ${ev.link.ensureScheme()}")
                }
                context.startActivity(Intent.createChooser(intent, null))
            },
            onOpenLink      = { openEventLink(context, it) },
            onAddToCalendar = { addEventToCalendar(context, ev) },
        )
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun EventDetailScreen(
    event: Event,
    saved: Boolean,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onOpenLink: (String) -> Unit,
    onAddToCalendar: () -> Unit,
) {
    val palette = LocalCatppuccin.current
    // Fall back to system sans-serif so the huge numeral renders without crashing
    // inside the Compose preview renderer (Google Fonts classes are absent there).
    val bricolage = if (LocalInspectionMode.current) FontFamily.SansSerif else Bricolage

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── 1. Category hero (photo + gradient placeholder) ──────────────────
        CategoryHero(
            category       = event.category,
            date           = event.date,
            photoUrl       = event.photoUrl,
            modifier       = Modifier
                .fillMaxWidth()
                .height(380.dp),
            showDateNumeral = false, // we render our own oversized numeral below
        )

        // ── 1a. Oversized date numeral at top-end of the hero ────────────────
        //  280sp, -16sp letter-spacing, anchored TopEnd then shifted inward.
        Box(
            Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            if (event.photoUrl == null) {
                Text(
                    text  = "%02d".format(event.date.dayOfMonth),
                    style = TextStyle(
                        fontFamily    = bricolage,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 280.sp,
                        lineHeight    = 280.sp,
                        letterSpacing = (-16).sp,
                        color         = Color.White.copy(alpha = 0.18f),
                    ),
                    modifier  = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-20).dp, y = 40.dp),
                    maxLines  = 1,
                    softWrap  = false,
                )
            }
        }

        // ── 1b. Bottom-fade scrim for legibility ─────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)),
                        startY = 380.dp.value * 0.6f,
                    )
                )
        )

        // ── 2. Floating top bar (back · share · heart) ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FloatingChip(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingChip(icon = Icons.Outlined.Share, onClick = onShare)
                FloatingChip(
                    icon  = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    tint  = if (saved) palette.red else null,
                    onClick = onToggleSave,
                )
            }
        }

        // ── 3. Scrollable content sheet ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 320.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 140.dp),
        ) {
            DragHandle()
            Spacer(Modifier.height(14.dp))
            CategoryPill(event.category, onContrastBg = false)
            Spacer(Modifier.height(14.dp))
            Text(
                text  = event.title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize      = 32.sp,
                    lineHeight    = 34.sp,
                    letterSpacing = (-1.2).sp,
                ),
                maxLines = 2,
            )
            Spacer(Modifier.height(22.dp))
            MetaTilesRow(event)
            Spacer(Modifier.height(10.dp))
            LocationCard(event)
            Spacer(Modifier.height(24.dp))
            SectionLabel("About")
            Spacer(Modifier.height(8.dp))
            Text(
                text  = event.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            OrganizerAndPrice(event)
        }

        // ── 4. Pinned bottom action bar ──────────────────────────────────────
        BottomActionBar(
            onOpenLink      = { onOpenLink(event.link) },
            onAddToCalendar = onAddToCalendar,
            modifier        = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

/**
 * 38dp translucent white circle button with a subtle drop shadow.
 * Used for back / share / heart floating over the hero.
 */
@Composable
private fun FloatingChip(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color? = null,
) {
    val palette = LocalCatppuccin.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .shadow(
                elevation  = 4.dp,
                shape      = CircleShape,
                spotColor  = Color.Black.copy(alpha = 0.18f),
            )
            .background(Color.White.copy(alpha = 0.92f), CircleShape)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = tint ?: palette.text,
            modifier           = Modifier.size(18.dp),
        )
    }
}

/**
 * 40×4 dp pill drag handle, centered.
 */
@Composable
private fun DragHandle() {
    val palette = LocalCatppuccin.current
    Box(
        modifier          = Modifier.fillMaxWidth(),
        contentAlignment  = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(palette.surface1)
        )
    }
}

/**
 * Two-up row of Date and Time [MetaTile]s.
 */
@Composable
private fun MetaTilesRow(event: Event) {
    val dateLabel  = formatDayOfWeek(event.date).uppercase()
    val dateValue  = event.date.format(DateTimeFormatter.ofPattern("d LLLL"))
    val timeValue  = formatTimeRange(event.startTime, event.endTime)

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetaTile(
            icon     = Icons.Outlined.CalendarMonth,
            label    = dateLabel,
            value    = dateValue,
            modifier = Modifier.weight(1f),
        )
        MetaTile(
            icon     = Icons.Outlined.Schedule,
            label    = "TIME",
            value    = timeValue,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Tappable location card: pin icon + venue name + distance + chevron.
 */
@Composable
private fun LocationCard(event: Event) {
    val palette      = LocalCatppuccin.current
    val primary      = MaterialTheme.colorScheme.primary
    val iconBgAlpha  = if (isSystemInDarkTheme()) 0.28f else 0.18f

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.mantle,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: launch Maps intent */ },
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Pin icon badge
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(primary.copy(alpha = iconBgAlpha)),
            ) {
                Icon(
                    imageVector        = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint               = primary,
                    modifier           = Modifier.size(22.dp),
                )
            }

            // Venue name + distance
            Column(Modifier.weight(1f)) {
                Text(
                    text  = event.location,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = "${formatDistance(event.distanceKm)} away · Tap for directions",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.subtext0,
                )
            }

            // Trailing chevron
            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = palette.subtext0,
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Two-column organizer + price section at the bottom of the sheet.
 */
@Composable
private fun OrganizerAndPrice(event: Event) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Organizer column
        Column(Modifier.weight(1f)) {
            SectionLabel("Organizer")
            Spacer(Modifier.height(4.dp))
            Text(
                text  = event.organizer,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        // Price column
        Column(Modifier.weight(1f)) {
            SectionLabel("Price")
            Spacer(Modifier.height(4.dp))
            Text(
                text  = event.priceLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Pinned bottom bar with a gradient fade so scrolled content slides under it.
 * Contains:
 * - "Open event" primary [Button] with accent drop shadow
 * - Calendar [FilledTonalIconButton]
 */
@Composable
private fun BottomActionBar(
    onOpenLink: () -> Unit,
    onAddToCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg      = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, bg, bg)))
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick  = onOpenLink,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .shadow(
                        elevation  = 12.dp,
                        shape      = RoundedCornerShape(16.dp),
                        spotColor  = primary.copy(alpha = 0.53f),
                    ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Open event", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector        = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                )
            }

            FilledTonalIconButton(
                onClick  = onAddToCalendar,
                modifier = Modifier.size(52.dp),
                shape    = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector        = Icons.Outlined.CalendarMonth,
                    contentDescription = "Add to calendar",
                    modifier           = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val synthwave = MOCK_EVENTS.first { it.id == "e1" }  // Synthwave Saturday
private val cinema    = MOCK_EVENTS.first { it.id == "e8" }  // Open-Air Cinema

@Preview(name = "Detail · Latte · Synthwave")
@Composable
private fun DetailLatteSynthwavePreview() {
    WhatsHappeningTheme(darkTheme = false) {
        EventDetailScreen(
            event           = synthwave,
            saved           = false,
            onBack          = {},
            onToggleSave    = {},
            onShare         = {},
            onOpenLink      = {},
            onAddToCalendar = {},
        )
    }
}

@Preview(name = "Detail · Mocha · Cinema", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailMochaCinemaPreview() {
    WhatsHappeningTheme(darkTheme = true) {
        EventDetailScreen(
            event           = cinema,
            saved           = false,
            onBack          = {},
            onToggleSave    = {},
            onShare         = {},
            onOpenLink      = {},
            onAddToCalendar = {},
        )
    }
}

@Preview(name = "Detail · Latte · Synthwave (saved)")
@Composable
private fun DetailLatteSynthwaveSavedPreview() {
    WhatsHappeningTheme(darkTheme = false) {
        EventDetailScreen(
            event           = synthwave,
            saved           = true,
            onBack          = {},
            onToggleSave    = {},
            onShare         = {},
            onOpenLink      = {},
            onAddToCalendar = {},
        )
    }
}
