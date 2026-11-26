package com.mountaya.elements

/**
 * CursorMarkerPin is what the marker an element added for its cursor is drawn with (`map.profile.cursor-marker`):
 * its colour, null for the map's own accent, and its glyph. An element holds it while its marker stands, and null
 * where it added none, so it never takes down a marker it did not add.
 *
 * Two are equal where both are written alike, or the colour absent on both.
 */
public class CursorMarkerPin(
    /** The pin's colour, null for the map's own accent. */
    public val color: String?,
    /** The name of the glyph the pin carries. */
    public val glyph: String,
) {
    override fun equals(other: Any?): Boolean = other is CursorMarkerPin && other.color == color && other.glyph == glyph

    override fun hashCode(): Int = color.hashCode() * 31 + glyph.hashCode()

    override fun toString(): String = "CursorMarkerPin(color=$color, glyph=$glyph)"
}
