package com.mountaya.geo

import kotlin.math.floor

/**
 * DEFAULT_SEGMENT_COLOR is the colour a segment is drawn in when it carries none of its own, a neutral grey.
 * It is a hex colour rather than a theme's token, since a renderer's paint takes the colour itself.
 */
public const val DEFAULT_SEGMENT_COLOR: String = "#868e96"

/**
 * livelyPalette is the palette a segment's colour is read from where its caller names none: twelve
 * hues stepping once around the wheel (red, pink, grape, violet, indigo, blue, cyan, teal, green,
 * lime, yellow, orange), so two neighbouring positions are easy to tell apart on a map.
 */
public val livelyPalette: List<String> =
    listOf(
        "#fa5252",
        "#e64980",
        "#be4bdb",
        "#7950f2",
        "#4c6ef5",
        "#228be6",
        "#15aabf",
        "#12b886",
        "#40c057",
        "#82c91e",
        "#fab005",
        "#fd7e14",
    )

/** The palette [palette] lists, or [livelyPalette] where it lists no colour. */
internal fun listedOr(palette: List<String>): List<String> = palette.ifEmpty { livelyPalette }

/** The colour of [palette], which lists one at least, at the whole [position] wrapped into it. */
internal fun colorAt(
    palette: List<String>,
    position: Int,
): String = palette[position.mod(palette.size)]

/**
 * segmentColorAt answers the colour of [palette] a segment at the 0-based [position] is drawn in,
 * the palette wrapped: a position past the end reads on from the first colour, and one before the
 * first reads back from the last, so 12 is the first colour of the twelve of [livelyPalette] and -1
 * its last. A palette that lists no colour reads as [livelyPalette], so the answer is always a
 * colour of the palette read.
 */
public fun segmentColorAt(
    position: Int,
    palette: List<String> = livelyPalette,
): String = colorAt(listedOr(palette), position)

/**
 * segmentColorAt answers the colour of [palette] at [position], as the whole position does, for a
 * position that is any number: a fraction reads the position it is past, so 2.7 reads 2 and -0.5
 * reads -1, and a number that is none, `NaN` or an infinity, reads the first colour.
 *
 * The position is wrapped as the number it is, `((index % size) + size) % size` on the whole part,
 * so a position past the range of an `Int` wraps as exactly as one within it.
 */
public fun segmentColorAt(
    position: Double,
    palette: List<String> = livelyPalette,
): String {
    val listed = listedOr(palette)
    val index = if (position.isFinite()) floor(position) else 0.0
    val size = listed.size.toDouble()
    return listed[((index % size + size) % size).toInt()]
}
