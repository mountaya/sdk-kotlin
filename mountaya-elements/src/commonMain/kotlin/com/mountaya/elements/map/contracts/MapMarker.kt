package com.mountaya.elements

/**
 * MapMarker is a pin a map shows at a place, above everything it draws: its id, its position, its
 * colour, the glyph at its centre and the label a click on it shows (`map.marker.pin`).
 *
 * A marker is the map's own, never a layer of its style: it takes no rank, no base map's change takes
 * it off, and its label is shown as it is written, in no language the map relabels. A click on it is
 * told to `MapView`'s `onClick` with its id.
 *
 * Two are equal where every member is the same, a position by the relation every Geo value compares
 * numbers by, `NaN` the same as `NaN` and 0 another than -0, which a `List<Double>` already compares
 * by, and a text as it is written.
 */
public class MapMarker(
    /** The marker's name, which a command naming it finds it by, as it is written. */
    public val id: String,
    /**
     * Where the marker stands, `[longitude, latitude]`, as the wire orders a position, and an altitude after
     * them, which a marker drawn flat on the map ignores.
     */
    public val position: List<Double>,
    /** The pin's colour, a CSS hexadecimal colour, `#rgb`, `#rrggbb` or `#rrggbbaa`, or null for the map's accent. */
    public val color: String? = null,
    /**
     * The glyph drawn in white at the pin's centre, a name of the map's glyph set, or null for a white dot,
     * which the pin also shows where the set cannot draw the glyph named.
     */
    public val glyph: String? = null,
    /** The text a click on the marker shows beside it, or null for none. */
    public val label: String? = null,
) {
    /** This marker with the members given in place of its own, every other as it is. */
    internal fun copy(
        position: List<Double> = this.position,
        color: String? = this.color,
        glyph: String? = this.glyph,
        label: String? = this.label,
    ): MapMarker = MapMarker(id, position, color, glyph, label)

    override fun equals(other: Any?): Boolean =
        other is MapMarker &&
            other.id == id &&
            other.position == position &&
            other.color == color &&
            other.glyph == glyph &&
            other.label == label

    override fun hashCode(): Int {
        var hash = id.hashCode()
        hash = 31 * hash + position.hashCode()
        hash = 31 * hash + (color?.hashCode() ?: 0)
        hash = 31 * hash + (glyph?.hashCode() ?: 0)
        hash = 31 * hash + (label?.hashCode() ?: 0)
        return hash
    }

    override fun toString(): String = "MapMarker(id=$id, position=$position, color=$color, glyph=$glyph, label=$label)"
}
