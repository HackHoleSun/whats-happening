package com.whatshappening.novisad.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.whatshappening.novisad.data.Event
import java.time.LocalDateTime
import java.time.ZoneId

// ── URL helpers ───────────────────────────────────────────────────────────────

/**
 * Prepends "https://" if the string doesn't already carry a scheme.
 * Handles bare domain strings like "velvet.events/synthwave-sat".
 */
fun String.ensureScheme(): String =
    if (startsWith("http://") || startsWith("https://")) this
    else "https://$this"

// ── Intent helpers ────────────────────────────────────────────────────────────

/** Opens [link] in the default browser, adding an https:// scheme if needed. */
fun openEventLink(context: Context, link: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.ensureScheme()))
    context.startActivity(intent)
}

/**
 * Opens Google Maps (or any installed maps app) for navigation to the event venue.
 *
 * - When the event has [lat]/[lng], uses a precise geo URI so the maps app
 *   drops a pin at the exact coordinates and pre-fills the venue name as label.
 * - When coordinates are absent, falls back to a name search URI so Google Maps
 *   searches for the venue name in Novi Sad.
 */
fun openMapsNavigation(context: Context, event: Event) {
    val uri: Uri = if (event.lat != null && event.lng != null) {
        // geo:lat,lng?q=lat,lng(Label) — precise pin with venue name label
        val label = Uri.encode(event.location)
        Uri.parse("geo:${event.lat},${event.lng}?q=${event.lat},${event.lng}($label)")
    } else {
        // geo:0,0?q=search+query — name-based search in Google Maps
        val query = Uri.encode("${event.location}, Novi Sad")
        Uri.parse("geo:0,0?q=$query")
    }
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

/**
 * Fires an ACTION_INSERT calendar intent pre-populated with the event's
 * title, location, description, and start/end epoch times.
 *
 * The system calendar app handles all edge cases (end < start crossing midnight
 * is intentional for late-night events and the OS lets the user adjust).
 */
fun addEventToCalendar(context: Context, event: Event) {
    val zone = ZoneId.systemDefault()

    val startMs = LocalDateTime.of(event.date, event.startTime)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

    // End time may be on the next day (e.g. a midnight-to-4am club night).
    // We keep the same date for simplicity; the user can adjust in the calendar UI.
    val endMs = LocalDateTime.of(event.date, event.endTime)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, event.title)
        putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
        putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs)
    }
    context.startActivity(intent)
}
