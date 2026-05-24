package com.whatshappening.novisad.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.whatshappening.novisad.App
import com.whatshappening.novisad.data.DateRange
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.data.EventFilter
import com.whatshappening.novisad.data.EventRepository
import com.whatshappening.novisad.data.apply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val repo: EventRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(EventFilter())
    val filter: StateFlow<EventFilter> = _filter

    val events: StateFlow<List<Event>> =
        combine(repo.observeEvents(), _filter) { all, f -> all.apply(f) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedIds: StateFlow<Set<String>> =
        repo.observeSavedIds()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    /** True until the first non-empty event list arrives from the repository. */
    private val _initialLoading = MutableStateFlow(true)
    val initialLoading: StateFlow<Boolean> = _initialLoading

    init {
        // Flip loading off as soon as any events come through the repository
        viewModelScope.launch {
            repo.observeEvents().first { it.isNotEmpty() }
            _initialLoading.value = false
        }
    }

    fun setRange(range: DateRange) {
        _filter.value = _filter.value.copy(range = range, selectedDate = null)
    }

    fun setCategories(cats: Set<EventCategory>) {
        _filter.value = _filter.value.copy(categories = cats)
    }

    fun setDate(date: LocalDate) {
        _filter.value = _filter.value.copy(range = DateRange.Specific, selectedDate = date)
    }

    fun applyFilter(filter: EventFilter) {
        _filter.value = filter
    }

    fun clearFilters() {
        _filter.value = EventFilter()
    }

    fun toggleSaved(id: String) = viewModelScope.launch {
        repo.toggleSaved(id)
    }

    fun refresh() = viewModelScope.launch {
        _refreshing.value = true
        try { repo.refresh() } finally { _refreshing.value = false }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
                HomeViewModel(app.repository)
            }
        }
    }
}
