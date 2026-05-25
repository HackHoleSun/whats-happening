@file:OptIn(ExperimentalMaterial3Api::class)

package com.whatshappening.novisad.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.whatshappening.novisad.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.whatshappening.novisad.ui.theme.Bricolage
import com.whatshappening.novisad.data.DateRange
import com.whatshappening.novisad.data.Event
import com.whatshappening.novisad.data.EventCategory
import com.whatshappening.novisad.data.EventFilter
import com.whatshappening.novisad.data.MOCK_EVENTS
import com.whatshappening.novisad.data.MOCK_TODAY
import com.whatshappening.novisad.ui.components.AppBottomNav
import com.whatshappening.novisad.ui.components.BottomNavDestination
import com.whatshappening.novisad.ui.components.EventCard
import com.whatshappening.novisad.ui.components.FilterChipRemovable
import com.whatshappening.novisad.ui.components.HeaderIconButton
import com.whatshappening.novisad.ui.components.PillSegmentedControl
import com.whatshappening.novisad.ui.screens.sheets.CategorySheet
import com.whatshappening.novisad.ui.screens.sheets.DateSheet
import com.whatshappening.novisad.ui.screens.sheets.FilterSheet
import com.whatshappening.novisad.ui.states.EmptyFiltersState
import com.whatshappening.novisad.ui.states.LoadingScreen
import com.whatshappening.novisad.prefs.LocalUserPrefs
import com.whatshappening.novisad.ui.theme.LocalCatppuccin
import com.whatshappening.novisad.ui.theme.WhatsHappeningTheme
import com.whatshappening.novisad.util.formatDate
import com.whatshappening.novisad.util.formatShortDate
import com.whatshappening.novisad.util.formatDayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── HomeState ─────────────────────────────────────────────────────────────────

data class HomeState(
    val events: List<Event>,
    val filter: EventFilter,
    val savedIds: Set<String>,
    val refreshing: Boolean,
    val today: LocalDate,
    val cityName: String = "Novi Sad",
    /** True only on the very first load before any events have arrived. */
    val isLoading: Boolean = false,
)

// ── HomeSheet — enum for active bottom sheet ──────────────────────────────────

enum class HomeSheet { Filter, Date, Category }

// ── HomeRoute — stateful wrapper ──────────────────────────────────────────────

/**
 * Connects [HomeViewModel] to the stateless [HomeScreen] and manages the three
 * bottom sheets: Filter, Date picker, and Category picker.
 *
 * The filter draft is hoisted here so it survives the Filter → Date → Filter
 * round-trip (M3 only supports one ModalBottomSheet at a time).
 *
 * Navigation callbacks are threaded in from the nav graph (Chunk 10).
 */
