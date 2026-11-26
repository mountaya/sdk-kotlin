package com.mountaya.elements

import com.mountaya.geo.hoveredItinerary

/**
 * hoveredLeg is the leg of an itinerary a hovering pointer is over, as [hover] tells it, read off its features, topmost
 * first, as `geo.layers.hovered-itinerary` reads them, or null where it is over none: what lights a segment table's row
 * from the map (`map.profile.table-rows`).
 */
internal fun hoveredLeg(hover: MapEvent.FeatureHover): String? =
    hoveredItinerary(hover.features.map { it.properties }).segmentId
