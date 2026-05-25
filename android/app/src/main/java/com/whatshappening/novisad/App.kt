package com.whatshappening.novisad

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.whatshappening.novisad.data.EventRepository
import com.whatshappening.novisad.data.RemoteEventRepository
import org.maplibre.android.MapLibre
import com.whatshappening.novisad.prefs.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

/**
 * Application class — single source of truth for app-wide singletons.
 *
 * All screens share [repository] so that saved-event state is consistent
 * across Home, Map, Search, Detail, and Saved tabs.
 *
 * Registered in AndroidManifest.xml via android:name=".App".
 */
class App : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }

    // ── Shared event repository ───────────────────────────────────────────────

    val repository: EventRepository by lazy { RemoteEventRepository(this) }

    // ── User preferences DataStore ────────────────────────────────────────────

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val preferencesDataStore by lazy {
        PreferenceDataStoreFactory.create(
            scope = appScope,
            produceFile = { preferencesDataStoreFile("user_prefs") },
        )
    }

    val userPreferences: UserPreferences by lazy { UserPreferences(preferencesDataStore) }

    // ── Coil image loader ─────────────────────────────────────────────────────

    /**
     * Explicit Coil singleton so the OkHttp network fetcher is always registered.
     *
     * The custom OkHttp client adds a [Referer] header for mojnovisad.com URLs —
     * WordPress hotlink protection checks the Referer, so without this the
     * scraped thumbnails would be blocked and the list cards would show only the
     * gradient placeholder.
     */
    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(
                    callFactory = {
                        OkHttpClient.Builder()
                            .addInterceptor { chain ->
                                val request = chain.request()
                                val modified = if (request.url.host.contains("mojnovisad.com")) {
                                    request.newBuilder()
                                        .header("Referer", "https://www.mojnovisad.com/")
                                        .header("User-Agent", "Mozilla/5.0 (Android)")
                                        .build()
                                } else {
                                    request
                                }
                                chain.proceed(modified)
                            }
                            .build()
                    }
                ))
            }
            .crossfade(true)
            .build()
}
