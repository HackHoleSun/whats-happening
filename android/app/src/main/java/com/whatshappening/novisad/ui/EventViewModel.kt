package com.whatshappening.novisad.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class Tab { TODAY, THIS_WEEK, ALL }

data class UiState(
  val isLoading: Boolean = false,
  val error: String? = null,
)

data class DateRange(
  val start: LocalDate,
  val end: LocalDate,
)

class EventViewModel(
  application: Application,
) : AndroidViewModel(application) {
  private val repository = EventRepository(application)

  private val allEvents = MutableStateFlow<List<Event>>(emptyList())
  val searchQuery = MutableStateFlow("")
  val selectedCategory = MutableStateFlow<String?>(null)
  val selectedTab = MutableStateFlow(Tab.TODAY)
  val selectedDateRange = MutableStateFlow<DateRange?>(null)
  val uiState = MutableStateFlow(UiState())

  val categories: StateFlow<List<String>> =
    allEvents
      .map { events -> events.mapNotNull { it.category }.distinct().sorted() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredEvents: StateFlow<List<Event>> =
    combine(
      allEvents,
      searchQuery,
      selectedCategory,
      selectedTab,
      selectedDateRange,
    ) { events, query, category, tab, range ->
      events
        .filter { matchesTab(it, tab) }
        .filter { category == null || it.category == category }
        .filter { range == null || (it.date >= range.start.toString() && it.date <= range.end.toString()) }
        .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    loadEvents()
  }

  fun loadEvents(forceRefresh: Boolean = false) {
    viewModelScope.launch {
      uiState.value = UiState(isLoading = true)
      try {
        allEvents.value = if (forceRefresh) repository.refreshEvents() else repository.getEvents()
        uiState.value = UiState()
      } catch (e: Exception) {
        uiState.value = UiState(error = e.message ?: "Greška pri učitavanju")
      }
    }
  }

  fun resetFilters() {
    searchQuery.value = ""
    selectedCategory.value = null
    selectedDateRange.value = null
  }

  private fun matchesTab(
    event: Event,
    tab: Tab,
  ): Boolean {
    val today = LocalDate.now()
    return when (tab) {
      Tab.TODAY -> {
        event.date == today.toString()
      }

      Tab.THIS_WEEK -> {
        val eventDate = runCatching { LocalDate.parse(event.date) }.getOrNull() ?: return false
        !eventDate.isBefore(today) && !eventDate.isAfter(today.plusDays(7))
      }

      Tab.ALL -> {
        true
      }
    }
  }
}