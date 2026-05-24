package com.whatshappening.novisad

import com.whatshappening.novisad.data.DateRange
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.data.EventFilter
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.data.MOCK_TODAY
import com.whatshappening.novisad.data.apply
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventFilterTest {

    // Convenience: run filter with MOCK_TODAY as the clock anchor
    private fun List<Event>.filtered(filter: EventFilter) = apply(filter, today = MOCK_TODAY)

    // ── DateRange.Today ───────────────────────────────────────────────────────

    @Test
    fun `today filter returns only events on MOCK_TODAY`() {
        val result = MOCK_EVENTS.filtered(EventFilter(range = DateRange.Today))
        assertTrue(result.all { it.date == MOCK_TODAY })
        assertEquals(2, result.size) // e1 + e2 are on 2026-05-24
    }

    // ── DateRange.Week ────────────────────────────────────────────────────────

    @Test
    fun `week filter returns events from today through today + 6 days`() {
        val result = MOCK_EVENTS.filtered(EventFilter(range = DateRange.Week))
        val weekEnd = MOCK_TODAY.plusDays(6)
        assertTrue(result.all { it.date in MOCK_TODAY..weekEnd })
        // e1–e6 (24–28 May) fall in the week; e7 (30 May) is day 6: 24+6=30 ✓
        assertTrue(result.any { it.id == "e7" })
        // e8 (29 May) is day 5, e7 (30 May) is day 6 — both inside
        assertTrue(result.none { it.date > weekEnd })
    }

    // ── DateRange.Specific ────────────────────────────────────────────────────

    @Test
    fun `specific date filter returns only events on that date`() {
        val target = MOCK_TODAY.plusDays(1) // 2026-05-25
        val result = MOCK_EVENTS.filtered(
            EventFilter(range = DateRange.Specific, selectedDate = target)
        )
        assertTrue(result.all { it.date == target })
        assertEquals(1, result.size) // only e3 (Bauhaus)
        assertEquals("e3", result.first().id)
    }

    // ── DateRange.All ─────────────────────────────────────────────────────────

    @Test
    fun `all filter returns every event`() {
        val result = MOCK_EVENTS.filtered(EventFilter(range = DateRange.All))
        assertEquals(MOCK_EVENTS.size, result.size)
    }

    // ── Category filter ───────────────────────────────────────────────────────

    @Test
    fun `category filter returns only matching categories`() {
        val result = MOCK_EVENTS.filtered(
            EventFilter(categories = setOf(EventCategory.Music))
        )
        assertTrue(result.all { it.category == EventCategory.Music })
        assertEquals(2, result.size) // e1 + e9
    }

    @Test
    fun `multi-category filter returns union of categories`() {
        val result = MOCK_EVENTS.filtered(
            EventFilter(categories = setOf(EventCategory.Music, EventCategory.Tech))
        )
        assertTrue(result.all { it.category == EventCategory.Music || it.category == EventCategory.Tech })
        assertEquals(3, result.size) // e1, e5, e9
    }

    // ── Search query ──────────────────────────────────────────────────────────

    @Test
    fun `search query matches title (case-insensitive)`() {
        val result = MOCK_EVENTS.filtered(EventFilter(searchQuery = "synthwave"))
        assertEquals(1, result.size)
        assertEquals("e1", result.first().id)
    }

    @Test
    fun `search query matches location`() {
        val result = MOCK_EVENTS.filtered(EventFilter(searchQuery = "holzmarkt"))
        assertEquals(1, result.size)
        assertEquals("e9", result.first().id)
    }

    @Test
    fun `search query matches category display name`() {
        val result = MOCK_EVENTS.filtered(EventFilter(searchQuery = "comedy"))
        assertEquals(1, result.size)
        assertEquals("e6", result.first().id)
    }

    @Test
    fun `empty search query returns all events unfiltered`() {
        val result = MOCK_EVENTS.filtered(EventFilter(searchQuery = ""))
        assertEquals(MOCK_EVENTS.size, result.size)
    }

    // ── Combined filters ──────────────────────────────────────────────────────

    @Test
    fun `category and date range combine as AND`() {
        // Music events in the current week
        val result = MOCK_EVENTS.filtered(
            EventFilter(
                range      = DateRange.Week,
                categories = setOf(EventCategory.Music),
            )
        )
        // e1 (Music, 24 May) is in the week; e9 (Music, 1 Jun) is outside
        assertEquals(1, result.size)
        assertEquals("e1", result.first().id)
    }

    // ── isActive ──────────────────────────────────────────────────────────────

    @Test
    fun `default filter is not active`() {
        assertTrue(!EventFilter().isActive)
    }

    @Test
    fun `filter with today range is active`() {
        assertTrue(EventFilter(range = DateRange.Today).isActive)
    }

    @Test
    fun `filter with non-empty category set is active`() {
        assertTrue(EventFilter(categories = setOf(EventCategory.Art)).isActive)
    }

    @Test
    fun `filter with blank search query is not active`() {
        assertTrue(!EventFilter(searchQuery = "   ").isActive)
    }
}
