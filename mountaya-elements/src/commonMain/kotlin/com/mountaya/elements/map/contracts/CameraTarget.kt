package com.mountaya.elements

import com.mountaya.geo.CameraPose

/**
 * CameraTarget is where a camera move takes the camera: the members it states, each null to keep the
 * camera's own (`map.camera.pose`).
 *
 * [center] is `[longitude, latitude]` in degrees, two finite numbers with the latitude from -90 to 90, a
 * longitude past the antimeridian passed on as written, since a renderer reads a longitude a turn away as
 * the same meridian; [zoom], [bearing] and [pitch] are finite numbers. A move is refused before anything
 * moves where a member it states is not one, `MapEngine.jump`, `ease` and `fly` alike. A renderer holds the
 * camera within its zoom window and the latitudes it draws as it moves there.
 *
 * Two are equal where they state the same members, each number as the same number, `NaN` the same as
 * `NaN` and 0 another than -0, as every Geo value compares.
 */
public class CameraTarget(
    /** The position the camera moves to, or null to keep its own. */
    public val center: List<Double>? = null,
    /** The zoom the camera moves to, or null to keep its own. */
    public val zoom: Double? = null,
    /** The compass heading the top of the frame turns to, in degrees, or null to keep it. */
    public val bearing: Double? = null,
    /** The tilt the camera turns to, in degrees from looking straight down, or null to keep it. */
    public val pitch: Double? = null,
) {
    /** [camera] with every member this target states in place of its own. */
    internal fun applied(camera: CameraPose): CameraPose =
        CameraPose(center ?: camera.center, zoom ?: camera.zoom, pitch ?: camera.pitch, bearing ?: camera.bearing)

    override fun equals(other: Any?): Boolean =
        other is CameraTarget &&
            other.center == center &&
            sameMember(other.zoom, zoom) &&
            sameMember(other.bearing, bearing) &&
            sameMember(other.pitch, pitch)

    override fun hashCode(): Int {
        var hash = center?.hashCode() ?: 0
        hash = 31 * hash + (zoom?.toBits()?.hashCode() ?: 0)
        hash = 31 * hash + (bearing?.toBits()?.hashCode() ?: 0)
        hash = 31 * hash + (pitch?.toBits()?.hashCode() ?: 0)
        return hash
    }

    override fun toString(): String = "CameraTarget(center=$center, zoom=$zoom, bearing=$bearing, pitch=$pitch)"
}

/** Whether [a] and [b] are both absent or the same number, its bits compared, every `NaN` read as one. */
private fun sameMember(
    a: Double?,
    b: Double?,
): Boolean = if (a == null || b == null) a == null && b == null else a.toBits() == b.toBits()