@Composable
fun HomeRoute(
    onEventClick: (Event) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onSavedClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val events      by viewModel.events.collectAsState()
    val filter      by viewModel.filter.collectAsState()
    val savedIds    by viewModel.savedIds.collectAsState()
    val refreshing  by viewModel.refreshing.collectAsState()
    val isLoading   by viewModel.initialLoading.collectAsState()

    // Show skeleton until the first batch of events arrives
    if (isLoading) {
        LoadingScreen()
        return
    }

    // Read theme-toggle action from the CompositionLocal provided by MainActivity
    val userPrefs = LocalUserPrefs.current

    // Sheet visibility state
    var sheet by rememberSaveable { mutableStateOf<HomeSheet?>(null) }

    // Filter draft hoisted here so it survives the Filter ↔ Date sheet round-trip
    var filterDraft by remember { mutableStateOf(filter) }

    HomeScreen(
        state = HomeState(
            events     = events,
            filter     = filter,
            savedIds   = savedIds,
            refreshing = refreshing,
            today      = LocalDate.now(),
        ),
        onEventClick      = onEventClick,
        onSearchClick     = onSearchClick,
        onMapClick        = onMapClick,
        onSavedClick      = onSavedClick,
        onFiltersClick    = {
            filterDraft = filter  // sync draft to committed filter when opening
            sheet = HomeSheet.Filter
        },
        onThemeToggle     = userPrefs.onToggleTheme,
        onRangeChange     = viewModel::setRange,
        onRemoveCategory  = { cat -> viewModel.setCategories(filter.categories - cat) },
        onClearDate       = { viewModel.setRange(DateRange.All) },
        onToggleSave      = viewModel::toggleSaved,
        onRefresh         = viewModel::refresh,
        onClearFilters    = viewModel::clearFilters,
    )

    // ── Sheet rendering ───────────────────────────────────────────────────────

    when (sheet) {
        HomeSheet.Filter -> FilterSheet(
            initial = filterDraft,
            onApply = { newFilter ->
                viewModel.applyFilter(newFilter)
                sheet = null
            },
            onOpenDatePicker = {
                // Dismiss FilterSheet and open DateSheet; draft stays in filterDraft
                sheet = HomeSheet.Date
            },
            onDismiss = { sheet = null },
        )

        HomeSheet.Date -> DateSheet(
            initialDateFrom = filterDraft.dateFrom,
            initialDateTo   = filterDraft.dateTo,
            eventDates      = events.map { it.date }.toSet(),
            onPick = { from, to ->
                // Update draft with the picked range then return to FilterSheet
                filterDraft = filterDraft.copy(
                    range    = DateRange.Range,
                    dateFrom = from,
                    dateTo   = to,
                )
                sheet = HomeSheet.Filter
            },
            onDismiss = { sheet = HomeSheet.Filter },
        )

        HomeSheet.Category -> CategorySheet(
            initial = filterDraft.categories,
            onApply = { cats ->
                viewModel.setCategories(cats)
                sheet = null
            },
            onDismiss = { sheet = null },
        )

        null -> { /* no sheet */ }
    }
}

