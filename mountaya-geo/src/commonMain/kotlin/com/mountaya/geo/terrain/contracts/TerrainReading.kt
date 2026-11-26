package com.mountaya.geo

/**
 * TerrainReading is the terrain read off the elevation tiles at a point (`geo.terrain.elevation-sample`): the
 * ground's height there and the slope and aspect its four neighbours give.
 *
 * Two are equal where they hold the same numbers, `NaN` the same as `NaN` and 0 another than -0, as every value of
 * the geo layer compares.
 */
public class TerrainReading(
    /** The height at the point, in meters. */
    public val elevation: Double,
    /** The angle of the ground from the horizontal, in degrees: 0 on flat ground. */
    public val slope: Double,
    /** The compass bearing the ground falls towards, in degrees within [0, 360), or null on flat ground. */
    public val aspect: Double?,
) {
    override fun equals(other: Any?): Boolean =
        other is TerrainReading && sameNumber(other.elevation, elevation) && sameNumber(other.slope, slope) &&
            sameNumberOrNull(other.aspect, aspect)

    override fun hashCode(): Int = (elevation.hashCode() * 31 + slope.hashCode()) * 31 + aspect.hashCode()

    override fun toString(): String = "TerrainReading(elevation=$elevation, slope=$slope, aspect=$aspect)"
}
