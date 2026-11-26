package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema

/**
 * itineraryWaypoints answers an itinerary's [waypoints] as an element draws them, its places, in chain
 * order, by their `position`, two at one position in the order they are listed
 * (`geo.data.itinerary-waypoints`).
 *
 * Each stands at the first point its geometry holds, a Point at any depth of the collections it nests
 * in whose longitude and latitude name a place, read as `[longitude, latitude]` with its altitude left
 * out, and a waypoint whose geometry holds no such point, or that has none, is left out, since there is
 * nowhere to draw it. Its name, its colour and its icon, the glyph it is pinned with, are carried as the
 * wire states them.
 *
 * Its details are carried only where a member holds something: the services it lists; its note; its
 * cut-off and its planned stop; each member of its contact the wire states; its photos, each with its
 * caption where it states one, and its links; and its fields in the order they are shown, each with its
 * format where it states one. A list left empty, a block left with nothing and details left with
 * nothing read as none.
 */
public fun itineraryWaypoints(waypoints: List<RestSchema.ItineraryWaypoint>): List<ItineraryPlace> =
    inChainOrder(waypoints) { it.position }.mapNotNull { waypoint ->
        val lngLat = firstPoint(waypoint.geojson) ?: return@mapNotNull null
        ItineraryPlace(
            id = waypoint.id,
            lngLat = lngLat,
            name = waypoint.name,
            color = waypoint.color,
            glyph = waypoint.icon,
            details = detailsOf(waypoint),
        )
    }

/**
 * The first point [geometry] holds whose longitude and latitude name a place, at any depth of the
 * collections it nests in, as `[longitude, latitude]`, or null where it holds none.
 */
private fun firstPoint(geometry: GeoJsonGeometry?): List<Double>? =
    when (geometry) {
        is GeoJsonPoint -> geometry.coordinates.takeIf(::namesPlace)?.let { listOf(it[0], it[1]) }
        is GeoJsonGeometryCollection -> geometry.geometries.firstNotNullOfOrNull(::firstPoint)
        else -> null
    }

/** What [waypoint] says about its place, or null where it says nothing. */
private fun detailsOf(waypoint: RestSchema.ItineraryWaypoint): WaypointDetails? {
    val details =
        WaypointDetails(
            amenities = waypoint.amenities.ifEmpty { null },
            description = waypoint.description,
            timing = timingOf(waypoint.timing),
            contact = contactOf(waypoint.contact),
            media = mediaOf(waypoint.media),
            properties = propertiesOf(waypoint.properties),
        )
    val saysNothing =
        details.amenities == null &&
            details.description == null &&
            details.timing == null &&
            details.contact == null &&
            details.media == null &&
            details.properties == null
    return if (saysNothing) null else details
}

/** A waypoint's [timing], or null where it holds neither a cut-off nor a stop. */
private fun timingOf(timing: RestSchema.WaypointTiming?): WaypointDetails.Timing? {
    if (timing == null || (timing.cutoff_at == null && timing.stop_duration == null)) return null
    return WaypointDetails.Timing(timing.cutoff_at, timing.stop_duration)
}

/** A waypoint's [contact], each member the wire states, or null where it states none. */
private fun contactOf(contact: RestSchema.WaypointContact?): WaypointDetails.Contact? {
    if (contact == null) return null
    val statesNothing =
        contact.address == null &&
            contact.booking_url == null &&
            contact.email == null &&
            contact.opening_hours == null &&
            contact.phone == null &&
            contact.website == null
    if (statesNothing) return null
    return WaypointDetails.Contact(
        address = contact.address,
        bookingUrl = contact.booking_url?.toString(),
        email = contact.email,
        openingHours = contact.opening_hours,
        phone = contact.phone,
        website = contact.website?.toString(),
    )
}

/** A waypoint's photos and links, each list null where it holds none, or null where both are. */
private fun mediaOf(media: RestSchema.WaypointMedia?): WaypointDetails.Media? {
    if (media == null) return null
    val photos = media.photos.orEmpty().map { WaypointDetails.Photo(it.url.toString(), it.caption) }
    val links = media.links.orEmpty().map { WaypointDetails.Link(it.label, it.url.toString()) }
    if (photos.isEmpty() && links.isEmpty()) return null
    return WaypointDetails.Media(photos.ifEmpty { null }, links.ifEmpty { null })
}

/** A waypoint's fields in the order they are shown, or null where it has none. */
private fun propertiesOf(properties: List<RestSchema.WaypointProperty>?): List<WaypointDetails.Property>? =
    properties.orEmpty().map { WaypointDetails.Property(it.key, it.label, it.value, it.format) }.ifEmpty { null }
