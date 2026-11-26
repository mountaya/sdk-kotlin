package com.mountaya.geo

import kotlin.time.Instant

/**
 * CurveCutoff is one pass of a place that publishes a cut-off, marked across a profile
 * (`geo.profile.curve-placement`): its key, the place, where it stands on the axis and across the plot, and
 * the instant by which the place must be left, carried unwritten, since writing it needs a time zone only the
 * caller holds. A cut-off needs no height, so one stands on an itinerary with no elevation too.
 *
 * Two are equal where they hold the same key, written alike, the same place, and the same numbers and
 * instant, as every value of the geo layer compares.
 */
public class CurveCutoff(
    /** The key, unique per pass: `id#pass`, counting the place's passes from zero. */
    public val key: String,
    /** The place marked. */
    public val place: ProfilePlace,
    /** The meters from the start of the itinerary. */
    public val distance: Double,
    /** How far across the plot the mark stands. */
    public val x: Double,
    /** The instant by which the place must be left. */
    public val cutoffAt: Instant,
) {
    override fun equals(other: Any?): Boolean =
        other is CurveCutoff &&
            other.key == key &&
            other.place == place &&
            sameNumber(other.distance, distance) &&
            sameNumber(other.x, x) &&
            other.cutoffAt == cutoffAt

    override fun hashCode(): Int = listOf(key, place, distance, x, cutoffAt).hashCode()

    override fun toString(): String =
        "CurveCutoff(key=$key, place=$place, distance=$distance, x=$x, cutoffAt=$cutoffAt)"
}
