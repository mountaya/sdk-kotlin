package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.LayerRank
import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.WAYPOINT_ICON_PREFIX
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.roadAccessColor
import com.mountaya.geo.roadAccessGlyph
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/** The source layer an itinerary's tiles carry its lines in. */
internal const val ITINERARY_LINE_SOURCE_LAYER: String = "itinerary_lines"

/** The source layer an itinerary's tiles carry its waypoints in. */
internal const val ITINERARY_POINT_SOURCE_LAYER: String = "itinerary_points"

/**
 * The source layer an itinerary's tiles carry its restricted stretches in, one line feature per stretch
 * carrying its `kind`, which no specification declares a layer for, so a map draws it with layers of its
 * own.
 */
internal const val ITINERARY_RESTRICTION_SOURCE_LAYER: String = "itinerary_restrictions"

/**
 * The ending of the id of the white halo drawn beneath an itinerary's line, which shares the line's source
 * layer, so its id alone tells the two apart.
 */
internal const val ITINERARY_CASING_SUFFIX: String = "-casing"

/**
 * The opening of the id of the image a kind's stripe pattern is drawn as: `road-access-stripe-` then the
 * kind, which the stripe layer names and the renderer draws.
 */
internal const val ROAD_ACCESS_STRIPE_IMAGE_PREFIX: String = "road-access-stripe-"

/**
 * The opacity the stripes paint at: below full, so the route under them shows through a little and a
 * stretch reads as a mark on the route rather than a second line.
 */
internal const val ROAD_ACCESS_STRIPE_OPACITY: Double = 0.9

/**
 * The scale of a kind's 48-pixel warning sprite to the size a waypoint's marker is drawn at, so a warning on
 * the route reads as clearly as the stops along it.
 */
internal const val ROAD_ACCESS_ICON_SIZE: Double = 0.7

/**
 * The width a renderer draws a line that states none at, in pixels: the width of the stripes over such a
 * line, which then cover the route they mark.
 */
internal const val UNSTATED_LINE_WIDTH: Double = 1.0

/** The ending of the id of the layer a source's restricted stretches are striped on, after the line's. */
internal const val ROAD_ACCESS_STRIPE_LAYER_SUFFIX: String = "__road-access-stripe"

/** The ending of the id of the layer a source's restricted-stretch warnings are drawn on, after the line's. */
internal const val ROAD_ACCESS_ICON_LAYER_SUFFIX: String = "__road-access-icon"

/**
 * roadAccessLayersOf answers the two layers an itinerary [source]'s restricted stretches are drawn on, each
 * at its rank, or none for a source that draws no itinerary line (`map.road-access.layers`).
 *
 * The pair stands beside the source's first `itinerary_lines` layer that is a line and no casing (an id
 * ending in `-casing`), binds that line's source, its own where it names one, and reads the
 * `itinerary_restrictions` source layer; its ids are the line's followed by `__road-access-stripe` and
 * `__road-access-icon`. The stripes are a line at the line's width, 1 where it states none, and in its zoom
 * window, with round caps and joins, painting each stretch's kind's stripe pattern (`road-access-stripe-`
 * then the kind) at 0.9, at `LayerRank.ItineraryRestriction`. The icons stamp each kind's warning sprite, its
 * glyph on a disc of its colour as `map.icons.missing-image` names it, at a size of 0.7, upright and giving
 * way to one another, at the middle of each stretch, in the zoom window of the source's first
 * `itinerary_points` layer or the line's where it has none, at `LayerRank.ItineraryPoint`. Each image is
 * matched on the feature's `kind` over `RoadAccessKind.entries`, most severe first, and a kind the table does
 * not name draws as `restricted`. Where [isShown] is false, the host hid the stretches, and both are born
 * hidden.
 */
