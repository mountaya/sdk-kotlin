package com.mountaya.geo

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * itineraryStrokeStyle answers how [stroke] draws under [paint], the shipped paint unless the caller
 * hands its own (`geo.style.itinerary-stroke`), so an itinerary drawn from data, a directions answer or
 * a draft looks as the itineraries the tiles draw.
 *
 * Every stroke draws round, `line-cap` and `line-join` `round`, at a `line-width` interpolated linearly
 * on zoom over a flat table of zoom and width pairs, `["interpolate", ["linear"], ["zoom"], 7, 2, 10, 2,
 * 13, 3, 16, 5]`. The line paints in its colour over the paint's `lineWidthStops`; the dashed stroke
 * paints in its colour over the same table, in the paint's `dashArray` at its `dashedOpacity`; and the
 * casing paints in the paint's `casingColor` at its `casingOpacity` over its `casingWidthStops`. Each
 * table and dash is written as the paint holds it.
 *
 * The answer is the style of a layer of type `line`; the layer's id, source, source layer and filter are
 * the caller's, and a casing is added before the line it lies beneath, since two layers of one rank draw
 * in the order they are added.
 */
public fun itineraryStrokeStyle(
    stroke: ItineraryStroke,
    paint: MapPaint = MapPaint.standard,
): LineLayerStyle {
    val itinerary = paint.itinerary
    return when (stroke) {
        is ItineraryStroke.Line -> {
            LineLayerStyle(
                roundLineLayout,
                linkedMapOf(
                    "line-color" to stroke.color,
                    "line-width" to zoomInterpolatedWidth(itinerary.lineWidthStops),
                ),
            )
        }

        is ItineraryStroke.Dashed -> {
            LineLayerStyle(
                roundLineLayout,
                linkedMapOf(
                    "line-color" to stroke.color,
                    "line-dasharray" to JsonArray(itinerary.dashArray.map(::JsonPrimitive)),
                    "line-opacity" to JsonPrimitive(itinerary.dashedOpacity),
                    "line-width" to zoomInterpolatedWidth(itinerary.lineWidthStops),
                ),
            )
        }

        ItineraryStroke.Casing -> {
            LineLayerStyle(
                roundLineLayout,
                linkedMapOf(
                    "line-color" to JsonPrimitive(itinerary.casingColor),
                    "line-opacity" to JsonPrimitive(itinerary.casingOpacity),
                    "line-width" to zoomInterpolatedWidth(itinerary.casingWidthStops),
                ),
            )
        }
    }
}

/**
 * The layout every stroke of an itinerary shares: round caps and joins, which make a routed line read as
 * one drawn line, where a butt cap notches every vertex and a mitre join throws a spike off every
 * switchback.
 */
private val roundLineLayout: Map<String, JsonElement> =
    linkedMapOf("line-cap" to JsonPrimitive("round"), "line-join" to JsonPrimitive("round"))

/**
 * The `line-width` expression a flat table of zoom and width pairs reads as: each width at its zoom,
 * interpolated linearly between them.
 */
private fun zoomInterpolatedWidth(stops: List<Double>): JsonElement =
    JsonArray(
        listOf(
            JsonPrimitive("interpolate"),
            JsonArray(listOf(JsonPrimitive("linear"))),
            JsonArray(listOf(JsonPrimitive("zoom"))),
        ) + stops.map(::JsonPrimitive),
    )
