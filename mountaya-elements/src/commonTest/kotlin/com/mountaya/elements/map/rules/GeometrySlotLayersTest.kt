package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.MapPaint
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeometrySlotLayersTest {
    private fun list(vararg items: JsonElement): JsonElement = JsonArray(items.toList())

    private fun text(value: String) = JsonPrimitive(value)

    @Test
    fun theHighlightDrawsACasedLineAndAPointAtTheChromeBand() {
        val layers = geometrySlotLayers(GeometrySlot.Highlight, "#f59f00", MapPaint.standard)

        assertEquals(
            listOf(
                "mountaya-highlight-casing",
                "mountaya-highlight-line",
                "mountaya-highlight-point",
            ),
            layers.map {
                it.id
            },
        )
        assertTrue(layers.all { it.source == "mountaya-highlight" && it.rank == 100 })
        val point = layers.last()
        assertEquals(TileSchema.TileLayerType.Circle, point.type)
        assertEquals(
            list(
                text("in"),
                list(text("geometry-type")),
                list(text("literal"), list(text("Point"), text("MultiPoint"))),
            ),
            point.filter,
        )
        assertEquals(
            mapOf(
                "circle-color" to text("#f59f00"),
                "circle-radius" to JsonPrimitive(6.0),
                "circle-stroke-color" to text("#ffffff"),
                "circle-stroke-width" to JsonPrimitive(2.0),
            ),
            point.paint,
        )
        assertEquals(text("#f59f00"), layers[1].paint["line-color"])
    }

    @Test
    fun thePreviewAndTheEmphasisDrawACasedLineJustAboveTheItineraryAndNoPoint() {
        for (slot in listOf(GeometrySlot.Preview, GeometrySlot.Emphasis)) {
            val layers = geometrySlotLayers(slot, slot.color, MapPaint.standard)

            assertEquals(listOf("${slot.sourceId}-casing", "${slot.sourceId}-line"), layers.map { it.id })
            assertTrue(layers.all { it.rank == 52 && it.type == TileSchema.TileLayerType.Line })
            assertEquals(text(slot.color), layers.last().paint["line-color"])
        }
    }

    @Test
    fun theSlotsLinesTakeTheMapsPaintThePointsRingItsCasingColour() {
        val paint =
            MapPaint.standard.copy {
                itinerary.casingColor = "#1b1b1b"
                itinerary.lineWidthStops = listOf(7.0, 3.0, 16.0, 6.0)
            }

        val emphasis = geometrySlotLayers(GeometrySlot.Emphasis, "#7048e8", paint)
        assertEquals(text("#1b1b1b"), emphasis.first().paint["line-color"])
        assertEquals(
            JsonArray(
                listOf(text("interpolate"), list(text("linear")), list(text("zoom"))) +
                    listOf(7.0, 3.0, 16.0, 6.0).map(::JsonPrimitive),
            ),
            emphasis.last().paint["line-width"],
        )
        assertEquals(text("#7048e8"), emphasis.last().paint["line-color"])
        assertEquals(
            text("#1b1b1b"),
            geometrySlotLayers(GeometrySlot.Highlight, "#123456", paint).last().paint["circle-stroke-color"],
        )
    }
}
