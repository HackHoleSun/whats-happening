package com.whatshappening.novisad.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
  val id: String,
  val title: String,
  val category: String? = null,
  val date: String,
  val time: String? = null,
  val location: String,
  val url: String,
)

@Serializable
data class EventsResponse(
  @SerialName("scraped_at") val scrapedAt: String,
  val events: List<Event>,
)