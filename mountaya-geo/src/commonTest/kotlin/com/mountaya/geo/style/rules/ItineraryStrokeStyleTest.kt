package com.mountaya.geo

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ItineraryStrokeStyleTest {
    private fun text(value: String) = JsonPrimitive(value)

    private fun width(vararg stops: Double): JsonElement =
        JsonArray(
            listOf(text("interpolate"), JsonArray(listOf(text("linear"))), JsonArray(listOf(text("zoom")))) +
                stops.map(::JsonPrimitive),
        )

    private val round = mapOf("line-cap" to text("round"), "line-join" to text("round"))
    private val lineWidth = width(7.0, 2.0, 10.0, 2.0, 13.0, 3.0, 16.0, 5.0)
    private val casingWidth = width(7.0, 4.0, 10.0, 4.0, 13.0, 5.0, 16.0, 7.0)

    @Test
    fun aLineStrokesRoundInItsColourAtTheLinesWidths() {
        assertEquals(
            LineLayerStyle(round, mapOf("line-color" to text("#123456"), "line-width" to lineWidth)),
            itineraryStrokeStyle(ItineraryStroke.Line(text("#123456"))),
        )
    }

    @Test
    fun aCasingStrokesRoundInTheCasingsColourAndOpacityWiderAtEveryStop() {
        assertEquals(
            LineLayerStyle(
                round,
                mapOf(
                    "line-color" to text("#ffffff"),
                    "line-opacity" to JsonPrimitive(0.9),
                    "line-width" to casingWidth,
                ),
            ),
            itineraryStrokeStyle(ItineraryStroke.Casing),
        )
    }

    @Test
    fun aProvisionalStrokeIsTheLineDashedAtReducedOpacity() {
        val dashed = itineraryStrokeStyle(ItineraryStroke.Dashed(text("#868e96")))
        assertEquals(
            LineLayerStyle(
                round,
                mapOf(
                    "line-color" to text("#868e96"),
                    "line-dasharray" to JsonArray(listOf(JsonPrimitive(2.0), JsonPrimitive(2.0))),
                    "line-opacity" to JsonPrimitive(0.6),
                    "line-width" to lineWidth,
                ),
            ),
            dashed,
        )
        val solid = itineraryStrokeStyle(ItineraryStroke.Line(text("#868e96")))
        assertEquals(solid.layout, dashed.layout)
        assertEquals(lineWidth, dashed.paint["line-width"])
    }

    @Test
    fun aColourReadFromTheFeatureIsWrittenAsTheExpression() {
        val read = JsonArray(listOf(text("get"), text("mtyaColor")))
        assertEquals(read, itineraryStrokeStyle(ItineraryStroke.Line(read)).paint["line-color"])
    }

    @Test
    fun thePaintHandedIsTheOneStroked() {
        val paint =
            MapPaint.standard.copy {
                itinerary.dashArray = listOf(1.0, 3.0)
                itinerary.lineWidthStops = listOf(7.0, 3.0, 16.0, 6.0)
            }
        assertEquals(
            LineLayerStyle(
                round,
                mapOf(
                    "line-color" to text("#2b8a3e"),
                    "line-dasharray" to JsonArray(listOf(JsonPrimitive(1.0), JsonPrimitive(3.0))),
                    "line-opacity" to JsonPrimitive(0.6),
                    "line-width" to width(7.0, 3.0, 16.0, 6.0),
                ),
            ),
            itineraryStrokeStyle(ItineraryStroke.Dashed(text("#2b8a3e")), paint),
        )

        val casing =
            MapPaint.standard.copy {
                itinerary.casingColor = "#1b1b1b"
                itinerary.casingOpacity = 1.0
            }
        assertEquals(
            LineLayerStyle(
                round,
                mapOf(
                    "line-color" to text("#1b1b1b"),
                    "line-opacity" to JsonPrimitive(1.0),
                    "line-width" to casingWidth,
                ),
            ),
            itineraryStrokeStyle(ItineraryStroke.Casing, casing),
        )
        // A line states no opacity of its own, and reads no member of the casing's.
        assertEquals(
            itineraryStrokeStyle(ItineraryStroke.Line(text("#2b8a3e"))),
            itineraryStrokeStyle(ItineraryStroke.Line(text("#2b8a3e")), casing),
        )
    }

    /**
     * A table and a dash are the paint's as it holds them: one of no stop or of an odd count is written as
     * it stands, and MapLibre refuses the layer it would draw.
     */
    @Test
    fun aTableAndADashAreWrittenAsThePaintHoldsThem() {
        val paint =
            MapPaint.standard.copy {
                itinerary.lineWidthStops = emptyList()
                itinerary.casingWidthStops = listOf(7.0)
                itinerary.dashArray = emptyList()
            }
        assertEquals(width(), itineraryStrokeStyle(ItineraryStroke.Dashed(text("#000000")), paint).paint["line-width"])
        assertEquals(width(7.0), itineraryStrokeStyle(ItineraryStroke.Casing, paint).paint["line-width"])
        assertEquals(
            JsonArray(emptyList()),
            itineraryStrokeStyle(ItineraryStroke.Dashed(text("#000000")), paint).paint["line-dasharray"],
        )
    }
}
