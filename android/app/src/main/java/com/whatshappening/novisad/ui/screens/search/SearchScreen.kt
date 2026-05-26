package com.whatshappening.novisad.ui.screens.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.ui.components.EventRow
import com.whatshappening.novisad.ui.components.HeaderIconButton
import com.whatshappening.novisad.ui.components.SectionLabel
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

// ── SearchRoute — stateful wrapper ────────────────────────────────────────────

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onEventClick: (Event) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val query    by viewModel.query.collectAsState()
    val recent   by viewModel.recent.collectAsState()
    val results  by viewModel.results.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    SearchScreen(
        query           = query,
        onQueryChange   = viewModel::onQueryChange,
        onQueryCommit   = viewModel::commitQuery,
        recent          = recent,
        results         = results,
        savedIds        = savedIds,
        onBack          = onBack,
        onEventClick    = onEventClick,
        onToggleSave    = viewModel::toggleSaved,
        onCategoryClick = { cat -> viewModel.onQueryChange(cat.displayName) },
    )
}

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onQueryCommit: () -> Unit,
    recent: List<String>,
    results: List<Event>,
    savedIds: Set<String>,
    onBack: () -> Unit,
    onEventClick: (Event) -> Unit,
    onToggleSave: (String) -> Unit,
    onCategoryClick: (EventCategory) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        SearchTopBar(
            query         = query,
            onQueryChange = onQueryChange,
            onCommit      = onQueryCommit,
            onBack        = onBack,
        )

        Box(Modifier.weight(1f)) {
            when {
                query.isBlank() -> SearchSuggestions(
                    recent          = recent,
                    onRecentClick   = onQueryChange,
                    onCategoryClick = onCategoryClick,
                )
                results.isEmpty() -> NoResults(query)
                else -> SearchResultsList(
                    results      = results,
                    savedIds     = savedIds,
                    onEventClick = onEventClick,
                    onToggleSave = onToggleSave,
                )
            }
        }
    }
}

// ── Search top bar ────────────────────────────────────────────────────────────

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCommit: () -> Unit,
    onBack: () -> Unit,
) {
    val palette = LocalCatppuccin.current
    val focus   = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Row(
        modifier              = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // Back button
        HeaderIconButton(
            onClick            = onBack,
            contentDescription = "Back",
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint               = palette.text,
            )
        }

        // Search input pill
        Surface(
            shape    = RoundedCornerShape(999.dp),
            color    = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Search,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp),
                )

                BasicTextField(
                    value           = query,
                    onValueChange   = onQueryChange,
                    modifier        = Modifier
                        .weight(1f)
                        .focusRequester(focus),
                    textStyle       = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush     = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onCommit() }),
                    decorationBox   = { inner ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text  = "Pretraži događaje, mesta, žanrove…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        }
                    },
                )

                // Clear button — only visible when there's text
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick  = { onQueryChange("") },
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            modifier           = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── Empty state — recent + browse by vibe ─────────────────────────────────────

private val BROWSE_CATEGORIES = listOf(
    EventCategory.Concert,  EventCategory.Performance, EventCategory.Film,
    EventCategory.Festival, EventCategory.Sport,     EventCategory.Exhibition,
)