// ── HomeScreen — stateless ────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    state: HomeState,
    onEventClick: (Event) -> Unit,
    onSearchClick: () -> Unit,
    onMapClick: () -> Unit,
    onSavedClick: () -> Unit,
    onFiltersClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onRangeChange: (DateRange) -> Unit,
    onRemoveCategory: (EventCategory) -> Unit,
    onClearDate: () -> Unit,
    onToggleSave: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit,
) {
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.fillMaxSize()) {
            HomeHeader(
                state            = state,
                onSearchClick    = onSearchClick,
                onThemeToggle    = onThemeToggle,
                onRangeChange    = onRangeChange,
                onFiltersClick   = onFiltersClick,
                onRemoveCategory = onRemoveCategory,
                onClearDate      = onClearDate,
            )

            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh    = onRefresh,
                modifier     = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    state         = listState,
                    modifier      = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 20.dp,
                        bottom = 110.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        CountAndRefreshRow(
                            count     = state.events.size,
                            onRefresh = onRefresh,
                        )
                    }

                    if (state.events.isEmpty()) {
                        item {
                            EmptyFiltersState(onClearFilters = onClearFilters)
                        }
                    } else {
                        items(state.events, key = { it.id }) { ev ->
                            EventCard(
                                event        = ev,
                                saved        = ev.id in state.savedIds,
                                onClick      = { onEventClick(ev) },
                                onToggleSave = { onToggleSave(ev.id) },
                            )
                        }
                    }
                }
            }
        }

        AppBottomNav(
            current      = BottomNavDestination.Home,
            onHomeClick  = {},
            onMapClick   = onMapClick,
            onSavedClick = onSavedClick,
            modifier     = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── HomeHeader ────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    state: HomeState,
    onSearchClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onRangeChange: (DateRange) -> Unit,
    onFiltersClick: () -> Unit,
    onRemoveCategory: (EventCategory) -> Unit,
    onClearDate: () -> Unit,
) {
    val palette   = LocalCatppuccin.current
    val accent    = MaterialTheme.colorScheme.primary
    val isDark    = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 18.dp, start = 20.dp, end = 20.dp, bottom = 16.dp),
    ) {
        // ── 1. Top row — wordmark + icon buttons ──────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Logo + wordmark side by side
                val bricolage = if (LocalInspectionMode.current) FontFamily.SansSerif else Bricolage
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Image(
                        painter            = painterResource(R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier           = Modifier.size(44.dp),
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("What's\nHappening")
                            withStyle(SpanStyle(color = accent)) { append(".") }
                        },
                        style = TextStyle(
                            fontFamily    = bricolage,
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 26.sp,
                            lineHeight    = 26.sp,
                            letterSpacing = (-1.4).sp,
                        ),
                        color = palette.text,
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Date sub-line: "Sunday · May 24 · Newhaven"
                val mdFormatter = DateTimeFormatter.ofPattern("MMM d")
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = palette.subtext1)) {
                            append(formatDayOfWeek(state.today))
                            append(" · ")
                            append(state.today.format(mdFormatter))
                        }
                        withStyle(SpanStyle(color = palette.subtext0)) {
                            append(" · ")
                            append(state.cityName)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                HeaderIconButton(onClick = onSearchClick, contentDescription = "Search") {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = palette.text,
                    )
                }
                HeaderIconButton(onClick = onThemeToggle, contentDescription = "Toggle theme") {
                    Icon(
                        imageVector        = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = palette.text,
                    )
                }
            }
        }

        // ── 2. Range row — segmented control + filter button ──────────────────
        Spacer(Modifier.height(18.dp))

        val rangeOptions = listOf("today" to "Danas", "week" to "Sedmica", "all" to "Sve")
        val selectedRangeId = when (state.filter.range) {
            DateRange.Today    -> "today"
            DateRange.Week     -> "week"
            else               -> "all"
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            PillSegmentedControl(
                options    = rangeOptions,
                selectedId = selectedRangeId,
                onSelected = { id ->
                    onRangeChange(
                        when (id) {
                            "today" -> DateRange.Today
                            "week"  -> DateRange.Week
                            else    -> DateRange.All
                        }
                    )
                },
                modifier = Modifier.weight(1f),
            )

            // Filter button — active state changes background to accent
            val filterBg by animateColorAsState(
                targetValue   = if (state.filter.isActive) accent else palette.mantle,
                animationSpec = tween(200),
                label         = "filterBg",
            )
            val filterTint by animateColorAsState(
                targetValue   = if (state.filter.isActive) MaterialTheme.colorScheme.onPrimary else palette.text,
                animationSpec = tween(200),
                label         = "filterTint",
            )
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(filterBg)
                        .clickable(onClick = onFiltersClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Tune,
                        contentDescription = "Filters",
                        modifier           = Modifier.size(22.dp),
                        tint               = filterTint,
                    )
                }
                // Red dot indicator when filters are active
                if (state.filter.isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(palette.red)
                            .align(Alignment.TopEnd),
                    )
                }
            }
        }

        // ── 3. Active filter chips (shown only when relevant) ─────────────────
        val showChips = state.filter.categories.isNotEmpty() ||
                (state.filter.range == DateRange.Range && state.filter.dateFrom != null)
        if (showChips) {
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
            ) {
                state.filter.categories.forEach { cat ->
                    FilterChipRemovable(
                        text     = cat.displayName,
                        onRemove = { onRemoveCategory(cat) },
                        icon = {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(cat.hue(palette))
                            )
                        },
                    )
                }
                if (state.filter.range == DateRange.Range && state.filter.dateFrom != null) {
                    val dateChipText = if (state.filter.dateTo != null &&
                            state.filter.dateTo != state.filter.dateFrom) {
                        "${formatShortDate(state.filter.dateFrom)} – ${formatShortDate(state.filter.dateTo)}"
                    } else {
                        formatDate(state.filter.dateFrom)
                    }
                    FilterChipRemovable(
                        text     = dateChipText,
                        onRemove = onClearDate,
                        icon = {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp),
                                tint               = palette.subtext1,
                            )
                        },
                    )
                }
            }
        }
    }
}

