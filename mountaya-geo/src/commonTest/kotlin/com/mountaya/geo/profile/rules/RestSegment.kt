package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/** A segment as the REST API answers one, holding what a test states and a placeholder for the rest. */
internal fun restSegment(
    id: String = "segment",
    position: Int = 0,
    geojson: GeoJsonGeometry? = null,
    distance: Double? = null,
    duration: Double? = null,
    ascent: Int? = null,
    descent: Int? = null,
    altitude: Pair<Int?, Int?> = null to null,
    gradient: Pair<Double?, Double?> = null to null,
    groupId: String? = null,
    trailDifficulty: List<RestSchema.TrailDifficultyInfo>? = null,
    slope: List<RestSchema.SlopeInfo>? = null,
    roadAccess: List<RestSchema.RoadAccessInfo>? = null,
    name: String = id,
    color: String? = null,
): RestSchema.ItinerarySegment =
    RestSchema.ItinerarySegment(
        altitude_max = altitude.second,
        altitude_min = altitude.first,
        ascent = ascent,
        color = color,
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        descent = descent,
        description = null,
        distance = distance,
        duration = duration,
        geojson = geojson,
        gradient_max = gradient.second,
        gradient_min = gradient.first,
        group_id = groupId,
        id = id,
        itinerary_id = "itinerary",
        mode = RestSchema.SegmentMode.Linestring,
        name = name,
        position = position,
        revision = 1,
        road_access_info = roadAccess,
        slope_info = slope,
        speed = null,
        speed_info = null,
        start_at = null,
        steps = null,
        surface_info = null,
        trail_difficulty_info = trailDifficulty,
        way_type_info = null,
    )

/** A waypoint as the REST API answers one, holding what a test states and a placeholder for the rest. */
internal fun restWaypoint(
    amenities: List<String> = emptyList(),
    description: String? = null,
    timing: RestSchema.WaypointTiming? = null,
    contact: RestSchema.WaypointContact? = null,
    media: RestSchema.WaypointMedia? = null,
    properties: List<RestSchema.WaypointProperty>? = null,
): RestSchema.ItineraryWaypoint =
    RestSchema.ItineraryWaypoint(
        amenities = amenities.map { RestSchema.WaypointAmenity(it) },
        color = null,
        contact = contact,
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        description = description,
        geojson = null,
        icon = null,
        id = "waypoint",
        itinerary_id = "itinerary",
        media = media,
        mode = RestSchema.WaypointMode.Point,
        name = null,
        position = 0,
        properties = properties,
        revision = 1,
        timing = timing,
    )
