package com.whatshappening.novisad.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Production [EventRepository] backed by the GitHub-hosted scraper feed.
 *
 * Delegates fetching + 24h file-caching to [NetworkEventRepository], then maps
 * [ScrapedEvent] → domain [Event].  Saved-event state persists in [dataStore]
 * so hearts survive process death.
 *
 * A [CoroutineScope] is created internally so this class is self-contained; the
 * [App] singleton keeps it alive for the process lifetime.
 */
class RemoteEventRepository(
    context: Context,
    httpClient: OkHttpClient,
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : EventRepository {

    private val network  = NetworkEventRepository(context, httpClient)

    private val _events        = MutableStateFlow<List<Event>>(emptyList())
    private val _loadAttempted = MutableStateFlow(false)

    init {
        scope.launch {
            try {
                _events.value = network.getEvents().mapNotNull { it.toDomain() }
            } catch (_: Exception) {
                /* stay empty — UI will show empty state */
            } finally {
                _loadAttempted.value = true
            }
        }
    }

    // ── EventRepository ───────────────────────────────────────────────────────

    override fun observeEvents(): Flow<List<Event>> = _events.asStateFlow()

    override fun observeLoadAttempted(): Flow<Boolean> = _loadAttempted.asStateFlow()

    /**
     * Throws when the fetch fails so callers can surface the failure; the
     * previously loaded events stay in [_events] either way.
     */
    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            _events.value = network.refreshEvents().mapNotNull { it.toDomain() }
        }
    }

    override fun observeSavedIds(): Flow<Set<String>> =
        dataStore.data.map { prefs -> prefs[KEY_SAVED_IDS] ?: emptySet() }

    override suspend fun toggleSaved(id: String) {
        dataStore.edit { prefs ->
            val ids = prefs[KEY_SAVED_IDS] ?: emptySet()
            prefs[KEY_SAVED_IDS] = if (id in ids) ids - id else ids + id
        }
    }

    override suspend fun fetchDetail(eventUrl: String): EventDetail? =
        runCatching { network.getEventDetail(eventUrl) }.getOrNull()

    private companion object {
        val KEY_SAVED_IDS = stringSetPreferencesKey("saved_event_ids")
    }
}

// ── ScrapedEvent → Event mapping ──────────────────────────────────────────────

private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

private fun ScrapedEvent.toDomain(): Event? {
    val date      = this.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
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
        // distanceKm left null — computed at runtime by HomeViewModel once GPS is available
        description = "",        // populated on detail-screen open via Cloudflare Worker
        organizer   = location,
        priceLabel  = "",
        link        = url,
        photoUrl    = imageUrl,
        lat         = lat,
        lng         = lng,
    )
}

private fun mapCategory(raw: String?): EventCategory =
    EventCategory.fromId(raw) ?: EventCategory.Performance
