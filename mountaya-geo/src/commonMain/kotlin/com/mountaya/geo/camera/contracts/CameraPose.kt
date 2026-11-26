package com.mountaya.geo

/**
 * CameraPose is one frame of a camera: the position it is centred on, how far it is zoomed, how
 * far it is tilted from looking straight down, and the compass heading the top of the frame faces.
 *
 * Two poses are equal where every field holds the same numbers, `NaN` the same as `NaN` and 0
 * another than -0, as every value of the geo layer compares; no tolerance is applied.
 */
public class CameraPose(
    /** The position the camera is centred on, `[longitude, latitude]` in degrees, as the wire orders a position. */
    public val center: List<Double>,
    /** The zoom level, on the scale every map of this SDK reads: 0 shows the whole world. */
    public val zoom: Double,
    /** The tilt from looking straight down, in degrees: 0 looks at the ground from above. */
    public val pitch: Double,
    /** The compass heading the top of the frame faces, in degrees: 0 north and 90 east. */
    public val bearing: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is CameraPose &&
            sameNumbers(other.center, center) &&
            sameNumber(other.zoom, zoom) &&
            sameNumber(other.pitch, pitch) &&
            sameNumber(other.bearing, bearing)

    override fun hashCode(): Int =
        ((center.hashCode() * 31 + zoom.hashCode()) * 31 + pitch.hashCode()) * 31 + bearing.hashCode()

    override fun toString(): String = "CameraPose(center=$center, zoom=$zoom, pitch=$pitch, bearing=$bearing)"
}
