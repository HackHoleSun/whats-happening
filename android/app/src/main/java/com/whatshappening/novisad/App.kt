package com.whatshappening.novisad

import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.whatshappening.novisad.data.EventRepository
import com.whatshappening.novisad.data.MockEventRepository
import com.whatshappening.novisad.prefs.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application class — single source of truth for app-wide singletons.
 *
 * All screens share [repository] so that saved-event state is consistent
 * across Home, Map, Search, Detail, and Saved tabs.
 *
 * Registered in AndroidManifest.xml via android:name=".App".
 */
class App : Application() {

    // ── Shared event repository ───────────────────────────────────────────────

    /**
     * Single repository instance. Every ViewModel obtains it via
     * [ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] so they all
     * observe the same in-memory state (toggle save on Home → visible in Saved).
     */
    val repository: EventRepository by lazy { MockEventRepository() }

    // ── User preferences DataStore ────────────────────────────────────────────

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Single DataStore instance for all user preferences (theme + accent).
     * Using [PreferenceDataStoreFactory.create] instead of the file-level
     * `preferencesDataStore` delegate avoids the static-initializer crash in
     * Compose preview renderers.
     */
    private val preferencesDataStore by lazy {
        PreferenceDataStoreFactory.create(
            scope = appScope,
            produceFile = { preferencesDataStoreFile("user_prefs") },
        )
    }

    val userPreferences: UserPreferences by lazy { UserPreferences(preferencesDataStore) }
}
