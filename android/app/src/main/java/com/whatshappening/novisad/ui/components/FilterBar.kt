package com.whatshappening.novisad.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.whatshappening.novisad.ui.DateRange as SelectedDateRange
import com.whatshappening.novisad.ui.Tab as AppTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedTab: AppTab,
  onTabSelected: (AppTab) -> Unit,
  categories: List<String>,
  selectedCategory: String?,
  onCategorySelected: (String?) -> Unit,
  selectedDateRange: SelectedDateRange?,
  onDateRangeSelected: (SelectedDateRange?) -> Unit,
  onResetFilters: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState =
    rememberDateRangePickerState(
      initialSelectedStartDateMillis =
        selectedDateRange
          ?.start
          ?.atStartOfDay(ZoneId.systemDefault())
          ?.toInstant()
          ?.toEpochMilli(),
      initialSelectedEndDateMillis =
        selectedDateRange
          ?.end
          ?.atStartOfDay(
            ZoneId.systemDefault(),
          )?.toInstant()
          ?.toEpochMilli(),
    )

  Column(modifier = modifier) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      placeholder = { Text("Pretraži događaje...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      trailingIcon =
        if (searchQuery.isNotEmpty()) {
          { Icon(Icons.Default.Close, contentDescription = "Obriši pretragu") }
        } else {
          null
        },
      singleLine = true,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
    )

    PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
      AppTab.entries.forEach { tab ->
        Tab(
          selected = selectedTab == tab,
          onClick = { onTabSelected(tab) },
          text = {
            Text(
              when (tab) {
                AppTab.TODAY -> "Danas"
                AppTab.THIS_WEEK -> "Ova nedelja"
                AppTab.ALL -> "Sve"
              },
            )
          },
        )
      }
    }

    Row(
      modifier =
        Modifier
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (searchQuery.isNotEmpty() || selectedDateRange != null || selectedCategory != null) {
        OutlinedButton(onClick = { onResetFilters() }) { Text("Reset") }
      }
      FilterChip(
        selected = selectedDateRange != null,
        onClick = { showDatePicker = true },
        label = {
          val fmt = DateTimeFormatter.ofPattern("d. MMM", Locale.forLanguageTag("sr"))
          Text(
            selectedDateRange?.let { "${it.start.format(fmt)} - ${it.end.format(fmt)}" } ?: "Datum",
          )
        },
        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        trailingIcon =
          if (selectedDateRange != null) {
            { Icon(Icons.Default.Close, contentDescription = "Ukloni filter datuma") }
          } else {
            null
          },
      )
      FilterChip(
        selected = selectedCategory == null && selectedDateRange == null,
        onClick = {
          onCategorySelected(null)
          onDateRangeSelected(null)
        },
        label = { Text("Sve") },
      )
      categories.forEach { category ->
        FilterChip(
          selected = selectedCategory == category,
          onClick = { onCategorySelected(if (selectedCategory == category) null else category) },
          label = { Text(category) },
        )
      }
    }
  }

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          val start = datePickerState.selectedStartDateMillis
          val end = datePickerState.selectedEndDateMillis
          if (start != null && end != null) {
            onDateRangeSelected(
              SelectedDateRange(
                start = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate(),
                end = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate(),
              ),
            )
          }
          showDatePicker = false
        }) { Text("OK") }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) { Text("Otkaži") }
      },
    ) {
      DateRangePicker(state = datePickerState)
    }
  }
}