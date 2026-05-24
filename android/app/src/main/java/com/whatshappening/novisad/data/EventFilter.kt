package com.whatshappening.novisad.data

import java.time.LocalDate

// ── DateRange ─────────────────────────────────────────────────────────────────

enum class DateRange {
    Today,    // events on today's date
    Week,     // today through today + 6 days
    All,      // no date constraint
    Specific, // user picked a calendar date; selectedDate is non-null
}

// ── EventFilter ───────────────────────────────────────────────────────────────

data class EventFilter(
    val range: DateRange = DateRange.All,
    /** Non-null only when [range] == [DateRange.Specific]. */
    val selectedDate: LocalDate? = null,
    val categories: Set<EventCategory> = emptySet(),
    val searchQuery: String = "",
) {
    val isActive: Boolean
        get() = range != DateRange.All || categories.isNotEmpty() || searchQuery.isNotBlank()
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
        DateRange.Today    -> filter { it.date == today }
        DateRange.Week     -> filter { it.date in today..today.plusDays(6) }
        DateRange.Specific -> filter { it.date == filter.selectedDate }
        DateRange.All      -> this
    }

    // 2 — categories
    val byCategory: List<Event> = if (filter.categories.isEmpty()) byRange
    else byRange.filter { it.category in filter.categories }

    // 3 — text search across title, location, category name
    val q = filter.searchQuery.trim().lowercase()
    return if (q.isEmpty()) byCategory
    else byCategory.filter {
        it.title.lowercase().contains(q) ||
        it.location.lowercase().contains(q) ||
        it.category.displayName.lowercase().contains(q)
    }
}
