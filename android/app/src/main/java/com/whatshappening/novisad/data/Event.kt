package com.whatshappening.novisad.data

import java.time.LocalDate
import java.time.LocalTime

/**
 * Rich domain model for an event. This is the type every new screen works with.
 * Network fetching uses [ScrapedEvent]; the scraper-backed [NetworkEventRepository] maps between them.
 */
data class Event(
    val id: String,
    val title: String,
    val category: EventCategory,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val location: String,
    val distanceKm: Double,
    val description: String,
    val organizer: String,
    val priceLabel: String,   // "Free", "€12 / €8 student", etc.
    val link: String,          // tap → open in browser
    val photoUrl: String? = null, // null falls back to gradient hero
)
