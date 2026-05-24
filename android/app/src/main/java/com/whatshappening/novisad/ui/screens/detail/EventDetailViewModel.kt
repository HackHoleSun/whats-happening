package com.whatshappening.novisad.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.whatshappening.novisad.App
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ── Detail load state ─────────────────────────────────────────────────────────

sealed interface DetailLoadState {
    /** Fetch in progress. */
    data object Loading : DetailLoadState
    /** Worker returned description (and optionally a better photo URL). */
    data class Loaded(val description: String, val photoUrl: String?) : DetailLoadState
    /** Fetch failed or repo doesn't support it (mock). */
    data object Unavailable : DetailLoadState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class EventDetailViewModel(
    private val repo: EventRepository,
    private val eventId: String,
) : ViewModel() {

    /** The event being viewed, or null while loading. */
    val event: StateFlow<Event?> = repo.observeEvents()
        .map { it.firstOrNull { e -> e.id == eventId } }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /** Whether the current event is in the user's saved list. */
    val saved: StateFlow<Boolean> = repo.observeSavedIds()
        .map { eventId in it }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _detailState = MutableStateFlow<DetailLoadState>(DetailLoadState.Loading)
    val detailState: StateFlow<DetailLoadState> = _detailState

    init {
        viewModelScope.launch {
            // Wait until the event is available (events may still be loading from network)
            val ev = event.filterNotNull().first()
            _detailState.value = DetailLoadState.Loading
            val detail = repo.fetchDetail(ev.link)
            _detailState.value = if (detail != null) {
                DetailLoadState.Loaded(
                    description = detail.description,
                    photoUrl    = detail.imageUrl,
                )
            } else {
                DetailLoadState.Unavailable
            }
        }
    }

    fun toggleSaved() = viewModelScope.launch { repo.toggleSaved(eventId) }

    companion object {
        fun factory(eventId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
                EventDetailViewModel(app.repository, eventId)
            }
        }
    }
}