@Composable
private fun SearchSuggestions(
    recent: List<String>,
    onRecentClick: (String) -> Unit,
    onCategoryClick: (EventCategory) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // ── Recent searches ───────────────────────────────────────────────────
        if (recent.isNotEmpty()) {
            SectionLabel("Nedavno")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp),
            ) {
                recent.forEach { term ->
                    RecentPill(term) { onRecentClick(term) }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Browse by vibe ────────────────────────────────────────────────────
        SectionLabel("Istraži po kategoriji")
        Spacer(Modifier.height(10.dp))

        // 2-column grid using chunked rows
        BROWSE_CATEGORIES.chunked(2).forEach { pair ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                pair.forEach { cat ->
                    BrowseCategoryCard(
                        category = cat,
                        onClick  = { onCategoryClick(cat) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Pad with an empty spacer if odd count (safety net)
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

/**
 * Pill button for a recent search term.
 */
@Composable
private fun RecentPill(term: String, onClick: () -> Unit) {
    val palette = LocalCatppuccin.current
    Box(
        modifier = Modifier
            .background(palette.mantle, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text  = term,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Category card for the Browse by Vibe grid.
 * Coloured glyph square on the left, category name on the right.
 */
@Composable
private fun BrowseCategoryCard(
    category: EventCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCatppuccin.current

    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(18.dp),
        color    = palette.mantle,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // 36dp coloured square with glyph
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = category.hue(palette),
            ) {
                Box(
                    modifier         = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = category.glyph,
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text(
                text  = category.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ── Results list ──────────────────────────────────────────────────────────────

@Composable
private fun SearchResultsList(
    results: List<Event>,
    savedIds: Set<String>,
    onEventClick: (Event) -> Unit,
    onToggleSave: (String) -> Unit,
) {
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionLabel("${results.size} result${if (results.size == 1) "" else "s"}")
        }
        items(results, key = { it.id }) { ev ->
            EventRow(
                event        = ev,
                saved        = ev.id in savedIds,
                onClick      = { onEventClick(ev) },
                onToggleSave = { onToggleSave(ev.id) },
            )
        }
    }
}

// ── No-results state ──────────────────────────────────────────────────────────

@Composable
private fun NoResults(query: String) {
    val palette = LocalCatppuccin.current

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = palette.subtext0)) {
                    append("Nema rezultata za ")
                }
                withStyle(
                    SpanStyle(
                        color      = palette.text,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append("\"$query\"")
                }
            },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewRecent  = listOf("Synthwave", "Pozorište", "Festival piva", "Jazz")
private val previewResults = MOCK_EVENTS.filter {
    it.title.contains("music", ignoreCase = true) ||
    it.category.displayName.contains("music", ignoreCase = true)
}.ifEmpty { MOCK_EVENTS.take(3) }

@Preview(name = "Search · Empty · Light")
@Composable
private fun SearchEmptyLightPreview() {
    WhatsHappeningTheme(darkTheme = false) {
        SearchScreen(
            query           = "",
            onQueryChange   = {},
            onQueryCommit   = {},
            recent          = previewRecent,
            results         = emptyList(),
            savedIds        = emptySet(),
            onBack          = {},
            onEventClick    = {},
            onToggleSave    = {},
            onCategoryClick = {},
        )
    }
}

@Preview(name = "Search · Empty · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchEmptyDarkPreview() {
    WhatsHappeningTheme(darkTheme = true) {
        SearchScreen(
            query           = "",
            onQueryChange   = {},
            onQueryCommit   = {},
            recent          = previewRecent,
            results         = emptyList(),
            savedIds        = emptySet(),
            onBack          = {},
            onEventClick    = {},
            onToggleSave    = {},
            onCategoryClick = {},
        )
    }
}

@Preview(name = "Search · Results · Light")
@Composable
private fun SearchResultsLightPreview() {
    WhatsHappeningTheme(darkTheme = false) {
        SearchScreen(
            query           = "music",
            onQueryChange   = {},
            onQueryCommit   = {},
            recent          = previewRecent,
            results         = previewResults,
            savedIds        = emptySet(),
            onBack          = {},
            onEventClick    = {},
            onToggleSave    = {},
            onCategoryClick = {},
        )
    }
}

@Preview(name = "Search · No Results · Light")
@Composable
private fun SearchNoResultsLightPreview() {
    WhatsHappeningTheme(darkTheme = false) {
        SearchScreen(
            query           = "synthbar",
            onQueryChange   = {},
            onQueryCommit   = {},
            recent          = previewRecent,
            results         = emptyList(),
            savedIds        = emptySet(),
            onBack          = {},
            onEventClick    = {},
            onToggleSave    = {},
            onCategoryClick = {},
        )
    }
}
