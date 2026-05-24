package com.whatshappening.novisad.ui.screens.map

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(private val repo: EventRepository) : ViewModel() {

    val events: StateFlow<List<Event>> = repo.observeEvents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedIds: StateFlow<Set<String>> = repo.observeSavedIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun toggleSaved(id: String) = viewModelScope.launch { repo.toggleSaved(id) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
                MapViewModel(app.repository)
            }
        }
    }
}
