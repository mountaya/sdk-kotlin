package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * AmenityFilter is what a waypoint table filtered by its places' services shows (`geo.profile.amenity-filter`):
 * the services a reader can pick, one chip each, in the order the reader's names for them sort, and the rows a
 * choice of them leaves.
 *
 * Two are equal where they offer the same services, in the same order, over the same rows.
 */
public class AmenityFilter(
    /** Each service the places offer, once, sorted by the reader's name for it. */
    public val available: List<RestSchema.WaypointAmenity>,
    /** The rows whose place offers every service picked, in their order. */
    public val shown: List<ItineraryWaypointRow>,
) {
    override fun equals(other: Any?): Boolean =
        other is AmenityFilter && other.available == available && other.shown == shown

    override fun hashCode(): Int = available.hashCode() * 31 + shown.hashCode()

    override fun toString(): String = "AmenityFilter(available=$available, shown=$shown)"
}
