package com.whatshappening.novisad.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

// ── Repository interface ──────────────────────────────────────────────────────

/**
 * The contract every screen uses. [MockEventRepository] satisfies it for previews;
 * a future DataStore-backed implementation satisfies it in production.
 */
interface EventRepository {
    /** Emits the current list and re-emits when data changes. */
    fun observeEvents(): Flow<List<Event>>

    /** Force-fetch fresh data from the network. */
    suspend fun refresh()

    /** Emits the set of saved event IDs; re-emits on every toggle. */
    fun observeSavedIds(): Flow<Set<String>>

    /** Adds the ID if not saved, removes it if already saved. */
    suspend fun toggleSaved(id: String)

    /**
     * Fetches full detail (description, higher-res photo) for an event via the
     * Cloudflare Worker.  Returns null if unavailable (no network, mock repo, etc.)
     */
    suspend fun fetchDetail(eventUrl: String): EventDetail?
}

// ── Network-backed implementation (existing scraper feed) ─────────────────────

private const val WORKER_URL   = "https://whats-happening-details.zeljkovic18.workers.dev"
private const val EVENTS_URL   = "https://raw.githubusercontent.com/HackHoleSun/whats-happening/master/scraper/events.json"
private const val CACHE_FILE   = "events_cache.json"
private const val CACHE_TTL_HOURS = 24L

/**
 * Fetches events from the GitHub-hosted scraper JSON and the Cloudflare Worker
 * for detail pages. This is the legacy API; new screens use [EventRepository]
 * with [MockEventRepository] until the full migration is complete.
 */
class NetworkEventRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val json   = Json { ignoreUnknownKeys = true }
    private val cacheFile get() = File(context.filesDir, CACHE_FILE)

    suspend fun getEvents(): List<ScrapedEvent> =
        withContext(Dispatchers.IO) {
            val fresh = readCache(requireFresh = true)
            if (fresh != null) return@withContext fresh.events
            try {
                fetchAndCache().events
            } catch (e: Exception) {
                readCache(requireFresh = false)?.events ?: throw e
            }
        }

    suspend fun refreshEvents(): List<ScrapedEvent> =
        withContext(Dispatchers.IO) {
            try {
                fetchAndCache().events
            } catch (e: Exception) {
                readCache(requireFresh = false)?.events ?: throw e
            }
        }

    suspend fun getEventDetail(eventUrl: String): EventDetail =
        withContext(Dispatchers.IO) {
            val url = WORKER_URL.toHttpUrl().newBuilder()
                .addQueryParameter("url", eventUrl)
                .build()
            val request = Request.Builder().url(url).build()
            val body = client.newCall(request).execute().use { response ->
                response.body?.string() ?: error("Empty response")
            }
            json.decodeFromString(body)
        }

    private fun readCache(requireFresh: Boolean): ScrapedEventsResponse? {
        if (!cacheFile.exists()) return null
        return try {
            val response = json.decodeFromString<ScrapedEventsResponse>(cacheFile.readText())
            if (!requireFresh) return response
            val ageHours = ChronoUnit.HOURS.between(Instant.parse(response.scrapedAt), Instant.now())
            if (ageHours < CACHE_TTL_HOURS) response else null
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchAndCache(): ScrapedEventsResponse {
        val request = Request.Builder().url(EVENTS_URL).build()
        val body = client.newCall(request).execute().use { response ->
            response.body?.string() ?: error("Empty response body")
        }
        cacheFile.writeText(body)
        return json.decodeFromString(body)
    }
}
