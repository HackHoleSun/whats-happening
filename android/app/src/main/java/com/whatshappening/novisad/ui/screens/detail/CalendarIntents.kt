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
