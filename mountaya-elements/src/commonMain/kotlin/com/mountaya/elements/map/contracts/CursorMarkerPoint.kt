package com.mountaya.elements

/**
 * CursorMarkerPoint is where an element's cursor stands and how its marker is drawn (`map.profile.cursor-marker`):
 * the id the marker is pinned under, the position of the sample the cursor stands on, `[longitude, latitude]`, its
 * colour, null for the map's own accent, and its glyph.
 *
 * Two are equal where every text is written alike and the positions are the same numbers, as the geo layer
 * compares them.
 */
public class CursorMarkerPoint(
    /** The id the marker is pinned under. */
    public val id: String,
    /** Where the cursor stands, `[longitude, latitude]`. */
    public val lngLat: List<Double>,
    /** The pin's colour, null for the map's own accent. */
    public val color: String? = null,
    /** The name of the glyph the pin carries. */
    public val glyph: String = CURSOR_MARKER_GLYPH,
) {
    override fun equals(other: Any?): Boolean =
        other is CursorMarkerPoint &&
            other.id == id &&
            other.lngLat == lngLat &&
            other.color == color &&
            other.glyph == glyph

    override fun hashCode(): Int = listOf(id, lngLat, color, glyph).hashCode()

    override fun toString(): String = "CursorMarkerPoint(id=$id, lngLat=$lngLat, color=$color, glyph=$glyph)"
}
