package com.whatshappening.novisad.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.components.AppBottomNav
import com.whatshappening.novisad.ui.components.BottomNavDestination
import com.whatshappening.novisad.ui.components.EventRow
import com.whatshappening.novisad.ui.states.SavedEmptyState
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme

// ── SavedRoute — stateful wrapper ─────────────────────────────────────────────

@Composable
fun SavedRoute(
    onEventClick: (Event) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    viewModel: SavedViewModel = viewModel(factory = SavedViewModel.Factory),
) {
    val events   by viewModel.events.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    SavedScreen(
        events       = events,
        savedIds     = savedIds,
        onEventClick = onEventClick,
        onToggleSave = viewModel::toggleSaved,
        onHomeClick  = onHomeClick,
        onMapClick   = onMapClick,
    )
}

// ── SavedScreen — stateless ───────────────────────────────────────────────────

@Composable
fun SavedScreen(
    events: List<Event>,
    savedIds: Set<String>,
    onEventClick: (Event) -> Unit,
    onToggleSave: (String) -> Unit,
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            SavedHeader(count = events.size)

            if (events.isEmpty()) {
                SavedEmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 110.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(events, key = { it.id }) { ev ->
                        EventRow(
                            event        = ev,
                            saved        = ev.id in savedIds,
                            onClick      = { onEventClick(ev) },
                            onToggleSave = { onToggleSave(ev.id) },
                        )
                    }
                }
            }
        }

        AppBottomNav(
            current      = BottomNavDestination.Saved,
            onHomeClick  = onHomeClick,
            onMapClick   = onMapClick,
            onSavedClick = {},
            modifier     = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── SavedHeader ───────────────────────────────────────────────────────────────

@Composable
private fun SavedHeader(count: Int) {
    Column(
        modifier = Modifier.padding(
            start  = 20.dp,
            end    = 20.dp,
            top    = 18.dp,
            bottom = 14.dp,
        ),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Saved")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(".")
                }
            },
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$count event${if (count == 1) "" else "s"} you've bookmarked",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val savedEvents = MOCK_EVENTS.take(3)
private val savedIds = savedEvents.map { it.id }.toSet()
private val noOp: (Any) -> Unit = {}

@Preview(name = "Saved · With items · Light", showSystemUi = true)
@Composable
private fun SavedScreenPreviewLight() {
    WhatsHappeningTheme {
        SavedScreen(
            events       = savedEvents,
            savedIds     = savedIds,
            onEventClick = noOp,
            onToggleSave = noOp,
            onHomeClick  = {},
            onMapClick   = {},
        )
    }
}

@Preview(
    name = "Saved · With items · Dark",
    showSystemUi = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SavedScreenPreviewDark() {
    WhatsHappeningTheme {
        SavedScreen(
            events       = savedEvents,
            savedIds     = savedIds,
            onEventClick = noOp,
            onToggleSave = noOp,
            onHomeClick  = {},
            onMapClick   = {},
        )
    }
}

@Preview(name = "Saved · Empty · Light", showSystemUi = true)
@Composable
private fun SavedScreenEmptyPreview() {
    WhatsHappeningTheme {
        SavedScreen(
            events       = emptyList(),
            savedIds     = emptySet(),
            onEventClick = noOp,
            onToggleSave = noOp,
            onHomeClick  = {},
            onMapClick   = {},
        )
    }
}
