package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * roadAccessFeatures cuts a segment's restricted stretches out of its [geometry]
 * (`geo.data.road-access-stretches`): one LineString feature per piece, whose property `kind` is its
 * stretch's `RoadAccessKind` as the contract spells it, `local-access`, the property the itinerary
 * tiles' restricted stretches carry, so a line drawn live and a saved one are striped by the same
 * layers.
 *
 * [info] is the segment's `road_access_info`, as REST answers it, and null or no stretch cuts nothing.
 * The lines [geometry] holds, a LineString, each line of a MultiLineString and the lines a
 * GeometryCollection holds, in order, are laid end to end, a geometry that is no line counting for
 * nothing, and a stretch's two indexes count their positions, both included. A stretch is cut into one
 * piece per line it touches and never across the gap between two lines, each piece clamped to its line,
 * so an index before the first position or past the last reads the geometry's end. A piece of fewer than
 * two positions, which covers no length, is left out, and so is a stretch whose values name no kind
 * ([roadAccessKindOf]), a value the table does not name reading as `restricted`.
 *
 * The pieces are in the order [info] lists the stretches, a stretch's in the order of its lines, each
 * position as the geometry writes it.
 */
public fun roadAccessFeatures(
    geometry: GeoJsonGeometry?,
    info: List<RestSchema.RoadAccessInfo>?,
): List<GeoJsonFeature> {
    if (info.isNullOrEmpty()) return emptyList()
    val lines = linesOf(geometry)
    val features = mutableListOf<GeoJsonFeature>()
    for (stretch in info) {
        val kind = roadAccessKindOf(stretch.values) ?: continue
        // The index of each line's first position, the lines laid end to end.
        var offset = 0
        for (line in lines) {
            val last = offset + line.size - 1
            val from = maxOf(stretch.from_index, offset)
            val to = minOf(stretch.to_index, last)
            if (to > from) {
                features +=
                    GeoJsonFeature(
                        GeoJsonLineString(line.subList(from - offset, to - offset + 1).toList()),
                        JsonObject(mapOf("kind" to JsonPrimitive(kind.id))),
                    )
            }
            offset = last + 1
        }
    }
    return features
}