internal fun roadAccessLayersOf(
    source: TileSchema.TileSource,
    isShown: Boolean = true,
): List<MapLayer> {
    val line =
        source.layers.firstOrNull { layer ->
            layer.source_layer == ITINERARY_LINE_SOURCE_LAYER &&
                layer.type == TileSchema.TileLayerType.Line &&
                !layer.id.endsWith(ITINERARY_CASING_SUFFIX)
        } ?: return emptyList()
    val sourceId = line.source ?: source.id
    val points = source.layers.firstOrNull { it.source_layer == ITINERARY_POINT_SOURCE_LAYER }
    val hidden = if (isShown) emptyMap() else mapOf("visibility" to JsonPrimitive("none"))
    val stripesLayout =
        linkedMapOf<String, JsonElement>(
            "line-cap" to JsonPrimitive("round"),
            "line-join" to JsonPrimitive("round"),
        )
    val iconsLayout =
        linkedMapOf<String, JsonElement>(
            "icon-allow-overlap" to JsonPrimitive(false),
            "icon-ignore-placement" to JsonPrimitive(false),
            "icon-image" to kindMatch(::roadAccessIconImageId),
            "icon-rotation-alignment" to JsonPrimitive("viewport"),
            "icon-size" to JsonPrimitive(ROAD_ACCESS_ICON_SIZE),
            "symbol-placement" to JsonPrimitive("line-center"),
        )
    return listOf(
        MapLayer(
            id = line.id + ROAD_ACCESS_STRIPE_LAYER_SUFFIX,
            type = TileSchema.TileLayerType.Line,
            source = sourceId,
            sourceLayer = ITINERARY_RESTRICTION_SOURCE_LAYER,
            rank = LayerRank.ItineraryRestriction.rank,
            paint =
                linkedMapOf(
                    "line-opacity" to JsonPrimitive(ROAD_ACCESS_STRIPE_OPACITY),
                    "line-pattern" to kindMatch { ROAD_ACCESS_STRIPE_IMAGE_PREFIX + it.id },
                    "line-width" to (line.paint?.get("line-width") ?: JsonPrimitive(UNSTATED_LINE_WIDTH)),
                ),
            layout = stripesLayout + hidden,
            zoom = ZoomBounds(line.zoom_min.toDouble(), line.zoom_max.toDouble()),
        ),
        MapLayer(
            id = line.id + ROAD_ACCESS_ICON_LAYER_SUFFIX,
            type = TileSchema.TileLayerType.Symbol,
            source = sourceId,
            sourceLayer = ITINERARY_RESTRICTION_SOURCE_LAYER,
            rank = LayerRank.ItineraryPoint.rank,
            paint = mapOf("icon-opacity" to JsonPrimitive(1)),
            layout = iconsLayout + hidden,
            zoom = ZoomBounds((points ?: line).zoom_min.toDouble(), (points ?: line).zoom_max.toDouble()),
        ),
    )
}

/**
 * roadAccessIconImageId answers the id of the image a restricted stretch of [kind] is marked with: the
 * marker sprite of its glyph on a disc of its colour, `section-icon-road-access-permit-9c36b5`, which the map
 * draws as it draws a waypoint's (`map.icons.missing-image`).
 */
internal fun roadAccessIconImageId(kind: RoadAccessKind): String {
    val digits =
        roadAccessColor(kind)
            .hex
            .removePrefix("#")
            .map {
                if (it in
                    'A'..'Z'
                ) {
                    it + ('a' - 'A')
                } else {
                    it
                }
            }.joinToString("")
    return WAYPOINT_ICON_PREFIX + roadAccessGlyph(kind) + "-" + digits
}

/**
 * A `match` on a feature's `kind` answering [answer] for each kind, most severe first, and `restricted`'s for
 * a kind the table does not name.
 */
private fun kindMatch(answer: (RoadAccessKind) -> String): JsonElement {
    val match =
        mutableListOf<JsonElement>(
            JsonPrimitive("match"),
            JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("kind"))),
        )
    for (kind in RoadAccessKind.entries) {
        match += JsonPrimitive(kind.id)
        match += JsonPrimitive(answer(kind))
    }
    match += JsonPrimitive(answer(RoadAccessKind.Restricted))
    return JsonArray(match)
}
