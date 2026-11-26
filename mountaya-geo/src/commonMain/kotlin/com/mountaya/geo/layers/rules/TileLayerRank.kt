package com.mountaya.geo

import com.mountaya.client.generated.TileSchema

/**
 * poiSourceLayers are the source layers of the base map's vector tiles that carry points of
 * interest, `road` among them for its parking aisles. A query of the features under a pointer names
 * the same set the ranks name, so it is published rather than restated.
 */
public val poiSourceLayers: List<String> =
    listOf("parking", "poi_accommodation", "poi_food", "poi_healthcare", "poi_tourism", "road", "street_furniture")

/**
 * tileSourceLayerRanks is the band each source layer Mountaya draws from takes: an itinerary's lines,
 * points and restricted stretches, a zone's fills and the points of interest.
 *
 * It is a map a consumer patches rather than a table fixed here: an app whose own overlay draws above
 * the points of interest and beneath an itinerary's waypoints hands [tileLayerRank]
 * `tileSourceLayerRanks + ("my_overlay" to LayerRank.Poi.rank + 2)`.
 */
public val tileSourceLayerRanks: Map<String, Int> =
    buildMap {
        put("itinerary_lines", LayerRank.ItineraryLine.rank)
        put("itinerary_points", LayerRank.ItineraryPoint.rank)
        put("itinerary_restrictions", LayerRank.ItineraryRestriction.rank)
        put("fills", LayerRank.Zone.rank)
        for (layer in poiSourceLayers) put(layer, LayerRank.Poi.rank)
    }

/**
 * tileLayerRank answers the rank [layer] of a tile specification draws at: a hillshade at
 * [LayerRank.Hillshade] whatever source layer its tiles ride in, since it shades the terrain, and any
 * other the rank [ranks] gives its `source_layer`, [LayerRank.Overlay] for one the table does not
 * know. The source layer is matched exactly, letter case included.
 */
public fun tileLayerRank(
    layer: TileSchema.TileLayer,
    ranks: Map<String, Int> = tileSourceLayerRanks,
): Int =
    if (layer.type == TileSchema.TileLayerType.Hillshade) {
        LayerRank.Hillshade.rank
    } else {
        ranks[layer.source_layer] ?: LayerRank.Overlay.rank
    }
