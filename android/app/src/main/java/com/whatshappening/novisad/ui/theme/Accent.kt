package com.whatshappening.novisad.ui.theme

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ── Accent model ──────────────────────────────────────────────────────────────

data class AccentHue(val latte: Color, val mocha: Color) {
    fun resolve(dark: Boolean) = if (dark) mocha else latte
}

enum class AccentChoice(val hue: AccentHue) {
    Mauve(AccentHue(LatteMauve,   MochaMauve)),
    Blue( AccentHue(LatteBlue,    MochaBlue)),
    Peach(AccentHue(LattePeach,   MochaPeach)),
    Teal( AccentHue(LatteTeal,    MochaTeal)),
    Pink( AccentHue(LattePink,    MochaPink)),
}

val LocalAccent = staticCompositionLocalOf { AccentChoice.Mauve }

// ── ViewModel ─────────────────────────────────────────────────────────────────
//
// DataStore is created inside the ViewModel (not as a file-level delegate) so
// the class initializer of AccentKt stays lightweight. The preferencesDataStore
// delegate creates a CoroutineScope(Dispatchers.IO) during <clinit>, which
// crashes the Compose preview renderer with NoClassDefFoundError.

class AccentViewModel(private val app: Application) : AndroidViewModel(app) {
    private val KEY = stringPreferencesKey("accent_choice")

    private val dataStore = PreferenceDataStoreFactory.create(
        scope       = viewModelScope,
        produceFile = { app.preferencesDataStoreFile("accent") },
    )

    val accent: StateFlow<AccentChoice> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val name = prefs[KEY] ?: AccentChoice.Mauve.name
            AccentChoice.entries.firstOrNull { it.name == name } ?: AccentChoice.Mauve
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccentChoice.Mauve)

    fun setAccent(choice: AccentChoice) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[KEY] = choice.name }
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

@Composable
fun rememberAccent(): AccentChoice {
    val vm: AccentViewModel = viewModel()
    return vm.accent.collectAsState().value
}
