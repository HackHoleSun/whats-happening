package com.whatshappening.novisad.data

import androidx.compose.ui.graphics.Color
import com.whatshappening.novisad.ui.theme.CatppuccinPalette

/**
 * The ten event categories sourced directly from the scraper feed.
 * [id] matches the raw Serbian string in the JSON so [fromId] needs no mapping table.
 * [displayName] is the user-facing Serbian label shown in the UI.
 */
enum class EventCategory(
    val id: String,
    val displayName: String,
    val glyph: String,
) {
    Festival    ("Festival",     "Festival",      "★"),
    Film        ("Film",         "Film",          "▶"),
    Exhibition  ("Izložba",      "Izložba",       "◆"),
    Book        ("Knjiga",       "Knjiga",        "◉"),
    Concert     ("Koncert",      "Koncert",       "♫"),
    Nightlife   ("Noćni provod", "Noćni provod",  "☽"),
    Lecture     ("Predavanje",   "Predavanje",    "◬"),
    Performance ("Predstava",    "Predstava",     "☻"),
    Workshop    ("Radionica",    "Radionica",     "✦"),
    Sport       ("Sport",        "Sport",         "●");

    /** Single accent colour for chips, badges, icon tints. */
    fun hue(palette: CatppuccinPalette): Color = when (this) {
        Festival    -> palette.mauve
        Film        -> palette.sapphire
        Exhibition  -> palette.pink
        Book        -> palette.lavender
        Concert     -> palette.mauve
        Nightlife   -> palette.overlay2
        Lecture     -> palette.blue
        Performance -> palette.teal
        Workshop    -> palette.yellow
        Sport       -> palette.red
    }

    /**
     * Multi-stop gradient colours for the hero placeholder.
     */
    fun gradientStops(palette: CatppuccinPalette): List<Color> = when (this) {
        Festival    -> listOf(palette.pink, palette.mauve, palette.blue)
        Film        -> listOf(palette.sapphire, palette.blue)
        Exhibition  -> listOf(palette.pink, palette.rosewater)
        Book        -> listOf(palette.lavender, palette.blue)
        Concert     -> listOf(palette.mauve, palette.pink)
        Nightlife   -> listOf(palette.mauve, palette.overlay2)
        Lecture     -> listOf(palette.blue, palette.sky)
        Performance -> listOf(palette.teal, palette.green)
        Workshop    -> listOf(palette.yellow, palette.peach)
        Sport       -> listOf(palette.red, palette.maroon)
    }

    companion object {
        /** Null-safe lookup by [id] string coming from the scraper feed. */
        fun fromId(id: String?): EventCategory? =
            entries.firstOrNull { it.id == id?.trim() }
    }
}
