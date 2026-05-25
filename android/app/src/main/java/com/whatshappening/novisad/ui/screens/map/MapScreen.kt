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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonObject
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.components.AppBottomNav
import com.whatshappening.novisad.ui.components.BottomNavDestination
import com.whatshappening.novisad.ui.components.EventRow
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import kotlin.math.cos
import kotlin.math.sin

// ── Constants ─────────────────────────────────────────────────────────────────

private val DEFAULT_CITY_CENTER = LatLng(45.2671, 19.8335) // Novi Sad

// Carto free basemaps — no API key required
private const val STYLE_LIGHT = "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
private const val STYLE_DARK  = "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"

private const val SOURCE_ID = "events"
private const val LAYER_ID  = "event-circles"

// ── Category colour map ───────────────────────────────────────────────────────

/** Catppuccin hex colour for a category, light/dark-aware. */
private fun EventCategory.hexColor(dark: Boolean): String = when (this) {
    EventCategory.Music     -> if (dark) "#CBA6F7" else "#8839EF"
    EventCategory.Food      -> if (dark) "#FAB387" else "#FE640B"
    EventCategory.Art       -> if (dark) "#F5C2E7" else "#EA76CB"
    EventCategory.Tech      -> if (dark) "#89B4FA" else "#1E66F5"
    EventCategory.Outdoor   -> if (dark) "#A6E3A1" else "#40A02B"
    EventCategory.Sports    -> if (dark) "#F38BA8" else "#D20F39"
    EventCategory.Film      -> if (dark) "#74C7EC" else "#209FB5"
    EventCategory.Comedy    -> if (dark) "#F9E2AF" else "#DF8E1D"
    EventCategory.Community -> if (dark) "#94E2D5" else "#179299"
}

