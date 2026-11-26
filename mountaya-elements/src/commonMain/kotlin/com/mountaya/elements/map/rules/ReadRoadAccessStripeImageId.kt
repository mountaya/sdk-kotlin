package com.mountaya.elements

import com.mountaya.geo.RoadAccessKind

/**
 * readRoadAccessStripeImageId answers the kind of restricted stretch whose stripe pattern the image id [id]
 * names, or null for an id that names none.
 *
 * An id names one where it is `road-access-stripe-` followed by a kind's id, as the stripe layer beside an
 * itinerary's line names it ([roadAccessLayersOf]), the whole id read exactly. An id under the prefix whose
 * tail is no kind is left missing, so no stretch is ever striped in a colour no kind has, and any other id
 * is left to the map's other answers.
 */
internal fun readRoadAccessStripeImageId(id: String): RoadAccessKind? {
    if (!id.startsWith(ROAD_ACCESS_STRIPE_IMAGE_PREFIX)) return null
    val tail = id.removePrefix(ROAD_ACCESS_STRIPE_IMAGE_PREFIX)
    return RoadAccessKind.entries.firstOrNull { it.id == tail }
}
