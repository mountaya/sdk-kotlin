package com.mountaya.elements

import com.mountaya.geo.GlyphSet

/** A marker pin's diameter, its ring included, in density-independent pixels. */
internal const val MARKER_PIN_DIAMETER: Double = 28.0

/** The width of the white ring a marker pin is drawn inside, in density-independent pixels. */
internal const val MARKER_PIN_RING_WIDTH: Double = 3.0

/** The side of the square a marker pin's glyph is drawn in, in density-independent pixels. */
internal const val MARKER_PIN_GLYPH_SIZE: Double = 16.0

/** The diameter of the dot a marker pin with no glyph shows, in density-independent pixels. */
internal const val MARKER_PIN_DOT_SIZE: Double = 8.0

/** The colour of a marker pin's ring and of what it carries at its centre. */
internal const val MARKER_PIN_MARK_COLOR: String = "#ffffff"

/**
 * describeMarkerPin describes the pin a marker given no view of its own is drawn as (`map.marker.pin`): a
 * disc 28 across in [color], the map's accent where it is null, ringed in white 3 wide, with its glyph
 * [glyph] in white in a square of 16 at its centre, or a white dot 8 across where it names no glyph or [set]
 * cannot draw the one it names. It is the disc the map's sprites are, so a marker reads as one of the map's
 * own pins.
 */
internal fun describeMarkerPin(
    color: String?,
    glyph: String?,
    set: GlyphSet,
): MarkerPin {
    val centre =
        if (glyph != null && set.markup[glyph] != null) {
            MarkerPin.Centre.Glyph(glyph, MARKER_PIN_GLYPH_SIZE, MARKER_PIN_MARK_COLOR)
        } else {
            MarkerPin.Centre.Dot(MARKER_PIN_DOT_SIZE, MARKER_PIN_MARK_COLOR)
        }
    return MarkerPin(MARKER_PIN_DIAMETER, color, MARKER_PIN_MARK_COLOR, MARKER_PIN_RING_WIDTH, centre)
}
