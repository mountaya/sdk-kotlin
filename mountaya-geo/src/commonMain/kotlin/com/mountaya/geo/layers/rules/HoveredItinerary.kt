package com.mountaya.geo

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/** The property a drawn itinerary's feature names its leg by, the `segmentProperty` `geo/hovered-itinerary.json` declares. */
internal const val SEGMENT_ID_PROPERTY: String = "segment_id"

/** The property a drawn itinerary's feature names its place by, the `waypointProperty` `geo/hovered-itinerary.json` declares. */
internal const val WAYPOINT_ID_PROPERTY: String = "waypoint_id"

/**
 * hoveredItinerary reads what a pointer is over from the [properties] of the features under it, topmost
 * first (`geo.layers.hovered-itinerary`): the first leg and the first place they name. A map's hover tells
 * them, and an itinerary's segment table lights the row of the leg it reads (`map.profile.table-rows`).
 *
 * Each is read from a feature's `segment_id` and `waypoint_id` properties, as a string that is not empty,
 * and never from the feature's own id: the itinerary's vector tiles leave that unset, so a read of it would
 * find nothing under the pointer, ever. A feature naming neither, or naming one by a value that is no id,
 * gives way to the next, and a feature naming both answers both: a place standing on its own leg is one
 * answer to two questions.
 */
public fun hoveredItinerary(properties: List<Map<String, JsonElement>>): HoveredItinerary {
    var segmentId: String? = null
    var waypointId: String? = null
    for (feature in properties) {
        segmentId = segmentId ?: idOn(feature, SEGMENT_ID_PROPERTY)
        waypointId = waypointId ?: idOn(feature, WAYPOINT_ID_PROPERTY)
    }
    return HoveredItinerary(segmentId, waypointId)
}

/** The id a feature's property [name] names, a string that is not empty, or null. */
private fun idOn(
    properties: Map<String, JsonElement>,
    name: String,
): String? = (properties[name] as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotEmpty() }
