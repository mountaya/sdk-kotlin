package com.mountaya.elements

/**
 * MapTap is a click on a map: the place clicked, the point of the view it was clicked at, and the
 * marker clicked, where it was one. It is what `MapView`'s `onClick` is handed, the name every SDK
 * gives the value, whatever verb its ecosystem gives the handler.
 *
 * Two are equal where every member is the same, a number by the relation every Geo value compares
 * numbers by, which a `List<Double>` already compares by, and a text as it is written.
 */
public class MapTap(
    /** The place clicked, `[longitude, latitude]`, its longitude within [-180, 180] whichever copy of the world the click fell on. */
    public val position: List<Double>,
    /** Where the click fell in the view, `[x, y]` in density-independent pixels from its top left corner. */
    public val point: List<Double>,
    /** The id of the marker clicked, or null where the click fell on the map itself. */
    public val marker: String? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is MapTap && other.position == position && other.point == point && other.marker == marker

    override fun hashCode(): Int = (position.hashCode() * 31 + point.hashCode()) * 31 + (marker?.hashCode() ?: 0)

    override fun toString(): String = "MapTap(position=$position, point=$point, marker=$marker)"
}
