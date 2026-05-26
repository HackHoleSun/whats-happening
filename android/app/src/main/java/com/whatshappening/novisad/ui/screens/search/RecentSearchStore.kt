package com.whatshappening.novisad.ui.screens.search

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

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
 * Returns an empty list on a fresh install — no fake seed data.
 */
class RecentSearchStore(private val ds: DataStore<Preferences>) : RecentSearchRepository {

    private val key = stringPreferencesKey("recent_searches")

    override val recent: Flow<List<String>> = ds.data.map { prefs ->
        prefs[key]?.split("\n").orEmpty().filter(String::isNotBlank)
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
 * Starts empty, just like a fresh install.
 */
class MockRecentSearchStore : RecentSearchRepository {
    private val _recent = MutableStateFlow(emptyList<String>())
    override val recent: Flow<List<String>> = _recent

    override suspend fun add(term: String) {
        _recent.update { current ->
            (listOf(term) + current.filter { it != term }).take(8)
        }
    }
}
