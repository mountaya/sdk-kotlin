package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.FEATURE_COLOR_PROPERTY
import com.mountaya.geo.FEATURE_KIND_PROPERTY
import com.mountaya.geo.FEATURE_WIDTH_PROPERTY
import com.mountaya.geo.ItineraryStroke
import com.mountaya.geo.LayerRank
import com.mountaya.geo.MapPaint
import com.mountaya.geo.itineraryStrokeStyle
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/** The colour a directions result's line is drawn in. */
internal const val DIRECTIONS_LINE_COLOR: String = "#1c7ed6"

/** The opacity an isochrone band's fill paints at. */
internal const val ISOCHRONE_FILL_OPACITY: Double = 0.35

/**
 * The colour of the outline of an isochrone band, which tells two nested bands apart where their
 * translucent fills would not.
 */
internal const val ISOCHRONE_OUTLINE_COLOR: String = "#0b7285"

/** The width of an isochrone band's outline, in pixels. */
internal const val ISOCHRONE_OUTLINE_WIDTH: Double = 1.0

/** The colour a matrix draws its connectors and its locations in. */
internal const val MATRIX_LINE_COLOR: String = "#7048e8"

/** The opacity a matrix's connectors paint at. */
internal const val MATRIX_LINE_OPACITY: Double = 0.55

/** The radius of the circle a place, a point of interest or a matrix location is drawn as, in pixels. */
internal const val RESULT_POINT_RADIUS: Double = 6.0

/** The colour of the ring around a result's circle. */
internal const val RESULT_POINT_STROKE_COLOR: String = "#ffffff"

/** The width of the ring around a result's circle, in pixels. */
internal const val RESULT_POINT_STROKE_WIDTH: Double = 2.0

/**
 * analysisLayers answers the layers a Data API result of [kind] is drawn through, from the GeoJSON source of
 * id [sourceId], each at the kind's rank and named after the source and its role, in the order they stack
 * (`map.analysis.layers`).
 *
 * Directions draw a `-casing` then a `-line` in `#1c7ed6`, and suggestions an `-alternative` dashed for every
 * candidate not chosen then a `-chosen`, each in the colour its feature is stamped with, all four stroked as
 * an itinerary is in [paint] (`geo.style.itinerary-stroke`), at `LayerRank.ItineraryLine`; isochrones a
 * `-fill` in the band's stamped colour and an `-outline`, a matrix a `-connector` at its stamped width and a
 * `-location` circle, at `LayerRank.Zone`; places and points of interest a `-point` circle in their stamped
 * colour, at `LayerRank.Poi`. A stamped colour or width is read from the feature's own property, `mtyaColor`
 * or `mtyaWidth` (`geo.layers.data-geojson`), so a redrawn result needs no new layer.
 */
internal fun analysisLayers(
    kind: AnalysisKind,
    sourceId: String,
    paint: MapPaint,
): List<MapLayer> {
    val stamped = expression("get", JsonPrimitive(FEATURE_COLOR_PROPERTY))

    fun stroke(
        stroke: ItineraryStroke,
        role: String,
        filter: JsonElement? = null,
    ): MapLayer {
        val style = itineraryStrokeStyle(stroke, paint)
        return MapLayer(
            id = sourceId + role,
            type = TileSchema.TileLayerType.Line,
            source = sourceId,
            rank = LayerRank.ItineraryLine.rank,
            paint = style.paint,
            layout = style.layout,
            filter = filter,
        )
    }

    fun point(
        role: String,
        color: JsonElement,
        rank: Int,
        filter: JsonElement? = null,
    ) = MapLayer(
        id = sourceId + role,
        type = TileSchema.TileLayerType.Circle,
        source = sourceId,
        rank = rank,
        paint =
            linkedMapOf(
                "circle-color" to color,
                "circle-radius" to JsonPrimitive(RESULT_POINT_RADIUS),
                "circle-stroke-color" to JsonPrimitive(RESULT_POINT_STROKE_COLOR),
                "circle-stroke-width" to JsonPrimitive(RESULT_POINT_STROKE_WIDTH),
            ),
        filter = filter,
    )

    fun kindIs(feature: String) =
        expression("==", expression("get", JsonPrimitive(FEATURE_KIND_PROPERTY)), JsonPrimitive(feature))

    return when (kind) {
        AnalysisKind.Directions -> {
            listOf(
                stroke(ItineraryStroke.Casing, "-casing"),
                stroke(ItineraryStroke.Line(JsonPrimitive(DIRECTIONS_LINE_COLOR)), "-line"),
            )
        }

        AnalysisKind.Suggestions -> {
            listOf(
                stroke(
                    ItineraryStroke.Dashed(stamped),
                    "-alternative",
                    expression("!", expression("get", JsonPrimitive("chosen"))),
                ),
                stroke(ItineraryStroke.Line(stamped), "-chosen", expression("get", JsonPrimitive("chosen"))),
            )
        }

        AnalysisKind.Isochrones -> {
            listOf(
                MapLayer(
                    id = "$sourceId-fill",
                    type = TileSchema.TileLayerType.Fill,
                    source = sourceId,
                    rank = LayerRank.Zone.rank,
                    paint =
                        linkedMapOf(
                            "fill-color" to stamped,
                            "fill-opacity" to JsonPrimitive(ISOCHRONE_FILL_OPACITY),
                        ),
                ),
                MapLayer(
                    id = "$sourceId-outline",
                    type = TileSchema.TileLayerType.Line,
                    source = sourceId,
                    rank = LayerRank.Zone.rank,
                    paint =
                        linkedMapOf(
                            "line-color" to JsonPrimitive(ISOCHRONE_OUTLINE_COLOR),
                            "line-width" to JsonPrimitive(ISOCHRONE_OUTLINE_WIDTH),
                        ),
                ),
            )
        }

        AnalysisKind.Matrix -> {
            listOf(
                MapLayer(
                    id = "$sourceId-connector",
                    type = TileSchema.TileLayerType.Line,
                    source = sourceId,
                    rank = LayerRank.Zone.rank,
                    paint =
                        linkedMapOf(
                            "line-color" to JsonPrimitive(MATRIX_LINE_COLOR),
                            "line-opacity" to JsonPrimitive(MATRIX_LINE_OPACITY),
                            "line-width" to expression("get", JsonPrimitive(FEATURE_WIDTH_PROPERTY)),
                        ),
                    layout = linkedMapOf("line-cap" to JsonPrimitive("round"), "line-join" to JsonPrimitive("round")),
                    filter = kindIs("matrix-connector"),
                ),
                point("-location", JsonPrimitive(MATRIX_LINE_COLOR), LayerRank.Zone.rank, kindIs("matrix-location")),
            )
        }

        AnalysisKind.Places, AnalysisKind.Pois -> {
            listOf(point("-point", stamped, LayerRank.Poi.rank))
        }
    }
}

/** A style expression: [operator] applied to [arguments]. */
internal fun expression(
    operator: String,
    vararg arguments: JsonElement,
): JsonElement = JsonArray(listOf(JsonPrimitive(operator)) + arguments)
