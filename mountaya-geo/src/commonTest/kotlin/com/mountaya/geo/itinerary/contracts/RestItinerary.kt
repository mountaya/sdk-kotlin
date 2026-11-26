package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/**
 * An itinerary as the REST API answers one, holding the segments, waypoints and groups a test states and
 * a placeholder for every member the rules never read.
 */
internal fun restItinerary(
    id: String = "itinerary",
    segments: List<RestSchema.ItinerarySegment> = emptyList(),
    waypoints: List<RestSchema.ItineraryWaypoint> = emptyList(),
    groups: List<RestSchema.ItinerarySegmentGroup> = emptyList(),
): RestSchema.ItineraryWithDetails =
    RestSchema.ItineraryWithDetails(
        activity_id = "hiking",
        ascent = null,
        count = RestSchema.ItineraryCount(groups = groups.size, segments = segments.size, waypoints = waypoints.size),
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        description = null,
        distance = null,
        id = id,
        is_private = false,
        is_verified = false,
        name = id,
        organization_id = null,
        etag = "etag",
        revision = 1,
        status = RestSchema.ItineraryStatus.Draft,
        updated_at = Instant.fromEpochMilliseconds(0),
        groups = groups,
        has_unpublished_changes = false,
        published_at = null,
        segments = segments,
        waypoints = waypoints,
    )

/** A segment group as the REST API answers one, named and coloured as a test states. */
internal fun restGroup(
    id: String,
    name: String = id,
    color: String = "#7048e8",
): RestSchema.ItinerarySegmentGroup =
    RestSchema.ItinerarySegmentGroup(
        color = color,
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        id = id,
        itinerary_id = "itinerary",
        name = name,
        revision = 1,
        speed = null,
    )

/**
 * A waypoint as the REST API answers one, at the geometry a test states, with the name, colour, icon and
 * details it states and a placeholder for every other member.
 */
internal fun restPlace(
    id: String,
    position: Int = 0,
    geojson: GeoJsonGeometry?,
    name: String? = null,
    color: String? = null,
    icon: RestSchema.WaypointIcon? = null,
    amenities: List<RestSchema.WaypointAmenity> = emptyList(),
    description: String? = null,
    timing: RestSchema.WaypointTiming? = null,
    contact: RestSchema.WaypointContact? = null,
    media: RestSchema.WaypointMedia? = null,
    properties: List<RestSchema.WaypointProperty>? = null,
): RestSchema.ItineraryWaypoint =
    RestSchema.ItineraryWaypoint(
        amenities = amenities,
        color = color,
        contact = contact,
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        description = description,
        geojson = geojson,
        icon = icon,
        id = id,
        itinerary_id = "itinerary",
        media = media,
        mode = RestSchema.WaypointMode.Poi,
        name = name,
        position = position,
        properties = properties,
        revision = 1,
        timing = timing,
    )

/** The Point at the position a test writes. */
internal fun pointAt(vararg position: Double): GeoJsonGeometry = GeoJsonPoint(position.toList())

/** The GeometryCollection of the geometries a test writes, the wrapper a waypoint's point is stored in. */
internal fun collectionOf(vararg geometries: GeoJsonGeometry): GeoJsonGeometry =
    GeoJsonGeometryCollection(geometries.toList())

/** The LineString through the positions a test writes. */
internal fun lineString(vararg positions: List<Double>): GeoJsonGeometry = GeoJsonLineString(positions.toList())
