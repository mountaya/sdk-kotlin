package com.mountaya.geo

/**
 * PlacedWaypoint is one placement of a waypoint on an itinerary's distance axis: one pass of the
 * line, so a place the itinerary passes twice is placed twice.
 *
 * Two are equal where they hold the same key, the same waypoint and the same number, as every
 * value of the geo layer compares.
 */
public class PlacedWaypoint(
    /** The key, unique per placement: `id#pass`, counting the waypoint's passes from zero, `hut#1`. */
    public val key: String,
    /** The waypoint placed. */
    public val waypoint: ProfileWaypoint,
    /** Meters from the start of the itinerary. */
    public val distance: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is PlacedWaypoint && other.key == key && other.waypoint == waypoint &&
            sameNumber(other.distance, distance)

    override fun hashCode(): Int = (key.hashCode() * 31 + waypoint.hashCode()) * 31 + distance.hashCode()

    override fun toString(): String = "PlacedWaypoint(key=$key, waypoint=$waypoint, distance=$distance)"
}
