package com.whatshappening.novisad.data

import kotlinx.serialization.Serializable

@Serializable
data class EventDetail(
    val description: String,
    val imageUrl: String? = null,
)
