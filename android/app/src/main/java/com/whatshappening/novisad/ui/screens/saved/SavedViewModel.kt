package com.whatshappening.novisad.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.whatshappening.novisad.App
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ── SavedViewModel ────────────────────────────────────────────────────────────

/**
 * Provides the Saved screen with:
 *  - [events] — only the events whose IDs are in the saved set, live-updated
 *  - [savedIds] — the raw ID set (passed down so [EventRow] can render the heart correctly)
 */
class SavedViewModel(
    private val repo: EventRepository,
) : ViewModel() {

    /** Saved events in order they appear in the full event list. */
    val events: StateFlow<List<Event>> =
        combine(repo.observeEvents(), repo.observeSavedIds()) { all, ids ->
            all.filter { it.id in ids }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Full saved-ID set, used by EventRow to render the heart state. */
    val savedIds: StateFlow<Set<String>> =
        repo.observeSavedIds()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun toggleSaved(id: String) = viewModelScope.launch {
        repo.toggleSaved(id)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
                SavedViewModel(app.repository)
            }
        }
    }
}
