package com.whatshappening.novisad.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

// Replace GITHUB_USERNAME with your actual GitHub username after pushing the repo
private const val EVENTS_URL =
    "https://raw.githubusercontent.com/HackHoleSun/whats-happening/main/scraper/events.json"
private const val CACHE_FILE = "events_cache.json"
private const val CACHE_TTL_HOURS = 24L

class EventRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val cacheFile get() = File(context.filesDir, CACHE_FILE)

    suspend fun getEvents(): List<Event> = withContext(Dispatchers.IO) {
        val fresh = readCache(requireFresh = true)
        if (fresh != null) return@withContext fresh.events
        try {
            fetchAndCache().events
        } catch (e: Exception) {
            // Fall back to stale cache rather than showing an error
            readCache(requireFresh = false)?.events ?: throw e
        }
    }

    suspend fun refreshEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            fetchAndCache().events
        } catch (e: Exception) {
            readCache(requireFresh = false)?.events ?: throw e
        }
    }

    private fun readCache(requireFresh: Boolean): EventsResponse? {
        if (!cacheFile.exists()) return null
        return try {
            val response = json.decodeFromString<EventsResponse>(cacheFile.readText())
            if (!requireFresh) return response
            val ageHours = ChronoUnit.HOURS.between(Instant.parse(response.scrapedAt), Instant.now())
            if (ageHours < CACHE_TTL_HOURS) response else null
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchAndCache(): EventsResponse {
        val request = Request.Builder().url(EVENTS_URL).build()
        val body = client.newCall(request).execute().use { response ->
            response.body?.string() ?: error("Empty response body")
        }
        cacheFile.writeText(body)
        return json.decodeFromString(body)
    }
}
