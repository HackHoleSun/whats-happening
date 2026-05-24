package com.whatshappening.novisad.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Date formatters ───────────────────────────────────────────────────────────

/**
 * "Sun · May 24"
 *
 * Pass [locale] explicitly in tests; defaults to the device locale at call sites.
 */
fun formatDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
    val dow = date.format(DateTimeFormatter.ofPattern("EEE", locale))  // "Sun"
    val md  = date.format(DateTimeFormatter.ofPattern("MMM d", locale)) // "May 24"
    return "$dow · $md"
}

/**
 * "Sunday"
 */
fun formatDayOfWeek(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.format(DateTimeFormatter.ofPattern("EEEE", locale))

// ── Time formatters ───────────────────────────────────────────────────────────

private val HH_MM = DateTimeFormatter.ofPattern("HH:mm")

/**
 * "22:00"
 */
fun formatTime(time: LocalTime): String = time.format(HH_MM)

/**
 * "22:00 – 04:00"
 *
 * Uses the en-dash (–) per the design spec.
 */
fun formatTimeRange(start: LocalTime, end: LocalTime): String =
    "${start.format(HH_MM)} – ${end.format(HH_MM)}"

// ── Distance formatter ────────────────────────────────────────────────────────

/**
 * "1.2km", "0.6km"
 *
 * Always uses a decimal point regardless of locale.
 */
fun formatDistance(km: Double): String =
    String.format(Locale.US, "%.1f", km) + "km"
