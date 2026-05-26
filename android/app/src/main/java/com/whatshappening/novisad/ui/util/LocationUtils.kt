package com.whatshappening.novisad.ui.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import org.maplibre.android.geometry.LatLng

data class UserLocationState(
    val location: LatLng?,
    val hasPermission: Boolean,
    val requestLocation: () -> Unit,
)

/**
 * Shared location composable used by both HomeRoute and MapRoute.
 *
 * Requests coarse/fine location permission on first composition if not already
 * granted, then fetches [LocationServices.getFusedLocationProviderClient]
 * last-known location. The resulting [UserLocationState.location] updates once
 * the permission is confirmed and the GPS fix is available.
 */
@SuppressLint("MissingPermission")
@Composable
fun rememberUserLocation(): UserLocationState {
    val context     = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var location      by remember { mutableStateOf<LatLng?>(null) }
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

    // Passively fetch location on first composition if already permitted.
    // Do NOT auto-request permission here — that is triggered by specific UI actions
    // (opening the Map screen, or touching the distance slider).
    LaunchedEffect(Unit) {
        if (hasPermission) fetchLocation()
    }

    val requestLocation: () -> Unit = {
        if (hasPermission) fetchLocation()
        else permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    return UserLocationState(location, hasPermission, requestLocation)
}