// ── CountAndRefreshRow ────────────────────────────────────────────────────────

@Composable
private fun CountAndRefreshRow(count: Int, onRefresh: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = "$count events".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick        = onRefresh,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                Icons.Outlined.Refresh,
                contentDescription = null,
                modifier           = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text  = "Osveži".uppercase(),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val defaultState = HomeState(
    events     = MOCK_EVENTS,
    filter     = EventFilter(),
    savedIds   = setOf("e1", "e3"),
    refreshing = false,
    today      = MOCK_TODAY,
)

private val activeFilterState = HomeState(
    events   = MOCK_EVENTS.filter {
        it.category == EventCategory.Music || it.category == EventCategory.Food
    },
    filter   = EventFilter(
        range      = DateRange.Week,
        categories = setOf(EventCategory.Music, EventCategory.Food),
    ),
    savedIds   = setOf("e1"),
    refreshing = false,
    today      = MOCK_TODAY,
)

private val emptyFilterState = HomeState(
    events     = emptyList(),
    filter     = EventFilter(
        range      = DateRange.Today,
        categories = setOf(EventCategory.Sports),
    ),
    savedIds   = emptySet(),
    refreshing = false,
    today      = MOCK_TODAY,
)

private val noOp: (Any) -> Unit = {}

@Preview(name = "Home · Default · Light", showSystemUi = true)
@Composable
private fun HomePreviewLight() {
    WhatsHappeningTheme {
        HomeScreen(
            state            = defaultState,
            onEventClick     = noOp,
            onSearchClick    = {},
            onMapClick       = {},
            onSavedClick     = {},
            onFiltersClick   = {},
            onThemeToggle    = {},
            onRangeChange    = noOp,
            onRemoveCategory = noOp,
            onClearDate      = {},
            onToggleSave     = noOp,
            onRefresh        = {},
            onClearFilters   = {},
        )
    }
}

@Preview(name = "Home · Default · Dark", showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomePreviewDark() {
    WhatsHappeningTheme {
        HomeScreen(
            state            = defaultState,
            onEventClick     = noOp,
            onSearchClick    = {},
            onMapClick       = {},
            onSavedClick     = {},
            onFiltersClick   = {},
            onThemeToggle    = {},
            onRangeChange    = noOp,
            onRemoveCategory = noOp,
            onClearDate      = {},
            onToggleSave     = noOp,
            onRefresh        = {},
            onClearFilters   = {},
        )
    }
}

@Preview(name = "Home · Active Filters · Light", showSystemUi = true)
@Composable
private fun HomePreviewActiveFilters() {
    WhatsHappeningTheme {
        HomeScreen(
            state            = activeFilterState,
            onEventClick     = noOp,
            onSearchClick    = {},
            onMapClick       = {},
            onSavedClick     = {},
            onFiltersClick   = {},
            onThemeToggle    = {},
            onRangeChange    = noOp,
            onRemoveCategory = noOp,
            onClearDate      = {},
            onToggleSave     = noOp,
            onRefresh        = {},
            onClearFilters   = {},
        )
    }
}

@Preview(name = "Home · Empty Filters · Light", showSystemUi = true)
@Composable
private fun HomePreviewEmpty() {
    WhatsHappeningTheme {
        HomeScreen(
            state            = emptyFilterState,
            onEventClick     = noOp,
            onSearchClick    = {},
            onMapClick       = {},
            onSavedClick     = {},
            onFiltersClick   = {},
            onThemeToggle    = {},
            onRangeChange    = noOp,
            onRemoveCategory = noOp,
            onClearDate      = {},
            onToggleSave     = noOp,
            onRefresh        = {},
            onClearFilters   = {},
        )
    }
}
