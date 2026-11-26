package com.mountaya.elements

import com.mountaya.geo.GlyphSet
import com.mountaya.geo.WAYPOINT_ICON_PREFIX

/**
 * The namespace a point of interest's sprite id opens with, as the Tile API's point-of-interest layers
 * name one: `poi-icon-`, the glyph, a hyphen and the colour's six hexadecimal digits.
 */
internal const val POI_ICON_PREFIX: String = "poi-icon-"

/**
 * The namespaces of the image ids a map answers with a marker sprite: a waypoint's, `section-icon-`, and a
 * point of interest's, `poi-icon-`, both the API's own.
 */
internal val markerImagePrefixes: List<String> = listOf(WAYPOINT_ICON_PREFIX, POI_ICON_PREFIX)

/**
 * poiIconImageId answers the id of the marker sprite of [glyph] on a disc of [color], as a point of interest's layer
 * names one: `poi-icon-`, the glyph, a hyphen, then the colour with one leading `#` dropped and its ASCII capitals
 * written small, so `mountain` in `#5D4037` is `poi-icon-mountain-5d4037`, which [readMarkerImageId] reads back. Every
 * other character of the colour is kept as written, so a colour that is no hexadecimal colour names a sprite the map
 * answers none for.
 */
internal fun poiIconImageId(
    glyph: String,
    color: String,
): String {
    val digits = color.removePrefix("#")
    return POI_ICON_PREFIX + glyph + "-" + digits.map { if (it in 'A'..'Z') it + ('a' - 'A') else it }.joinToString("")
}

/** The length of an id's tail after its glyph's name: a hyphen and six hexadecimal digits. */
private const val COLOR_TAIL = 7

/**
 * readMarkerImageId answers the marker sprite the image id [id] names, drawn from [set], or null for an id
 * that names none (`map.icons.missing-image`).
 *
 * An id names one where it is one of [markerImagePrefixes] followed by a glyph name, a hyphen and six
 * hexadecimal digits in either case, and the set holds that glyph. The name is everything before the
 * hyphen the six digits follow, so a name of several words, `road-access-permit`, is read whole, and holds
 * no line terminator, as a pattern's `.` reads none. The colour is `#` and the six digits as the id writes
 * them. Any other id is left missing: one under no prefix, one whose tail is no name, hyphen and six
 * digits, and one naming a glyph the set does not hold, which would draw a blank disc. Every character the
 * prefix and the tail are read for is one UTF-16 unit, so the id is read unit by unit as it would be code
 * point by code point.
 */
internal fun readMarkerImageId(
    id: String,
    set: GlyphSet,
): MarkerImage? {
    val prefix = markerImagePrefixes.firstOrNull { id.startsWith(it) } ?: return null
    val tail = id.substring(prefix.length)
    // A glyph name of one character at least, a hyphen, and six hexadecimal digits.
    if (tail.length < COLOR_TAIL + 1) return null
    val hex = tail.takeLast(COLOR_TAIL - 1)
    val glyph = tail.dropLast(COLOR_TAIL)
    if (tail[tail.length - COLOR_TAIL] != '-' || !hex.all(::isHexDigit) || glyph.any(::isLineTerminator)) return null
    if (glyph !in set.markup) return null
    return MarkerImage(glyph, "#$hex")
}

/** Whether [char] is a hexadecimal digit in either case: `0` to `9`, `a` to `f`, `A` to `F`. */
private fun isHexDigit(char: Char): Boolean = char in '0'..'9' || char in 'a'..'f' || char in 'A'..'F'

/** Whether [char] ends a line as a pattern's `.` reads one: a line feed, a carriage return, or the line or paragraph separator. */
private fun isLineTerminator(char: Char): Boolean = char == '\n' || char == '\r' || char == ' ' || char == ' '
