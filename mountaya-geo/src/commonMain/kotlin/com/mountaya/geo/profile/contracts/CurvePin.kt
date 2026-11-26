package com.mountaya.geo

/**
 * CurvePin is one pass of a place pinned on a profile's curve (`geo.profile.curve-placement`): its key, the
 * place, where it stands on the axis and in the plot, and the curve's altitude under it, read on the line
 * between the two samples either side, so a pin stands on the stroke.
 *
 * Two are equal where they hold the same key, written alike, the same place and the same numbers, as every
 * value of the geo layer compares.
 */
public class CurvePin(
    /** The key, unique per pass: `id#pass`, counting the place's passes from zero. */
    public val key: String,
    /** The place pinned. */
    public val place: ProfilePlace,
    /** The meters from the start of the itinerary. */
    public val distance: Double,
    /** How far across the plot the pin stands. */
    public val x: Double,
    /** How far down the plot the pin stands, the curve's height at its distance. */
    public val y: Double,
    /** The curve's altitude under the pin, in meters above sea level. */
    public val altitude: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is CurvePin &&
            other.key == key &&
            other.place == place &&
            sameNumber(other.distance, distance) &&
            sameNumber(other.x, x) &&
            sameNumber(other.y, y) &&
            sameNumber(other.altitude, altitude)

    override fun hashCode(): Int = listOf(key, place, distance, x, y, altitude).hashCode()

    override fun toString(): String =
        "CurvePin(key=$key, place=$place, distance=$distance, x=$x, y=$y, altitude=$altitude)"
}
