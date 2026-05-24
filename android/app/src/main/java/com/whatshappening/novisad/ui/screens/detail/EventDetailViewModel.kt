package com.whatshappening.novisad.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventRepository
import com.whatshappening.novisad.data.MockEventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    fun toggleSaved() = viewModelScope.launch { repo.toggleSaved(eventId) }

    companion object {
        /**
         * Default factory wired to [MockEventRepository].
         * Replaced by proper DI in Chunk 10 (navigation).
         */
        fun factory(eventId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { EventDetailViewModel(MockEventRepository(), eventId) }
        }
    }
}
