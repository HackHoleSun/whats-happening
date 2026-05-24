package com.whatshappening.novisad.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Card
import com.whatshappening.novisad.R
import com.whatshappening.novisad.data.ScrapedEvent
import com.whatshappening.novisad.ui.components.EventDetailSheet
import com.whatshappening.novisad.ui.components.FilterBar
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

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
  val detailUiState by viewModel.detailUiState.collectAsState()
  val context = LocalContext.current

  detailUiState?.let { state ->
    EventDetailSheet(
      state = state,
      onDismiss = { viewModel.clearSelectedEvent() },
      onOpenInBrowser = {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.event.url)))
      },
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = "Šta se dešava u Novom Sadu",
            modifier = Modifier.size(32.dp),
          )
        },
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

        // TODO(chunk-04): Replace with new HomeScreen + EventCard(domain Event)
        items(filteredEvents, key = { it.id }) { event ->
          Card(
            onClick   = { viewModel.selectEvent(event) },
            modifier  = Modifier.fillMaxWidth(),
          ) {
            Column(Modifier.padding(16.dp)) {
              Text(event.title, style = MaterialTheme.typography.titleMedium)
              Text(
                "${event.date} · ${event.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}

// region Previews
// EventsScreen uses AndroidViewModel so we build a static replica for preview.

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EventsScreenPreview() {
  val sampleEvents = listOf(
    ScrapedEvent("1", "Jazz večer u Kazamatu", "Muzika", "2026-05-24", "20:00", "Kazamat, Novi Sad", "https://example.com"),
    ScrapedEvent("2", "Pozorišna predstava Hamleta", "Pozorište", "2026-05-25", "19:30", "Srpsko narodno pozorište", "https://example.com"),
    ScrapedEvent("3", "Košarkaška utakmica Vojvodina – Crvena zvezda", "Sport", "2026-05-23", null, "Spens, Novi Sad", "https://example.com"),
    ScrapedEvent("4", "Izložba savremene fotografije", null, "2026-05-26", "18:00", "Galerija Matice srpske", "https://example.com"),
  )
  WhatsHappeningTheme() {
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
          Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = "Šta se dešava u Novom Sadu",
            modifier = Modifier.size(32.dp),
          )
        },
          actions = {
            IconButton(onClick = {}) {
              Icon(Icons.Default.Refresh, contentDescription = "Osveži")
            }
          },
        )
      },
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        FilterBar(
          searchQuery = "",
          onSearchQueryChange = {},
          selectedTab = Tab.ALL,
          onTabSelected = {},
          categories = listOf("Muzika", "Pozorište", "Sport"),
          selectedCategory = null,
          onCategorySelected = {},
          selectedDateRange = null,
          onDateRangeSelected = {},
          onResetFilters = {},
        )
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          // TODO(chunk-04): Replace with new EventCard(domain Event)
          items(sampleEvents, key = { it.id }) { event ->
            Card(
              onClick  = {},
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(Modifier.padding(16.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium)
                Text(
                  "${event.date} · ${event.location}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    }
  }
}

// endregion