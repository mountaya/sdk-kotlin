package com.mountaya.geo

/**
 * CurveDivider is one pass of a place that divides an itinerary, named under its profile's distance axis
 * (`geo.profile.curve-placement`): its key, the place, where it stands on the axis and across the plot, and
 * how far down the plot the curve stands there, where a drop line from its name ends, null where the series
 * has no sample.
 *
 * Two are equal where they hold the same key, written alike, the same place and the same numbers, as every
 * value of the geo layer compares.
 */
public class CurveDivider(
    /** The key, unique per pass: `id#pass`, counting the place's passes from zero. */
    public val key: String,
    /** The place named. */
    public val place: ProfilePlace,
    /** The meters from the start of the itinerary. */
    public val distance: Double,
    /** How far across the plot the name stands. */
    public val x: Double,
    /** How far down the plot the curve stands at the divider's distance, null with no sample. */
    public val y: Double?,
) {
    /** The name written under the axis, the place's, or empty for a place with none. */
    public val name: String get() = place.name ?: ""

    override fun equals(other: Any?): Boolean =
        other is CurveDivider &&
            other.key == key &&
            other.place == place &&
            sameNumber(other.distance, distance) &&
            sameNumber(other.x, x) &&
            sameNumberOrNull(other.y, y)

    override fun hashCode(): Int = listOf(key, place, distance, x, y).hashCode()

    override fun toString(): String = "CurveDivider(key=$key, place=$place, distance=$distance, x=$x, y=$y)"
}
