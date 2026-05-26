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

    /** Nullable lat/lng pair updated whenever the composable receives a GPS fix. */
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    val events: StateFlow<List<Event>> =
        combine(repo.observeEvents(), _filter, _userLocation) { all, f, loc ->
            val withDistances = if (loc != null) {
                all.map { event ->
                    if (event.lat != null && event.lng != null) {
                        val result = FloatArray(1)
                        android.location.Location.distanceBetween(
                            loc.first, loc.second, event.lat, event.lng, result
                        )
                        event.copy(distanceKm = result[0] / 1000.0)
                    } else {
                        event
                    }
                }
            } else all
            // Distance filter is meaningless without a real GPS fix — disable it
            val effectiveFilter = if (loc == null) f.copy(maxDistanceKm = 10f) else f
            withDistances.apply(effectiveFilter)
        }
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
        _filter.value = _filter.value.copy(range = range, dateFrom = null, dateTo = null)
    }

    fun setCategories(cats: Set<EventCategory>) {
        _filter.value = _filter.value.copy(categories = cats)
    }

    fun setDateRange(from: LocalDate, to: LocalDate) {
        _filter.value = _filter.value.copy(range = DateRange.Range, dateFrom = from, dateTo = to)
    }

    fun applyFilter(filter: EventFilter) {
        _filter.value = filter
    }

    fun clearFilters() {
        _filter.value = EventFilter()
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
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
