package com.whatshappening.novisad.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// ── Date formatters ───────────────────────────────────────────────────────────

// DateTimeFormatter construction is comparatively expensive and these run per
// visible card while scrolling — cache per pattern+locale. Formatters are
// immutable and thread-safe, so sharing is fine.
private val formatterCache = ConcurrentHashMap<String, DateTimeFormatter>()

private fun cachedFormatter(pattern: String, locale: Locale): DateTimeFormatter =
    formatterCache.getOrPut("$pattern|$locale") { DateTimeFormatter.ofPattern(pattern, locale) }

/**
 * "Sun · May 24"
 *
 * Pass [locale] explicitly in tests; defaults to the device locale at call sites.
 */
fun formatDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
    val dow = date.format(cachedFormatter("EEE", locale))   // "Sun"
    val md  = date.format(cachedFormatter("MMM d", locale)) // "May 24"
    return "$dow · $md"
}

/**
 * "May 24" — compact form used for date-range chips and labels.
 */
fun formatShortDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.format(cachedFormatter("MMM d", locale))

/**
 * "Sunday"
 */
fun formatDayOfWeek(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.format(cachedFormatter("EEEE", locale))

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

// ── Count formatter ───────────────────────────────────────────────────────────

/**
 * "1 događaj", "3 događaja", "11 događaja" — Serbian count agreement.
 * Numbers ending in 1 (except 11) take the singular; everything else the
 * genitive form, which for the words we use is the same for 2–4 and 5+.
 */
fun serbianCount(count: Int, singular: String, genitive: String): String =
    if (count % 10 == 1 && count % 100 != 11) "$count $singular" else "$count $genitive"

// ── Distance formatter ────────────────────────────────────────────────────────

/**
 * "1.2km", "0.6km"
 *
 * Always uses a decimal point regardless of locale.
 */
fun formatDistance(km: Double): String =
    String.format(Locale.US, "%.1f", km) + "km"
