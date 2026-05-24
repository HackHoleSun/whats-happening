package com.whatshappening.novisad.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
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

private val DEFAULT_CITY_CENTER = LatLng(45.2671, 19.8335) // Novi Sad

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
    userLocation: LatLng? = null,
    hasLocationPermission: Boolean = false,
    onLocateMe: () -> Unit = {},
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

    // Re-centre on the user's location the first time it becomes available
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 14f),
                durationMs = 600,
            )
        }
    }

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
                isMyLocationEnabled = hasLocationPermission,
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false, // we show our own FAB
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
            cityName = "Novi Sad",
            nearbyCount = events.size,
            onListClick = onListClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        )

        // ── 2a. Locate-me FAB ─────────────────────────────────────────────────
        Surface(
            onClick = onLocateMe,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 56.dp, end = 14.dp)
                .size(42.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = Icons.Filled.MyLocation,
                    contentDescription = "My location",
                    tint               = if (userLocation != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(20.dp),
                )
            }
        }

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
 * Returns the event's real [LatLng] when the scraper has provided coordinates,
 * otherwise falls back to a deterministic scatter around [cityCenter] so the
 * map is never empty while coordinates are being added to the data feed.
 */
private fun Event.toLatLng(cityCenter: LatLng): LatLng {
    if (lat != null && lng != null) return LatLng(lat, lng)
    // Fallback: scatter by id hash at a fixed ~500m radius so pins don't stack
    val angle = (id.hashCode().and(0xFFFF) / 65535.0) * 2 * Math.PI
    val radiusKm = 0.5
    val latOffset = (radiusKm / 111.0) * cos(angle)
    val lngOffset = (radiusKm / (111.0 * cos(Math.toRadians(cityCenter.latitude)))) * sin(angle)
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

    val locationState = rememberUserLocation()

    MapScreen(
        events               = events,
        savedIds             = savedIds,
        cityCenter           = locationState.location ?: DEFAULT_CITY_CENTER,
        userLocation         = locationState.location,
        hasLocationPermission = locationState.hasPermission,
        onLocateMe           = locationState.requestLocation,
        onEventClick         = onEventClick,
        onToggleSave         = viewModel::toggleSaved,
        onListClick          = onListClick,
        onHomeClick          = onHomeClick,
        onSavedClick         = onSavedClick,
    )
}

// ── Location helper ───────────────────────────────────────────────────────────

private data class UserLocationState(
    val location: LatLng?,
    val hasPermission: Boolean,
    val requestLocation: () -> Unit,
)

/**
 * Manages location permission and the last-known device position.
 *
 * - If permission is already granted, fetches the location immediately.
 * - Calling [UserLocationState.requestLocation] will ask for permission if needed,
 *   then fetch the location and re-centre the camera.
 */
@SuppressLint("MissingPermission")
@Composable
private fun rememberUserLocation(): UserLocationState {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var location by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    fun fetchLocation() {
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) location = LatLng(loc.latitude, loc.longitude)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) { results ->
        val granted = results.values.any { it }
        hasPermission = granted
        if (granted) fetchLocation()
    }

    // Auto-fetch if permission was already granted before this screen opened
    LaunchedEffect(Unit) {
        if (hasPermission) fetchLocation()
    }

    val requestLocation = {
        if (hasPermission) {
            fetchLocation()
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }

    return UserLocationState(location, hasPermission, requestLocation)
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
