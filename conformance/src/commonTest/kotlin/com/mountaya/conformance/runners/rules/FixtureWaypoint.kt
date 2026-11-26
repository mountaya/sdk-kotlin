package com.mountaya.conformance

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/**
 * A waypoint as the REST API answers one, holding the members a case states and a placeholder for
 * every other the type requires, which no profile rule reads.
 */
internal fun fixtureWaypoint(
    amenities: List<String> = emptyList(),
    description: String? = null,
    timing: RestSchema.WaypointTiming? = null,
    contact: RestSchema.WaypointContact? = null,
    id: String = "waypoint",
    name: String? = null,
    position: Int = 0,
    geojson: GeoJsonGeometry? = null,
    mode: RestSchema.WaypointMode = RestSchema.WaypointMode.Point,
): RestSchema.ItineraryWaypoint =
    RestSchema.ItineraryWaypoint(
        amenities = amenities.map { RestSchema.WaypointAmenity(it) },
        color = null,
        contact = contact,
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        description = description,
        geojson = geojson,
        icon = null,
        id = id,
        itinerary_id = "itinerary",
        media = null,
        mode = mode,
        name = name,
        position = position,
        properties = null,
        revision = 1,
        timing = timing,
    )
