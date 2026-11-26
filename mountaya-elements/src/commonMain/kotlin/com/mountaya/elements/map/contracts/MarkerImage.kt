package com.mountaya.elements

/**
 * MarkerImage is the marker sprite an image id names (`map.icons.missing-image`): the glyph it carries
 * and the colour of its disc.
 *
 * Two are equal where both are written with the same characters.
 */
internal class MarkerImage(
    /** The set's name for the glyph the sprite carries. */
    val glyph: String,
    /** The disc's colour: `#` and the id's six hexadecimal digits, in the case the id writes them. */
    val color: String,
) {
    override fun equals(other: Any?): Boolean = other is MarkerImage && other.glyph == glyph && other.color == color

    override fun hashCode(): Int = glyph.hashCode() * 31 + color.hashCode()

    override fun toString(): String = "MarkerImage(glyph=$glyph, color=$color)"
}
