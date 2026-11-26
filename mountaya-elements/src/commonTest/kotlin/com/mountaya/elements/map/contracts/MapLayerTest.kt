package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.LayerRank
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.tileSourceLayerRanks
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapLayerTest {
    private fun tileLayer(
        sourceLayer: String,
        type: TileSchema.TileLayerType = TileSchema.TileLayerType.Line,
    ): TileSchema.TileLayer =
        TileSchema.TileLayer(
            id = "itineraries-$sourceLayer",
            key = sourceLayer,
            layout = mapOf("line-cap" to JsonPrimitive("round")),
            paint = mapOf("line-color" to JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("color")))),
            source_layer = sourceLayer,
            type = type,
            zoom_max = 22,
            zoom_min = 4,
        ) {
            filter =
                listOf(
                    JsonPrimitive("=="),
                    JsonArray(listOf(JsonPrimitive("geometry-type"))),
                    JsonPrimitive("LineString"),
                )
        }

    @Test
    fun aTileSpecificationsLayerTakesTheRankItsSourceLayerDeclaresAndItsMembers() {
        val layer = MapLayer(tileLayer("itinerary_lines"), source = "itineraries")

        assertEquals("itineraries-itinerary_lines", layer.id)
        assertEquals(LayerRank.ItineraryLine.rank, layer.rank)
        assertEquals("itineraries", layer.source)
        assertEquals("itinerary_lines", layer.sourceLayer)
        assertEquals(
            mapOf("line-color" to JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("color")))),
            layer.paint,
        )
        assertEquals(mapOf("line-cap" to JsonPrimitive("round")), layer.layout)
        assertEquals(
            JsonArray(
                listOf(
                    JsonPrimitive("=="),
                    JsonArray(listOf(JsonPrimitive("geometry-type"))),
                    JsonPrimitive("LineString"),
                ),
            ),
            layer.filter,
        )
        assertEquals(ZoomBounds(4.0, 22.0), layer.zoom)
        assertEquals("itinerary_lines", layer.key)
    }

    @Test
    fun aRasterOrAHillshadeCarriesNoSourceLayerAndNoFilterAnEmptySourceLayerAndKeyNone() {
        for (type in listOf(
            TileSchema.TileLayerType.Raster,
            TileSchema.TileLayerType.Hillshade,
        )) {
            val layer = MapLayer(tileLayer("imagery", type), source = "s")
            assertNull(layer.sourceLayer)
            assertNull(layer.filter)
        }
        val empty =
            TileSchema.TileLayer(
                "zone-fill",
                key = "",
                layout = null,
                paint = null,
                source_layer = "",
                type = TileSchema.TileLayerType.Fill,
                zoom_max = 22,
                zoom_min = 0,
            )
        val unnamed = MapLayer(empty, source = "zones")
        assertNull(unnamed.sourceLayer)
        assertNull(unnamed.key)
        assertTrue(unnamed.paint.isEmpty() && unnamed.layout.isEmpty())
        val own =
            TileSchema.TileLayer(
                "rain",
                key = "rain",
                layout = null,
                paint = null,
                source_layer = "precipitation",
                type = TileSchema.TileLayerType.Fill,
                zoom_max = 22,
                zoom_min = 0,
            ) { source = "weather-live" }
        assertEquals("weather-live", MapLayer(own, source = "weather").source)
    }

    @Test
    fun aLayerIsHiddenWhereItsLayoutWritesItsVisibilityNoneAndOnlyThere() {
        val layer = MapLayer(id = "a", type = TileSchema.TileLayerType.Line, source = "s", rank = 50)
        assertFalse(layer.isHidden)
        assertTrue(layer.withVisibility("none").isHidden)
        assertFalse(layer.withVisibility("visible").isHidden)
        assertFalse(layer.with(layout = mapOf("visibility" to JsonPrimitive(0))).isHidden)
    }

    @Test
    fun aHillshadeIsTheTerrainsRankAnUnknownSourceLayerAnOverlaysAPatchedTableItsOwn() {
        assertEquals(
            LayerRank.Hillshade.rank,
            MapLayer(tileLayer("itinerary_lines", TileSchema.TileLayerType.Hillshade), source = "s").rank,
        )
        assertEquals(LayerRank.Overlay.rank, MapLayer(tileLayer("snow_depth"), source = "s").rank)
        val ranks = tileSourceLayerRanks + ("snow_depth" to LayerRank.Poi.rank + 2)
        assertEquals(82, MapLayer(tileLayer("snow_depth"), source = "s", ranks = ranks).rank)
    }

    @Test
    fun twoLayersAreEqualWhereTheyStateTheSameANanTheSameAsANan() {
        val layer =
            MapLayer(
                id = "route",
                type = TileSchema.TileLayerType.Line,
                source = "route",
                rank = 50,
                paint = mapOf("line-width" to JsonPrimitive(Double.NaN)),
            )

        assertEquals(layer, layer.with(layer.paint, layer.layout))
        assertEquals(layer.hashCode(), layer.with(layer.paint, layer.layout).hashCode())
        assertNotEquals(layer, layer.with(mapOf("line-width" to JsonPrimitive(4)), layer.layout))
        val filtered =
            MapLayer(
                id = "route",
                type = TileSchema.TileLayerType.Line,
                source = "route",
                rank = 50,
                paint = layer.paint,
                filter = JsonNull,
            )
        assertNotEquals(layer, filtered)
    }

    @Test
    fun anIdIsComparedAsWrittenSoTwoSpellingsOfOneTextAreTwoLayers() {
        val composed = MapLayer(id = "café", type = TileSchema.TileLayerType.Line, source = "s", rank = 50)
        val decomposed =
            MapLayer(id = "café", type = TileSchema.TileLayerType.Line, source = "s", rank = 50)

        assertNotEquals(composed, decomposed)
    }
}
