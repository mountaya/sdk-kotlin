package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.LayerRank
import com.mountaya.geo.MapPaint
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalysisLayersTest {
    private fun list(vararg items: JsonElement): JsonElement = JsonArray(items.toList())

    private fun text(value: String) = JsonPrimitive(value)

    private val stamped = list(text("get"), text("mtyaColor"))

    private fun width(vararg stops: Double): JsonElement =
        JsonArray(listOf(text("interpolate"), list(text("linear")), list(text("zoom"))) + stops.map(::JsonPrimitive))

    @Test
    fun directionsDrawACasedLineInTheirOwnColourInTheItinerarysBand() {
        val layers = analysisLayers(AnalysisKind.Directions, "route", MapPaint.standard)

        assertEquals(listOf("route-casing", "route-line"), layers.map { it.id })
        assertTrue(layers.all { it.source == "route" && it.rank == LayerRank.ItineraryLine.rank })
        assertTrue(layers.all { it.type == TileSchema.TileLayerType.Line })
        assertEquals(text("#1c7ed6"), layers.last().paint["line-color"])
        assertEquals(mapOf("line-cap" to text("round"), "line-join" to text("round")), layers.last().layout)
        assertEquals(text(MapPaint.standard.itinerary.casingColor), layers.first().paint["line-color"])
    }

    @Test
    fun suggestionsDrawTheAlternativesDashedUnderTheChosenEachInItsStampedColour() {
        val layers = analysisLayers(AnalysisKind.Suggestions, "suggested", MapPaint.standard)

        assertEquals(listOf("suggested-alternative", "suggested-chosen"), layers.map { it.id })
        assertEquals(
            listOf(
                list(text("!"), list(text("get"), text("chosen"))),
                list(text("get"), text("chosen")),
            ),
            layers.map {
                it.filter
            },
        )
        assertTrue(layers.all { it.paint["line-color"] == stamped })
        assertEquals(list(JsonPrimitive(2.0), JsonPrimitive(2.0)), layers.first().paint["line-dasharray"])
        assertNull(layers.last().paint["line-dasharray"])
        assertTrue(layers.all { it.rank == 50 })
    }

    @Test
    fun isochronesDrawAFillInTheBandsColourAndAnOutlineInTheZonesBand() {
        val layers = analysisLayers(AnalysisKind.Isochrones, "reach", MapPaint.standard)

        assertEquals(listOf("reach-fill", "reach-outline"), layers.map { it.id })
        assertEquals(
            listOf(
                TileSchema.TileLayerType.Fill,
                TileSchema.TileLayerType.Line,
            ),
            layers.map {
                it.type
            },
        )
        assertEquals(mapOf("fill-color" to stamped, "fill-opacity" to JsonPrimitive(0.35)), layers.first().paint)
        assertEquals(mapOf("line-color" to text("#0b7285"), "line-width" to JsonPrimitive(1.0)), layers.last().paint)
        assertTrue(layers.all { it.rank == LayerRank.Zone.rank })
    }

    @Test
    fun aMatrixDrawsItsConnectorsAsWideAsEachIsStampedThenItsLocations() {
        val layers = analysisLayers(AnalysisKind.Matrix, "grid", MapPaint.standard)

        assertEquals(listOf("grid-connector", "grid-location"), layers.map { it.id })
        assertEquals(
            listOf(
                TileSchema.TileLayerType.Line,
                TileSchema.TileLayerType.Circle,
            ),
            layers.map {
                it.type
            },
        )
        val connector = layers.first()
        assertEquals(list(text("get"), text("mtyaWidth")), connector.paint["line-width"])
        assertEquals(JsonPrimitive(0.55), connector.paint["line-opacity"])
        assertEquals(list(text("=="), list(text("get"), text("mtyaKind")), text("matrix-connector")), connector.filter)
        assertEquals(
            list(text("=="), list(text("get"), text("mtyaKind")), text("matrix-location")),
            layers.last().filter,
        )
        assertEquals(text("#7048e8"), layers.last().paint["circle-color"])
        assertTrue(layers.all { it.rank == 40 })
    }

    @Test
    fun placesAndPointsOfInterestDrawARingedPointInThePointsOfInterestsBand() {
        for (kind in listOf(AnalysisKind.Places, AnalysisKind.Pois)) {
            val layers = analysisLayers(kind, "found", MapPaint.standard)

            assertEquals(listOf("found-point"), layers.map { it.id })
            assertEquals(
                mapOf(
                    "circle-color" to stamped,
                    "circle-radius" to JsonPrimitive(6.0),
                    "circle-stroke-color" to text("#ffffff"),
                    "circle-stroke-width" to JsonPrimitive(2.0),
                ),
                layers.first().paint,
            )
            assertEquals(LayerRank.Poi.rank, layers.first().rank)
        }
    }

    @Test
    fun theLinesTakeTheMapsPaintItsDashesItsWidthsAndItsCasing() {
        val paint =
            MapPaint.standard.copy {
                itinerary.dashArray = listOf(1.0, 3.0)
                itinerary.lineWidthStops = listOf(7.0, 3.0, 16.0, 6.0)
                itinerary.casingColor = "#1b1b1b"
                itinerary.casingOpacity = 1.0
            }

        val suggestions = analysisLayers(AnalysisKind.Suggestions, "s", paint)
        assertEquals(list(JsonPrimitive(1.0), JsonPrimitive(3.0)), suggestions.first().paint["line-dasharray"])
        assertEquals(width(7.0, 3.0, 16.0, 6.0), suggestions.last().paint["line-width"])
        val casing = analysisLayers(AnalysisKind.Directions, "d", paint).first()
        assertEquals(text("#1b1b1b"), casing.paint["line-color"])
        assertEquals(JsonPrimitive(1.0), casing.paint["line-opacity"])
    }
}
