package com.mountaya.geo

/**
 * StatStripCursor is where a scrub stands, as the stat strip reads it (`geo.profile.stat-strip`): the distance
 * and the altitude of the sample the cursor stands on, and the grade there.
 *
 * Two are equal where they hold the same numbers, as every value of the geo layer compares.
 */
public class StatStripCursor(
    /** The meters from the start of the itinerary of the sample the cursor stands on. */
    public val distance: Double,
    /** That sample's altitude, in meters above sea level. */
    public val elevation: Double,
    /** The grade there, in degrees, null where none reads. */
    public val grade: Double?,
) {
    override fun equals(other: Any?): Boolean =
        other is StatStripCursor &&
            sameNumber(other.distance, distance) &&
            sameNumber(other.elevation, elevation) &&
            sameNumberOrNull(other.grade, grade)

    override fun hashCode(): Int = (distance.hashCode() * 31 + elevation.hashCode()) * 31 + grade.hashCode()

    override fun toString(): String = "StatStripCursor(distance=$distance, elevation=$elevation, grade=$grade)"
}