// ── MapScreen ─────────────────────────────────────────────────────────────────

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

    var focusedId by remember(events) { mutableStateOf(events.firstOrNull()?.id) }
    val focused = events.firstOrNull { it.id == focusedId }

    // Async map references — set when style finishes loading
    var mapRef   by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleRef by remember { mutableStateOf<Style?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {

        // ── 1. MapLibre map view ──────────────────────────────────────────────
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            isDark   = isDark,
            onReady  = { map, style ->
                mapRef   = map
                styleRef = style
                // Pin-click: query the circle layer at the tapped point
                map.addOnMapClickListener { latlng ->
                    val screen = map.projection.toScreenLocation(latlng)
                    val hits   = map.queryRenderedFeatures(screen, LAYER_ID)
                    if (hits.isNotEmpty()) {
                        hits.first().getStringProperty("id")?.let { focusedId = it }
                        true
                    } else false
                }
            },
        )

        // ── 2. Sync pins whenever events / focused / style change ─────────────
        SyncPins(
            map      = mapRef,
            style    = styleRef,
            events   = events,
            focusedId = focusedId,
            isDark   = isDark,
            cityCenter = cityCenter,
        )

        // ── 3. Re-centre on user location once it arrives ─────────────────────
        LaunchedEffect(userLocation) {
            userLocation?.let {
                mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 14.0), 600)
            }
        }

        // ── 4. Fly to focused event ───────────────────────────────────────────
        LaunchedEffect(focusedId) {
            focused?.let { ev ->
                mapRef?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(ev.toMapLatLng(cityCenter), 15.0),
                    600,
                )
            }
        }

        // ── 5. Top bar ────────────────────────────────────────────────────────
        MapTopBar(
            cityName    = "Novi Sad",
            nearbyCount = events.size,
            onListClick = onListClick,
            modifier    = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        )

        // ── 6. Locate-me FAB ──────────────────────────────────────────────────
        Surface(
            onClick        = onLocateMe,
            shape          = CircleShape,
            color          = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier       = Modifier
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
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // ── 7. Focused event card ─────────────────────────────────────────────
        focused?.let { ev ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 88.dp),
            ) {
                EventRow(
                    event         = ev,
                    saved         = ev.id in savedIds,
                    onClick       = { onEventClick(ev) },
                    onToggleSave  = { onToggleSave(ev.id) },
                )
            }
        }

        // ── 8. Bottom nav ─────────────────────────────────────────────────────
        AppBottomNav(
            current      = BottomNavDestination.Map,
            onHomeClick  = onHomeClick,
            onMapClick   = {},
            onSavedClick = onSavedClick,
            modifier     = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── MapLibreView — lifecycle-aware AndroidView wrapper ────────────────────────

@Composable
private fun MapLibreView(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    onReady: (MapLibreMap, Style) -> Unit,
) {
    val context   = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // rememberUpdatedState so the callback always sees the latest lambda
    val currentOnReady by rememberUpdatedState(onReady)

    val mapView = remember {
        MapView(context).apply {
            getMapAsync { map ->
                val styleUrl = if (isDark) STYLE_DARK else STYLE_LIGHT
                map.setStyle(styleUrl) { style ->
                    map.uiSettings.isCompassEnabled    = false
                    map.uiSettings.isLogoEnabled       = false
                    map.uiSettings.isAttributionEnabled = true   // keep for tile licensing
                    map.cameraPosition = CameraPosition.Builder()
                        .target(DEFAULT_CITY_CENTER)
                        .zoom(13.0)
                        .build()
                    currentOnReady(map, style)
                }
            }
        }
    }

    // Forward Compose lifecycle events to MapView
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

// ── SyncPins — keeps the GeoJSON source up to date ───────────────────────────

@Composable
private fun SyncPins(
    map: MapLibreMap?,
    style: Style?,
    events: List<Event>,
    focusedId: String?,
    isDark: Boolean,
    cityCenter: LatLng,
) {
    LaunchedEffect(style, events, focusedId, isDark) {
        val s = style ?: return@LaunchedEffect

        // Build a FeatureCollection — one Point per event
        val features = events.map { ev ->
            val pos = ev.toMapLatLng(cityCenter)
            val props = JsonObject().apply {
                addProperty("id",      ev.id)
                addProperty("color",   ev.category.hexColor(isDark))
                addProperty("focused", ev.id == focusedId)
            }
            Feature.fromGeometry(Point.fromLngLat(pos.longitude, pos.latitude), props)
        }
        val collection = FeatureCollection.fromFeatures(features)

        val existing = s.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (existing == null) {
            // First load: add source + styled circle layer
            s.addSource(GeoJsonSource(SOURCE_ID, collection))
            s.addLayer(
                CircleLayer(LAYER_ID, SOURCE_ID).withProperties(
                    // Focused pin: 14px, normal: 10px
                    PropertyFactory.circleRadius(
                        Expression.switchCase(
                            Expression.toBool(Expression.get("focused")),
                            Expression.literal(14f),
                            Expression.literal(10f),
                        )
                    ),
                    // Colour from the GeoJSON "color" property
                    PropertyFactory.circleColor(
                        Expression.toColor(Expression.get("color"))
                    ),
                    // White ring; thicker on focused
                    PropertyFactory.circleStrokeWidth(
                        Expression.switchCase(
                            Expression.toBool(Expression.get("focused")),
                            Expression.literal(3f),
                            Expression.literal(2f),
                        )
                    ),
                    PropertyFactory.circleStrokeColor("#FFFFFF"),
                    // Focused pin always renders on top
                    PropertyFactory.circleSortKey(
                        Expression.switchCase(
                            Expression.toBool(Expression.get("focused")),
                            Expression.literal(1f),
                            Expression.literal(0f),
                        )
                    ),
                )
            )
        } else {
            // Subsequent updates: just swap the data
            existing.setGeoJson(collection)
        }
    }
}

// ── Event.toMapLatLng ─────────────────────────────────────────────────────────

/**
 * Returns the event's real [LatLng] when the scraper provided coordinates,
 * or a deterministic 500m scatter around [cityCenter] so the map is never empty.
 */
private fun Event.toMapLatLng(cityCenter: LatLng): LatLng {
    if (lat != null && lng != null) return LatLng(lat, lng)
    val angle     = (id.hashCode().and(0xFFFF) / 65535.0) * 2 * Math.PI
    val radiusKm  = 0.5
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
        modifier              = modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // City + count pill
        Surface(
            shape          = RoundedCornerShape(22.dp),
            color          = palette.base.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            border         = BorderStroke(1.dp, palette.crust),
            modifier       = Modifier.weight(1f).height(44.dp),
        ) {
            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector        = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint               = palette.subtext0,
                    modifier           = Modifier.size(18.dp),
                )
                Text(
                    text  = buildAnnotatedString {
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

        // List view toggle
        Surface(
            onClick        = onListClick,
            shape          = RoundedCornerShape(22.dp),
            color          = palette.base.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            border         = BorderStroke(1.dp, palette.crust),
            modifier       = Modifier.height(44.dp),
        ) {
            Box(
                modifier        = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "List",
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

@SuppressLint("MissingPermission")
@Composable
private fun rememberUserLocation(): UserLocationState {
    val context     = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var location     by remember { mutableStateOf<LatLng?>(null) }
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

    LaunchedEffect(Unit) { if (hasPermission) fetchLocation() }

    val requestLocation: () -> Unit = {
        if (hasPermission) fetchLocation()
        else permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    return UserLocationState(location, hasPermission, requestLocation)
}

// ── Previews ──────────────────────────────────────────────────────────────────
// MapLibre can't render in the Preview canvas, but overlays (top bar, card,
// nav) are still useful to check layout. The map area will show as blank.

@Preview(name = "MapScreen · Light", showSystemUi = true)
@Composable
private fun MapScreenPreviewLight() {
    WhatsHappeningTheme {
        MapScreen(
            events       = MOCK_EVENTS,
            savedIds     = setOf("e1", "e3"),
            onEventClick = {},
            onToggleSave = {},
            onListClick  = {},
            onHomeClick  = {},
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
            events       = MOCK_EVENTS,
            savedIds     = emptySet(),
            onEventClick = {},
            onToggleSave = {},
            onListClick  = {},
            onHomeClick  = {},
            onSavedClick = {},
        )
    }
}
