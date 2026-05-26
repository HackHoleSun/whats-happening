package com.whatshappening.novisad.prefs

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.whatshappening.novisad.ui.theme.AccentChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ── ThemeOverride ─────────────────────────────────────────────────────────────

enum class ThemeOverride { Light, Dark }

// ── UserPreferences ───────────────────────────────────────────────────────────

/**
 * Persists the user's theme override and accent colour in DataStore.
 *
 * Theme toggle: Light ↔ Dark
 */
class UserPreferences(private val ds: DataStore<Preferences>) {

    val themeOverride: Flow<ThemeOverride> = ds.data.map { prefs ->
        when (prefs[KEY_THEME]) {
            "dark" -> ThemeOverride.Dark
            else   -> ThemeOverride.Light
        }
    }

    val accent: Flow<AccentChoice> = ds.data.map { prefs ->
        runCatching {
            AccentChoice.valueOf(prefs[KEY_ACCENT] ?: AccentChoice.Mauve.name)
        }.getOrDefault(AccentChoice.Mauve)
    }

    suspend fun toggleThemeOverride() {
        ds.edit { prefs ->
            prefs[KEY_THEME] = when (prefs[KEY_THEME]) {
                "dark" -> "light"
                else   -> "dark"
            }
        }
    }

    suspend fun setAccent(choice: AccentChoice) {
        ds.edit { prefs ->
            prefs[KEY_ACCENT] = choice.name
        }
    }

    companion object {
        private val KEY_THEME  = stringPreferencesKey("theme_override")
        private val KEY_ACCENT = stringPreferencesKey("accent_choice")
    }
}

// ── UserPreferencesController ─────────────────────────────────────────────────

/**
 * Convenience holder provided via [LocalUserPrefs] so any composable can read
 * the current preferences and dispatch changes without prop-drilling.
 */
class UserPreferencesController(
    val themeOverride: State<ThemeOverride>,
    val accent: State<AccentChoice>,
    val onToggleTheme: () -> Unit,
    val onSetAccent: (AccentChoice) -> Unit,
)

val LocalUserPrefs = compositionLocalOf<UserPreferencesController> {
    error("LocalUserPrefs not provided — wrap the app in CompositionLocalProvider")
}
