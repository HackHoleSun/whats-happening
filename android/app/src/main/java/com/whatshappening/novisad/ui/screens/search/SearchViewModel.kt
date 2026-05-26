package com.whatshappening.novisad.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.whatshappening.novisad.App
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventFilter
import com.whatshappening.novisad.data.EventRepository
import com.whatshappening.novisad.data.apply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repo: EventRepository,
    private val recentSearchStore: RecentSearchRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val recent: StateFlow<List<String>> = recentSearchStore.recent
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedIds: StateFlow<Set<String>> = repo.observeSavedIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val results: StateFlow<List<Event>> =
        combine(repo.observeEvents(), _query) { all, q ->
            if (q.isBlank()) emptyList()
            else all.apply(EventFilter(searchQuery = q))
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onQueryChange(q: String) { _query.value = q }

    fun commitQuery() {
        val q = _query.value.trim()
        if (q.isNotEmpty()) viewModelScope.launch { recentSearchStore.add(q) }
    }

    fun toggleSaved(id: String) = viewModelScope.launch { repo.toggleSaved(id) }

    /** Pre-fill the search bar with a recent term (does not commit to history). */
    fun useRecent(term: String) { _query.value = term }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
                SearchViewModel(
                    repo              = app.repository,
                    recentSearchStore = app.recentSearchStore,
                )
            }
        }
    }
}
