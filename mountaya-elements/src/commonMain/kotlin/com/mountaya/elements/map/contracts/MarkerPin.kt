package com.mountaya.elements

/**
 * MarkerPin is what a marker given no view of its own is drawn as: a disc of its [diameter] in its [fill],
 * ringed, with a glyph or a dot at its [centre] (`map.marker.pin`).
 */
internal data class MarkerPin(
    /** The disc's diameter, its ring included, in pixels. */
    val diameter: Double,
    /** The disc's colour, the marker's own, or null for the map's accent. */
    val fill: String?,
    /** The colour of the ring around the disc. */
    val ringColor: String,
    /** The ring's width, in pixels. */
    val ringWidth: Double,
    /** What the disc carries at its centre. */
    val centre: Centre,
) {
    /** Centre is what a pin's disc carries at its centre. */
    sealed interface Centre {
        /** The marker's [glyph], of the glyph set the map draws with, in a square of [size] pixels, in [color]. */
        data class Glyph(
            val glyph: String,
            val size: Double,
            val color: String,
        ) : Centre

        /** A dot of diameter [size] pixels in [color], where the marker names no glyph or the set cannot draw the one it names. */
        data class Dot(
            val size: Double,
            val color: String,
        ) : Centre
    }
}
