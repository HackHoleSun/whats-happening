package com.whatshappening.novisad.ui.screens.search

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Searches shown in the empty state when the user has never searched. */
private val SEED_RECENT = listOf("Synthwave", "Market", "Open air", "Vinyl")

// ── Interface ─────────────────────────────────────────────────────────────────

/**
 * Contract for persisting recent search terms.
 * Two implementations: [RecentSearchStore] (DataStore-backed) and
 * [MockRecentSearchStore] (in-memory, for previews and tests).
 */
interface RecentSearchRepository {
    val recent: Flow<List<String>>
    suspend fun add(term: String)
}

// ── DataStore-backed ──────────────────────────────────────────────────────────

/**
 * Stores up to 8 search terms in DataStore, newest-first, deduplicated on add.
 * Falls back to [SEED_RECENT] when the store is empty.
 */
class RecentSearchStore(private val ds: DataStore<Preferences>) : RecentSearchRepository {

    private val key = stringPreferencesKey("recent_searches")

    override val recent: Flow<List<String>> = ds.data.map { prefs ->
        val stored = prefs[key]?.split("\n").orEmpty().filter(String::isNotBlank)
        stored.ifEmpty { SEED_RECENT }
    }

    override suspend fun add(term: String) {
        ds.edit { prefs ->
            val current = prefs[key]?.split("\n").orEmpty().filter(String::isNotBlank)
            val updated = (listOf(term) + current.filter { it != term }).take(8)
            prefs[key] = updated.joinToString("\n")
        }
    }
}

// ── In-memory mock ────────────────────────────────────────────────────────────

/**
 * In-memory implementation for Compose previews.
 * Pre-seeded with [SEED_RECENT].
 */
class MockRecentSearchStore : RecentSearchRepository {
    private val _recent = MutableStateFlow(SEED_RECENT)
    override val recent: Flow<List<String>> = _recent

    override suspend fun add(term: String) {
        _recent.update { current ->
            (listOf(term) + current.filter { it != term }).take(8)
        }
    }
}
