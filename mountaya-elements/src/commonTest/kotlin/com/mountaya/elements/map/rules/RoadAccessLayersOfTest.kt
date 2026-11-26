package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.ZoomBounds
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoadAccessLayersOfTest {
    private fun layer(
        id: String,
        sourceLayer: String,
        type: TileSchema.TileLayerType = TileSchema.TileLayerType.Line,
        width: JsonElement? = null,
        zoom: IntRange = 0..22,
        source: String? = null,
    ) = TileSchema.TileLayer(
        id,
        key = id,
        layout = null,
        paint = width?.let { mapOf("line-width" to it) },
        source_layer = sourceLayer,
        type = type,
        zoom_max = zoom.last,
        zoom_min = zoom.first,
    ) { if (source != null) this.source = source }

    private fun source(vararg layers: TileSchema.TileLayer) =
        TileSchema.TileSource(
            id = "itinerary",
            layers = layers.toList(),
            tiles = listOf("https://tiles.test/itinerary/{z}/{x}/{y}"),
            type = TileSchema.TileSourceType.Vector,
            zoom_max = 22,
            zoom_min = 0,
        )

    private fun texts(vararg texts: String): List<JsonElement> = texts.map(::JsonPrimitive)

    private fun match(vararg pairs: String): JsonElement =
        JsonArray(listOf(JsonPrimitive("match"), JsonArray(texts("get", "kind"))) + texts(*pairs))

    @Test
    fun onePairStandsBesideTheFirstItineraryLineThatIsNoCasingAtItsRankAndWidth() {
        val layers =
            roadAccessLayersOf(
                source(
                    layer("line-a-casing", "itinerary_lines", width = JsonPrimitive(6)),
                    layer("line-a", "itinerary_lines", width = JsonPrimitive(4), zoom = 6..22),
                    layer("line-a-dashed", "itinerary_lines", width = JsonPrimitive(4)),
                    layer("point-a", "itinerary_points", TileSchema.TileLayerType.Symbol, zoom = 11..22),
                ),
            )

        assertEquals(listOf("line-a__road-access-stripe", "line-a__road-access-icon"), layers.map { it.id })
        assertEquals(listOf(55, 90), layers.map { it.rank })
        assertTrue(layers.all { it.sourceLayer == "itinerary_restrictions" })
        assertTrue(layers.all { it.source == "itinerary" && it.key == null })
        val stripes = layers.first()
        assertEquals(TileSchema.TileLayerType.Line, stripes.type)
        assertEquals(JsonPrimitive(4), stripes.paint["line-width"])
        assertEquals(JsonPrimitive(0.9), stripes.paint["line-opacity"])
        assertEquals(mapOf("line-cap" to JsonPrimitive("round"), "line-join" to JsonPrimitive("round")), stripes.layout)
        assertEquals(ZoomBounds(6.0, 22.0), stripes.zoom)
        val icons = layers.last()
        assertEquals(TileSchema.TileLayerType.Symbol, icons.type)
        assertEquals(ZoomBounds(11.0, 22.0), icons.zoom, "the waypoints' window")
        assertEquals(JsonPrimitive(0.7), icons.layout["icon-size"])
        assertEquals(JsonPrimitive("line-center"), icons.layout["symbol-placement"])
        assertEquals(mapOf("icon-opacity" to JsonPrimitive(1)), icons.paint)
    }

    @Test
    fun eachImageIsMatchedOnTheKindMostSevereFirstAnUnnamedKindDrawnAsRestricted() {
        val layers = roadAccessLayersOf(source(layer("route", "itinerary_lines")))

        assertEquals(
            match(
                "forbidden",
                "road-access-stripe-forbidden",
                "private",
                "road-access-stripe-private",
                "permit",
                "road-access-stripe-permit",
                "service",
                "road-access-stripe-service",
                "local-access",
                "road-access-stripe-local-access",
                "restricted",
                "road-access-stripe-restricted",
                "road-access-stripe-restricted",
            ),
            layers.first().paint["line-pattern"],
        )
        assertEquals(
            match(
                "forbidden",
                "section-icon-road-access-forbidden-e03131",
                "private",
                "section-icon-road-access-private-e8590c",
                "permit",
                "section-icon-road-access-permit-9c36b5",
                "service",
                "section-icon-road-access-service-1971c2",
                "local-access",
                "section-icon-road-access-local-access-0c8599",
                "restricted",
                "section-icon-road-access-restricted-495057",
                "section-icon-road-access-restricted-495057",
            ),
            layers.last().layout["icon-image"],
        )
        assertEquals("section-icon-road-access-permit-9c36b5", roadAccessIconImageId(RoadAccessKind.Permit))
    }

    @Test
    fun aLineStatingNoWidthHasStripes1WideAndTheIconsTakeItsWindowWithNoWaypoints() {
        val layers = roadAccessLayersOf(source(layer("route", "itinerary_lines", zoom = 5..18)))
        assertEquals(JsonPrimitive(1.0), layers.first().paint["line-width"])
        assertEquals(ZoomBounds(5.0, 18.0), layers.last().zoom)
        val expression =
            JsonArray(
                listOf(
                    JsonPrimitive("interpolate"),
                    JsonArray(texts("linear")),
                    JsonArray(texts("zoom")),
                    JsonPrimitive(7),
                    JsonPrimitive(2),
                    JsonPrimitive(16),
                    JsonPrimitive(5),
                ),
            )
        val interpolated = roadAccessLayersOf(source(layer("route", "itinerary_lines", width = expression)))
        assertEquals(expression, interpolated.first().paint["line-width"])
    }

    @Test
    fun aLineNamingItsOwnSourceBindsTheStretchesToIt() {
        val layers = roadAccessLayersOf(source(layer("route", "itinerary_lines", source = "itinerary-live")))
        assertEquals(listOf("itinerary-live", "itinerary-live"), layers.map { it.source })
    }

    @Test
    fun stretchesTheHostHidAreBornHidden() {
        assertTrue(roadAccessLayersOf(source(layer("route", "itinerary_lines")), isShown = false).all { it.isHidden })
        assertNull(roadAccessLayersOf(source(layer("route", "itinerary_lines"))).first().layout["visibility"])
    }

    @Test
    fun aSourceOfNoItineraryLineALabelOrACasingAloneDrawsNoStretch() {
        assertTrue(
            roadAccessLayersOf(
                source(layer("zone-fill", "fills", TileSchema.TileLayerType.Fill), layer("o", "fills")),
            ).isEmpty(),
        )
        assertTrue(
            roadAccessLayersOf(
                source(
                    layer("slope-line", "slope"),
                    layer("route-label", "itinerary_lines", TileSchema.TileLayerType.Symbol),
                    layer("route-casing", "itinerary_lines"),
                ),
            ).isEmpty(),
        )
        assertEquals(
            2,
            roadAccessLayersOf(source(layer("route-casinǵ", "itinerary_lines"))).size,
            "a casing is told by its id as written",
        )
    }
}
