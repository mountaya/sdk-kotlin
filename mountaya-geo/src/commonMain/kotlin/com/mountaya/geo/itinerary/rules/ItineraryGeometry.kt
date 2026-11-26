package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.generated.RestSchema

/**
 * itineraryGeometry answers the one geometry an itinerary's profile walks: a GeometryCollection of its
 * [segments]' geometries in chain order, by their `position`, two at one position in the order they are
 * listed, each as its segment stores it, a segment with none left out.
 *
 * The profile lays every line a geometry holds end to end on one distance axis
 * (`buildElevationSeries`), so an itinerary stored as many legs profiles as one curve, each leg's
 * stretch the length of its own lines. It is null where no segment carries a geometry, so a reader
 * draws the empty state a missing itinerary draws, rather than a collection that reads as an itinerary
 * of no length.
 */
public fun itineraryGeometry(segments: List<RestSchema.ItinerarySegment>): GeoJsonGeometry? {
    val geometries = inChainOrder(segments) { it.position }.mapNotNull { it.geojson }
    return if (geometries.isEmpty()) null else GeoJsonGeometryCollection(geometries)
}
