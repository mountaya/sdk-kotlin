package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * itineraryBounds is the box a drawn itinerary is framed on (`geo.camera.content-bounds`): the box
 * `boundsOf` draws around every position of every segment's stored geometry, a directions segment's vias
 * included, so a chain straddling the antimeridian is framed across it with its east edge past 180. The
 * itinerary's places, its waypoints, are no segment's and are left out.
 *
 * A segment with no geometry yet adds nothing, whatever its place in the chain, and the answer is null
 * where no segment's geometry names a place: such an itinerary frames nothing.
 */
public fun itineraryBounds(segments: List<RestSchema.ItinerarySegment>): List<Double>? =
    boundsOf(segments.mapNotNull { it.geojson })
