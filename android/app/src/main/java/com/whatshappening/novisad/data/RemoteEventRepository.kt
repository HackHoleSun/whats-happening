package com.whatshappening.novisad.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Production [EventRepository] backed by the GitHub-hosted scraper feed.
 *
 * Delegates fetching + 24h file-caching to [NetworkEventRepository], then maps
 * [ScrapedEvent] → domain [Event].  Saved-event state is held in-memory.
 *
 * A [CoroutineScope] is created internally so this class is self-contained; the
 * [App] singleton keeps it alive for the process lifetime.
 */
class RemoteEventRepository(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : EventRepository {

    private val network  = NetworkEventRepository(context)

    private val _events   = MutableStateFlow<List<Event>>(emptyList())
    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        scope.launch {
            try {
                _events.value = network.getEvents().mapNotNull { it.toDomain() }
            } catch (_: Exception) {
                /* stay empty — UI will show loading/error state */
            }
        }
    }

    // ── EventRepository ───────────────────────────────────────────────────────

    override fun observeEvents(): Flow<List<Event>> = _events.asStateFlow()

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            try {
                _events.value = network.refreshEvents().mapNotNull { it.toDomain() }
            } catch (_: Exception) { /* keep stale data */ }
        }
    }

    override fun observeSavedIds(): Flow<Set<String>> = _savedIds.asStateFlow()

    override suspend fun toggleSaved(id: String) {
        _savedIds.update { ids -> if (id in ids) ids - id else ids + id }
    }

    override suspend fun fetchDetail(eventUrl: String): EventDetail? =
        runCatching { network.getEventDetail(eventUrl) }.getOrNull()
}

// ── ScrapedEvent → Event mapping ──────────────────────────────────────────────

private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

private fun ScrapedEvent.toDomain(): Event? {
    val date      = runCatching { LocalDate.parse(this.date) }.getOrNull() ?: return null
    val startTime = runCatching { LocalTime.parse(this.time ?: "0:00", timeFormatter) }
                        .getOrDefault(LocalTime.MIDNIGHT)
    return Event(
        id          = id,
        title       = title,
        category    = mapCategory(category),
        date        = date,
        startTime   = startTime,
        endTime     = startTime.plusHours(2),
        location    = location,
        distanceKm  = 0.0,
        description = "",        // populated on detail-screen open via Cloudflare Worker
        organizer   = location,
        priceLabel  = "",
        link        = url,
        photoUrl    = imageUrl,
    )
}

private fun mapCategory(raw: String?): EventCategory = when (raw?.trim()) {
    "Film"          -> EventCategory.Film
    "Izložba"       -> EventCategory.Art
    "Koncert"       -> EventCategory.Music
    "Noćni provod"  -> EventCategory.Music
    "Festival"      -> EventCategory.Music
    "Predstava"     -> EventCategory.Community
    "Radionica"     -> EventCategory.Community
    "Predavanje"    -> EventCategory.Community
    "Knjiga"        -> EventCategory.Community
    "Sport"         -> EventCategory.Sports
    else            -> EventCategory.Community
}
