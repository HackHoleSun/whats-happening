package com.whatshappening.novisad.ui.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.components.AppBottomNav
import com.whatshappening.novisad.ui.components.BottomNavDestination
import com.whatshappening.novisad.ui.components.EventRow
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import kotlin.math.sin
import kotlin.math.cos

// ── Default city center (Berlin as stand-in for the mock "Newhaven") ──────────

private val DEFAULT_CITY_CENTER = LatLng(52.516, 13.405)

// ── MapScreen ─────────────────────────────────────────────────────────────────

/**
 * Map screen showing all events as coloured category pins on a styled Google Map.
 *
 * Tapping a pin:
 *  - Scales that pin up (see [CategoryMarker])
 *  - Animates the camera to zoom in
 *  - Surfaces the event in a bottom [EventRow] card above the nav bar
 *
 * [cityCenter] defaults to the Berlin stand-in; wire a real location service or
 * city config in Chunk 10.
 */
@Composable
fun MapScreen(
    events: List<Event>,
    savedIds: Set<String>,
    cityCenter: LatLng = DEFAULT_CITY_CENTER,
    onEventClick: (Event) -> Unit,
    onToggleSave: (String) -> Unit,
    onListClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSavedClick: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cityCenter, 13f)
    }
    var focused by remember(events) { mutableStateOf(events.firstOrNull()) }

    // Animate camera when focused event changes
    LaunchedEffect(focused?.id) {
        focused?.let { ev ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(ev.toLatLng(cityCenter), 15f),
                durationMs = 600,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── 1. Map ────────────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions(if (isDark) MochaMapStyle else LatteMapStyle),
                isBuildingEnabled = false,
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false,
            ),
        ) {
            events.forEach { ev ->
                val markerState = remember(ev.id) {
                    MarkerState(position = ev.toLatLng(cityCenter))
                }
                MarkerComposable(
                    state = markerState,
                    onClick = {
                        focused = ev
                        true // consume — prevents default info window
                    },
                ) {
                    CategoryMarker(
                        category = ev.category,
                        highlighted = focused?.id == ev.id,
                    )
                }
            }
        }

        // ── 2. Frosted top bar ────────────────────────────────────────────────
        MapTopBar(
            cityName = "Newhaven",
            nearbyCount = events.size,
            onListClick = onListClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        )

        // ── 3. Focused event card ─────────────────────────────────────────────
        focused?.let { ev ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 88.dp),
            ) {
                EventRow(
                    event = ev,
                    saved = ev.id in savedIds,
                    onClick = { onEventClick(ev) },
                    onToggleSave = { onToggleSave(ev.id) },
                )
            }
        }

        // ── 4. Bottom nav ─────────────────────────────────────────────────────
        AppBottomNav(
            current = BottomNavDestination.Map,
            onHomeClick = onHomeClick,
            onMapClick = {},
            onSavedClick = onSavedClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Event.toLatLng ────────────────────────────────────────────────────────────

/**
 * Derives a deterministic [LatLng] for each event from its [Event.id] hash and
 * [Event.distanceKm]. Events without real coordinates are scattered around
 * [cityCenter] at their stated distance.
 *
 * Replace with real `latitude`/`longitude` fields on [Event] when coordinates
 * become available from the data layer.
 */
private fun Event.toLatLng(cityCenter: LatLng): LatLng {
    // Use hashCode as a seed for a deterministic angle
    val angle = (id.hashCode().and(0xFFFF) / 65535.0) * 2 * Math.PI
    // 1 degree ≈ 111km; scale distance to degrees
    val latOffset = (distanceKm / 111.0) * cos(angle)
    val lngOffset = (distanceKm / (111.0 * cos(Math.toRadians(cityCenter.latitude)))) * sin(angle)
    return LatLng(cityCenter.latitude + latOffset, cityCenter.longitude + lngOffset)
}

// ── MapTopBar ─────────────────────────────────────────────────────────────────

@Composable
private fun MapTopBar(
    cityName: String,
    nearbyCount: Int,
    onListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current

    Row(
        modifier = modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // City label pill
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = palette.base.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            border = BorderStroke(1.dp, palette.crust),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = palette.subtext0,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = palette.text)) {
                            append("$cityName · ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = palette.subtext0)) {
                            append("$nearbyCount nearby events")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // List view button
        Surface(
            onClick = onListClick,
            shape = RoundedCornerShape(22.dp),
            color = palette.base.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, palette.crust),
            modifier = Modifier.height(44.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "List",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text,
                )
            }
        }
    }
}

// ── MapRoute — stateful wrapper ───────────────────────────────────────────────

@Composable
fun MapRoute(
    onEventClick: (Event) -> Unit,
    onListClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSavedClick: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory),
) {
    val events   by viewModel.events.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    MapScreen(
        events       = events,
        savedIds     = savedIds,
        onEventClick = onEventClick,
        onToggleSave = viewModel::toggleSaved,
        onListClick  = onListClick,
        onHomeClick  = onHomeClick,
        onSavedClick = onSavedClick,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────
//
// Note: GoogleMap can't render in previews — the composable shows a placeholder.
// The preview is useful for checking the overlay layout (top bar, bottom card, nav).

@Preview(name = "MapScreen · Light", showSystemUi = true)
@Composable
private fun MapScreenPreviewLight() {
    WhatsHappeningTheme {
        MapScreen(
            events = MOCK_EVENTS,
            savedIds = setOf("e1", "e3"),
            onEventClick = {},
            onToggleSave = {},
            onListClick = {},
            onHomeClick = {},
            onSavedClick = {},
        )
    }
}

@Preview(
    name = "MapScreen · Dark",
    showSystemUi = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MapScreenPreviewDark() {
    WhatsHappeningTheme {
        MapScreen(
            events = MOCK_EVENTS,
            savedIds = emptySet(),
            onEventClick = {},
            onToggleSave = {},
            onListClick = {},
            onHomeClick = {},
            onSavedClick = {},
        )
    }
}
