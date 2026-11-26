package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ItineraryPlace is a waypoint of an itinerary as an element draws it: a place on the itinerary, where
 * it stands, its name, its colour, the glyph it is pinned with, and what it says about the place it
 * marks. An itinerary's places are `itineraryWaypoints`.
 *
 * A place marks an itinerary where a leg divides it: it is a pin on a profile's curve and a name under
 * its axis, placed by its point, since a waypoint's `position` is the API's ordinal and states no
 * distance (`resolveWaypoints`). Its name, its colour and its glyph are carried as the wire states
 * them.
 *
 * Two are equal where they hold the same texts, the same position, each number as the same number, and
 * the same details, as every value of the geo layer compares.
 */
public class ItineraryPlace(
    /** The waypoint's own id. */
    public val id: String,
    /** Where the place stands, `[longitude, latitude]` in degrees, with no altitude. */
    public val lngLat: List<Double>,
    /** The place's name, null for a bare marker, which has none. */
    public val name: String? = null,
    /** The colour the place is pinned in, painted as given, null where it states none. */
    public val color: String? = null,
    /** The icon the place is pinned with, which names a glyph of the set it is drawn from, null where it states none. */
    public val glyph: RestSchema.WaypointIcon? = null,
    /** What the waypoint says about the place, null where it says nothing. */
    public val details: WaypointDetails? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is ItineraryPlace &&
            other.id == id &&
            other.lngLat == lngLat &&
            other.name == name &&
            other.color == color &&
            other.glyph == glyph &&
            other.details == details

    override fun hashCode(): Int = listOf(id, lngLat, name, color, glyph, details).hashCode()

    override fun toString(): String =
        "ItineraryPlace(id=$id, lngLat=$lngLat, name=$name, color=$color, glyph=$glyph, details=$details)"
}
