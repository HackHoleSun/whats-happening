package com.whatshappening.novisad.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime

// ── Preview anchor ────────────────────────────────────────────────────────────

/** Fixed "today" for previews so date-sensitive filters read sensibly. */
val MOCK_TODAY: LocalDate = LocalDate.of(2026, 5, 24)

// ── Mock events (verbatim from the prototype's data.jsx) ──────────────────────

val MOCK_EVENTS: List<Event> = listOf(
    Event(
        id          = "e1",
        title       = "Synthwave Saturday",
        category    = EventCategory.Concert,
        date        = LocalDate.of(2026, 5, 24),
        startTime   = LocalTime.of(22, 0),
        endTime     = LocalTime.of(4, 0),
        location    = "Velvet Underground · Riverside",
        distanceKm  = 1.2,
        description = "A 6-hour journey through retrofuturist beats. Three rooms, three eras: italo disco, vintage synth, neon house. Visuals by Studio Halo. 18+, ID required.",
        organizer   = "Velvet Underground",
        priceLabel  = "€18 / €22 door",
        link        = "velvet.events/synthwave-sat",
    ),
    Event(
        id          = "e2",
        title       = "Sunday Späti Market",
        category    = EventCategory.Festival,
        date        = LocalDate.of(2026, 5, 24),
        startTime   = LocalTime.of(10, 0),
        endTime     = LocalTime.of(16, 0),
        location    = "Mauerpark Lawn · Greenway 4",
        distanceKm  = 0.6,
        description = "Forty makers, one lawn. Sourdough, fermented hot sauce, single-origin chocolate, natural wine. Bring a tote, leave with weekly groceries.",
        organizer   = "Newhaven Makers Co-op",
        priceLabel  = "Free entry",
        link        = "makersco.op/sunday",
    ),
    Event(
        id          = "e3",
        title       = "Bauhaus, Reimagined",
        category    = EventCategory.Exhibition,
        date        = LocalDate.of(2026, 5, 25),
        startTime   = LocalTime.of(11, 0),
        endTime     = LocalTime.of(20, 0),
        location    = "Kunsthalle Newhaven · Hall B",
        distanceKm  = 2.4,
        description = "A reframing of Bauhaus principles by twelve contemporary artists from across the continent. Includes a working print studio open every afternoon.",
        organizer   = "Kunsthalle Newhaven",
        priceLabel  = "€12 / €8 student",
        link        = "kunsthalle.nh/bauhaus",
    ),
    Event(
        id          = "e4",
        title       = "Sunset Cyclists",
        category    = EventCategory.Sport,
        date        = LocalDate.of(2026, 5, 26),
        startTime   = LocalTime.of(19, 30),
        endTime     = LocalTime.of(21, 30),
        location    = "Tempelhof Field · East Gate",
        distanceKm  = 3.1,
        description = "15km group ride along the canal at golden hour. Easy pace, no drop. Bring lights, water, and a sense of humor.",
        organizer   = "Newhaven Wheelers",
        priceLabel  = "Free",
        link        = "wheelers.cc/sunset",
    ),
    Event(
        id          = "e5",
        title       = "Code & Coffee #042",
        category    = EventCategory.Lecture,
        date        = LocalDate.of(2026, 5, 27),
        startTime   = LocalTime.of(18, 30),
        endTime     = LocalTime.of(22, 0),
        location    = "Factory Co-working · Floor 3",
        distanceKm  = 1.8,
        description = "Two lightning talks (LLM evals; tiny game engines) then open hack tables. Drinks sponsored by the usual suspects.",
        organizer   = "Newhaven Devs",
        priceLabel  = "Free, RSVP",
        link        = "nhdevs.org/042",
    ),
    Event(
        id          = "e6",
        title       = "Open Mic: New Comics",
        category    = EventCategory.Performance,
        date        = LocalDate.of(2026, 5, 28),
        startTime   = LocalTime.of(20, 0),
        endTime     = LocalTime.of(23, 0),
        location    = "Lido Basement · Lothar Str 8",
        distanceKm  = 2.0,
        description = "Eight emerging comedians, five minutes each, ranked at the door. House cocktails. Loud.",
        organizer   = "Lido",
        priceLabel  = "€10",
        link        = "lido.nh/openmic",
    ),
    Event(
        id          = "e7",
        title       = "Half-Marathon: Riverline",
        category    = EventCategory.Sport,
        date        = LocalDate.of(2026, 5, 30),
        startTime   = LocalTime.of(8, 0),
        endTime     = LocalTime.of(12, 0),
        location    = "Start · North Quay",
        distanceKm  = 4.5,
        description = "The annual flat-fast 21k along the river. Bib pickup Friday & Saturday. Pacers for 1:45 / 2:00 / 2:15.",
        organizer   = "Newhaven Athletics",
        priceLabel  = "€42 entry",
        link        = "nhathletics/half",
    ),
    Event(
        id          = "e8",
        title       = "Open-Air Cinema: Wong Kar-wai",
        category    = EventCategory.Film,
        date        = LocalDate.of(2026, 5, 29),
        startTime   = LocalTime.of(21, 30),
        endTime     = LocalTime.of(23, 45),
        location    = "Kulturhof Courtyard",
        distanceKm  = 1.1,
        description = "Two films under the stars. Bring a blanket. Hot ramen and plum wine on site until midnight.",
        organizer   = "Kulturhof",
        priceLabel  = "€14",
        link        = "kulturhof.nh/wkw",
    ),
    Event(
        id          = "e9",
        title       = "Vinyl Swap Society",
        category    = EventCategory.Concert,
        date        = LocalDate.of(2026, 6, 1),
        startTime   = LocalTime.of(14, 0),
        endTime     = LocalTime.of(18, 0),
        location    = "Holzmarkt 25",
        distanceKm  = 2.7,
        description = "Bring 5 records, leave with 5 new ones. House DJs play the rejects. All genres welcome.",
        organizer   = "Wax Cult",
        priceLabel  = "Free",
        link        = "waxcult.nh/swap",
    ),
    Event(
        id          = "e10",
        title       = "Pottery Throw-Down",
        category    = EventCategory.Workshop,
        date        = LocalDate.of(2026, 6, 2),
        startTime   = LocalTime.of(17, 0),
        endTime     = LocalTime.of(20, 0),
        location    = "Studio Tonart · Lane 6",
        distanceKm  = 0.9,
        description = "Two hours at the wheel, beginners welcome. All clay and aprons provided. Take home your favorite piece next week, glazed.",
        organizer   = "Studio Tonart",
        priceLabel  = "€28",
        link        = "tonart.nh/throw",
    ),
)

// ── MockEventRepository ───────────────────────────────────────────────────────

/**
 * In-memory implementation for Compose previews and dev builds.
 * Uses [MOCK_EVENTS] and a [MutableStateFlow] for saved IDs.
 */
class MockEventRepository : EventRepository {
    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())

    override fun observeEvents(): Flow<List<Event>> = flowOf(MOCK_EVENTS)

    override fun observeLoadAttempted(): Flow<Boolean> = flowOf(true)

    override suspend fun refresh() { /* no-op: mock data never changes */ }

    override fun observeSavedIds(): Flow<Set<String>> = _savedIds.asStateFlow()

    override suspend fun toggleSaved(id: String) {
        _savedIds.update { ids -> if (id in ids) ids - id else ids + id }
    }

    override suspend fun fetchDetail(eventUrl: String): EventDetail? = null
}
