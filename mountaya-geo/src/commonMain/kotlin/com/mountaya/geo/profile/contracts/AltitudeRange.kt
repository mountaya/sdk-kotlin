package com.mountaya.geo

/**
 * AltitudeRange is a stretch's altitude band, its lowest and its highest altitude in meters above sea level, as a
 * segment table states it where both are known (`geo.profile.segment-table-cells`) and `formatAltitudeRange`
 * writes it.
 *
 * Two are equal where they hold the same numbers, as every value of the geo layer compares.
 */
public class AltitudeRange(
    /** The lowest altitude, in meters above sea level. */
    public val min: Double,
    /** The highest altitude, in meters above sea level. */
    public val max: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is AltitudeRange && sameNumber(other.min, min) && sameNumber(other.max, max)

    override fun hashCode(): Int = min.hashCode() * 31 + max.hashCode()

    override fun toString(): String = "AltitudeRange(min=$min, max=$max)"
}
