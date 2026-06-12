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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
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

    /**
     * Events annotated with distance from the user. Kept separate from the
     * filter combine so distances are recomputed only when the event list or
     * GPS fix changes — not on every filter tweak.
     */
    private val eventsWithDistance: Flow<List<Event>> =
        combine(repo.observeEvents(), _userLocation) { all, loc ->
            if (loc == null) all
            else all.map { event ->
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
        }

    val events: StateFlow<List<Event>> =
        combine(eventsWithDistance, _filter, _userLocation) { all, f, loc ->
            // Distance filter is meaningless without a real GPS fix — disable it
            val effectiveFilter = if (loc == null) f.copy(maxDistanceKm = 10f) else f
            all.apply(effectiveFilter)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedIds: StateFlow<Set<String>> =
        repo.observeSavedIds()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    /** One-shot message shown when a pull-to-refresh fails; cleared after display. */
    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError

    /** True until the first non-empty event list arrives from the repository. */
    private val _initialLoading = MutableStateFlow(true)
    val initialLoading: StateFlow<Boolean> = _initialLoading

    init {
        viewModelScope.launch {
            repo.observeLoadAttempted().first { it }
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
        try {
            repo.refresh()
        } catch (_: Exception) {
            _refreshError.value = "Osvežavanje nije uspelo. Proverite internet vezu."
        } finally {
            _refreshing.value = false
        }
    }

    fun clearRefreshError() {
        _refreshError.value = null
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
