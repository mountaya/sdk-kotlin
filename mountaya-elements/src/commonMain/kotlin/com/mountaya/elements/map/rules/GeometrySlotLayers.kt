package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.ItineraryStroke
import com.mountaya.geo.MapPaint
import com.mountaya.geo.itineraryStrokeStyle
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

/**
 * geometrySlotLayers answers the layers [slot] draws its geometry with, in [color], from its own source, at
 * its band, in the order they stack (`map.geometry.layers`): the itinerary's cased line in [paint], a
 * `-casing` then a `-line` in the slot's colour (`geo.style.itinerary-stroke`), and, for the highlight alone,
 * a `-point` circle drawn only for a point or a multi-point, in its colour, ringed in the paint's casing
 * colour, since a picked place is a point.
 */
internal fun geometrySlotLayers(
    slot: GeometrySlot,
    color: String,
    paint: MapPaint,
): List<MapLayer> {
    val source = slot.sourceId

    fun stroke(
        stroke: ItineraryStroke,
        role: String,
    ): MapLayer {
        val style = itineraryStrokeStyle(stroke, paint)
        return MapLayer(
            id = source + role,
            type = TileSchema.TileLayerType.Line,
            source = source,
            rank = slot.rank,
            paint = style.paint,
            layout = style.layout,
        )
    }
    val layers =
        mutableListOf(
            stroke(ItineraryStroke.Casing, "-casing"),
            stroke(ItineraryStroke.Line(JsonPrimitive(color)), "-line"),
        )
    if (slot == GeometrySlot.Highlight) {
        layers +=
            MapLayer(
                id = "$source-point",
                type = TileSchema.TileLayerType.Circle,
                source = source,
                rank = slot.rank,
                paint =
                    linkedMapOf(
                        "circle-color" to JsonPrimitive(color),
                        "circle-radius" to JsonPrimitive(RESULT_POINT_RADIUS),
                        "circle-stroke-color" to JsonPrimitive(paint.itinerary.casingColor),
                        "circle-stroke-width" to JsonPrimitive(RESULT_POINT_STROKE_WIDTH),
                    ),
                filter =
                    expression(
                        "in",
                        expression("geometry-type"),
                        expression("literal", JsonArray(listOf(JsonPrimitive("Point"), JsonPrimitive("MultiPoint")))),
                    ),
            )
    }
    return layers
}
