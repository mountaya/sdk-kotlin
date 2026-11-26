package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.generated.RestSchema

/**
 * ResolvedItinerary is every projection of one itinerary an element draws, read from one answer at once:
 * the geometry its profile walks, the figures the server stated, its legs and its waypoints. Each is a
 * projection of what `getItinerary` answers, so an element handed an id draws all four from one read,
 * and an app that holds the itinerary already reads them the same way, `ResolvedItinerary(itinerary)`.
 *
 * Every projection reads the segments in chain order: the geometry is `itineraryGeometry`, the figures
 * `profileCharacteristics` over the segments and every waypoint, its planned stops included, the legs
 * `itinerarySegments` and the waypoints `itineraryWaypoints`. Each is computed once, as the value is
 * built.
 *
 * Two are equal where every projection is; the engines showing one answer share one value, which is
 * compared first, so they compare no vertex.
 */
public class ResolvedItinerary private constructor(
    /** The one geometry the itinerary's profile walks, its segments' in chain order, null where no segment carries one. */
    public val geometry: GeoJsonGeometry?,
    /**
     * The summary the server's figures fold to: distance, duration, ascent and descent, the altitude
     * band, the slope range, the planned stops and the trail difficulty.
     */
    public val characteristics: ProfileCharacteristics,
    /** The itinerary's legs, a table's rows and a profile's chips. */
    public val legs: List<ItineraryLeg>,
    /** The itinerary's places, the pins on a profile's curve and the names under its axis. */
    public val waypoints: List<ItineraryPlace>,
) {
    /** Creates the projections of [itinerary], as the REST API answers it, reading each once. */
    public constructor(itinerary: RestSchema.ItineraryWithDetails) :
        this(inChainOrder(itinerary.segments) { it.position }, itinerary)

    private constructor(chain: List<RestSchema.ItinerarySegment>, itinerary: RestSchema.ItineraryWithDetails) :
        this(
            itineraryGeometry(chain),
            profileCharacteristics(chain, itinerary.waypoints),
            itinerarySegments(chain, itinerary.groups),
            itineraryWaypoints(itinerary.waypoints),
        )

    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is ResolvedItinerary &&
                    other.geometry == geometry &&
                    other.characteristics == characteristics &&
                    other.legs == legs &&
                    other.waypoints == waypoints
            )

    override fun hashCode(): Int = listOf(geometry, characteristics, legs, waypoints).hashCode()

    override fun toString(): String =
        "ResolvedItinerary(geometry=$geometry, characteristics=$characteristics, legs=$legs, waypoints=$waypoints)"
}
