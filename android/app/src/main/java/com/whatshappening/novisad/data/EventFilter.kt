package com.whatshappening.novisad.data

import java.time.LocalDate

// ── DateRange ─────────────────────────────────────────────────────────────────

enum class DateRange {
    Today,  // events on today's date
    Week,   // today through today + 6 days
    All,    // no date constraint
    Range,  // user picked a date range; dateFrom/dateTo are non-null
}

// ── EventFilter ───────────────────────────────────────────────────────────────

data class EventFilter(
    val range: DateRange = DateRange.All,
    /** Non-null only when [range] == [DateRange.Range]. Inclusive start date. */
    val dateFrom: LocalDate? = null,
    /** Non-null only when [range] == [DateRange.Range]. Inclusive end date. */
    val dateTo: LocalDate? = null,
    val categories: Set<EventCategory> = emptySet(),
    val searchQuery: String = "",
    /** 0–10 km radius cap. 10 means "no distance filter". */
    val maxDistanceKm: Float = 10f,
) {
    val isActive: Boolean
        get() = range != DateRange.All || categories.isNotEmpty() ||
                searchQuery.isNotBlank() || maxDistanceKm < 10f
}

// ── Filter logic ──────────────────────────────────────────────────────────────

/**
 * Pure filter function — no Android dependencies, easily testable.
 * [today] defaults to the system clock; pass [MOCK_TODAY] in previews.
 */
fun List<Event>.apply(
    filter: EventFilter,
    today: LocalDate = LocalDate.now(),
): List<Event> {
    // 1 — date range
    val byRange: List<Event> = when (filter.range) {
        DateRange.Today -> filter { it.date == today }
        DateRange.Week  -> filter { it.date in today..today.plusDays(6) }
        DateRange.Range -> {
            val from = filter.dateFrom ?: return@apply emptyList()
            val to   = filter.dateTo   ?: from
            filter { it.date in from..to }
        }
        DateRange.All   -> this
    }

    // 2 — categories
    val byCategory: List<Event> = if (filter.categories.isEmpty()) byRange
    else byRange.filter { it.category in filter.categories }

    // 3 — text search across title, location, category name
    val q = filter.searchQuery.trim().lowercase()
    val bySearch = if (q.isEmpty()) byCategory
    else byCategory.filter {
        it.title.lowercase().contains(q) ||
        it.location.lowercase().contains(q) ||
        it.category.displayName.lowercase().contains(q)
    }

    // 4 — distance cap (skip when at max = "no filter")
    // Events with no coordinates (distanceKm == null) are always included in the list.
    return if (filter.maxDistanceKm >= 10f) bySearch
    else bySearch.filter { it.distanceKm == null || it.distanceKm <= filter.maxDistanceKm }
}
