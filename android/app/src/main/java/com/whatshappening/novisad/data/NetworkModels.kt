package com.whatshappening.novisad.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire-format DTOs for the scraper JSON feed.
 * These are intentionally separate from the rich domain [Event] model.
 */
@Serializable
data class ScrapedEvent(
    val id: String,
    val title: String,
    val category: String? = null,
    val date: String,
    val time: String? = null,
    val location: String,
    val url: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class ScrapedEventsResponse(
    @SerialName("scraped_at") val scrapedAt: String,
    val events: List<ScrapedEvent>,
)
