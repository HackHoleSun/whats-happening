package com.whatshappening.novisad.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.whatshappening.novisad.ui.components.EventCard
import com.whatshappening.novisad.ui.components.FilterBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(viewModel: EventViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val filteredEvents by viewModel.filteredEvents.collectAsState()
  val categories by viewModel.categories.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val selectedDateRange by viewModel.selectedDateRange.collectAsState()
  val context = LocalContext.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Šta se dešava u Novom Sadu") },
        actions = {
          IconButton(
            onClick = { viewModel.loadEvents(forceRefresh = true) },
            enabled = !uiState.isLoading,
          ) {
            if (uiState.isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
              Icon(Icons.Default.Refresh, contentDescription = "Osveži")
            }
          }
        },
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
    ) {
      FilterBar(
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.searchQuery.value = it },
        selectedTab = selectedTab,
        onTabSelected = { viewModel.selectedTab.value = it },
        categories = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = { viewModel.selectedCategory.value = it },
        selectedDateRange = selectedDateRange,
        onDateRangeSelected = { viewModel.selectedDateRange.value = it },
        onResetFilters = { viewModel.resetFilters() },
      )

      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        if (uiState.isLoading && filteredEvents.isEmpty()) {
          item {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator()
            }
          }
        }

        uiState.error?.let { error ->
          item {
            Text(
              text = "Greška: $error",
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.padding(8.dp),
            )
          }
        }

        if (!uiState.isLoading && filteredEvents.isEmpty() && uiState.error == null) {
          item {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text("Nema pronađenih događaja.")
            }
          }
        }

        items(filteredEvents, key = { it.id }) { event ->
          EventCard(
            event = event,
            onClick = {
              context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(event.url)),
              )
            },
          )
        }
      }
    }
  }
}