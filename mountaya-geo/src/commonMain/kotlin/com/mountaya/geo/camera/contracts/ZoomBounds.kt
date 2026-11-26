package com.mountaya.geo

/**
 * ZoomBounds is the zoom range a map's camera is held to, the least and the greatest zoom it may
 * take, as a renderer's minimum and maximum zoom take them.
 *
 * Two are equal where they hold the same numbers, `NaN` the same as `NaN` and 0 another than -0, as
 * every value of the geo layer compares.
 */
public class ZoomBounds(
    /** The least zoom, the farthest the camera may pull out. */
    public val minZoom: Double,
    /** The greatest zoom, the closest the camera may come in. */
    public val maxZoom: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is ZoomBounds && sameNumber(other.minZoom, minZoom) && sameNumber(other.maxZoom, maxZoom)

    override fun hashCode(): Int = minZoom.hashCode() * 31 + maxZoom.hashCode()

    override fun toString(): String = "ZoomBounds(minZoom=$minZoom, maxZoom=$maxZoom)"
}
