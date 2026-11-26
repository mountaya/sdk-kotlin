package com.mountaya.elements

import com.mountaya.geo.LayerRank

/**
 * queryLayerIds is the layers a point [query] asks the renderer about (`map.query.layers`): every layer the
 * map [placed] in a band the query reads that the live style still holds, [isLive], in the order the map
 * placed them, or null where none is left, which asks the renderer nothing.
 *
 * A map answers for what it drew, never for its base map, whose roads and landuse are no answer to "what is
 * here", so a query names the layers it placed: `Features` every band, `Pois` the points of interest band,
 * `Zones` the zones band and `Itineraries` the itinerary line and point bands, a layer placed at a rank
 * between two bands being in neither. A renderer refuses a layer its style no longer holds, which happens for
 * a beat as a switch of base map replays the map, so such a layer is left out.
 */
internal fun queryLayerIds(
    query: PointQuery,
    placed: List<PlacedLayer>,
    isLive: (String) -> Boolean,
): List<String>? {
    val bands: List<LayerRank>? =
        when (query) {
            PointQuery.Features -> null
            PointQuery.Pois -> listOf(LayerRank.Poi)
            PointQuery.Zones -> listOf(LayerRank.Zone)
            PointQuery.Itineraries -> listOf(LayerRank.ItineraryLine, LayerRank.ItineraryPoint)
        }
    val ids =
        placed
            .filter { layer -> (bands?.any { it.rank == layer.rank } ?: true) && isLive(layer.id) }
            .map { it.id }
    return ids.ifEmpty { null }
}
