package com.mountaya.geo

/**
 * MapViewport is where a map's camera opens: the position it is centred on and how far it is zoomed.
 *
 * Two viewports are equal where they hold the same numbers, `NaN` the same as `NaN` and 0 another
 * than -0, as every value of the geo layer compares.
 */
public class MapViewport(
    /** The position the camera is centred on, `[longitude, latitude]` in degrees, as the wire orders a position. */
    public val center: List<Double>,
    /** The zoom level, on the scale every map of this SDK reads: 0 shows the whole world. */
    public val zoom: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is MapViewport && sameNumbers(other.center, center) && sameNumber(other.zoom, zoom)

    override fun hashCode(): Int = center.hashCode() * 31 + zoom.hashCode()

    override fun toString(): String = "MapViewport(center=$center, zoom=$zoom)"
}
