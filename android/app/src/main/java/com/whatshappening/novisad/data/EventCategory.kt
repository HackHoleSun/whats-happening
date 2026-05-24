package com.whatshappening.novisad.data

import androidx.compose.ui.graphics.Color
import com.whatshappening.novisad.ui.theme.CatppuccinPalette

/**
 * The nine event categories, each tied to a Catppuccin hue and a single-char glyph
 * used as the hero placeholder marker.
 */
enum class EventCategory(
    val id: String,
    val displayName: String,
    val glyph: String,
) {
    Music     ("music",     "Music",     "♫"),
    Food      ("food",      "Food",      "◉"),
    Art       ("art",       "Art",       "◆"),
    Tech      ("tech",      "Tech",      "◬"),
    Outdoor   ("outdoor",   "Outdoor",   "▲"),
    Sports    ("sports",    "Sports",    "●"),
    Film      ("film",      "Film",      "▶"),
    Comedy    ("comedy",    "Comedy",    "☻"),
    Community ("community", "Community", "✦");

    /** Single accent colour for chips, badges, icon tints. */
    fun hue(palette: CatppuccinPalette): Color = when (this) {
        Music     -> palette.mauve
        Food      -> palette.peach
        Art       -> palette.pink
        Tech      -> palette.blue
        Outdoor   -> palette.green
        Sports    -> palette.red
        Film      -> palette.sapphire
        Comedy    -> palette.yellow
        Community -> palette.teal
    }

    /**
     * Multi-stop gradient colours for the hero placeholder.
     * Music intentionally has three stops to match the artboard.
     */
    fun gradientStops(palette: CatppuccinPalette): List<Color> = when (this) {
        Music     -> listOf(palette.pink, palette.mauve, palette.blue)
        Food      -> listOf(palette.peach, palette.yellow)
        Art       -> listOf(palette.pink, palette.rosewater)
        Tech      -> listOf(palette.blue, palette.sky)
        Outdoor   -> listOf(palette.green, palette.teal)
        Sports    -> listOf(palette.red, palette.maroon)
        Film      -> listOf(palette.sapphire, palette.blue)
        Comedy    -> listOf(palette.yellow, palette.peach)
        Community -> listOf(palette.teal, palette.green)
    }

    companion object {
        /** Null-safe lookup by [id] string coming from the scraper feed. */
        fun fromId(id: String?): EventCategory? = entries.firstOrNull { it.id == id }
    }
}
