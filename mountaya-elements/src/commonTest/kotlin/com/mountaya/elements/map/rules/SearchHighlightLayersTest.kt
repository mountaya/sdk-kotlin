package com.mountaya.elements

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayerRank
import com.mountaya.geo.SearchHighlight
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchHighlightLayersTest {
    private val accent = HexColor("#1c7ed6")

    private fun ids(layers: List<MapLayer>): List<String> = layers.map { it.id }

    @Test
    fun aPlaceIsThePinCarryingAPointInTheAccentOrTheMapPinWhereNoneIsHeld() {
        val (features, layers) =
            searchHighlightLayers(
                SearchHighlight.Place(listOf(6.86, 45.83)),
                accent,
                hasPointGlyph = true,
            )

        assertEquals(listOf("mountaya-search-highlight-place"), ids(layers))
        val pin = layers[0]
        assertEquals(TileSchema.TileLayerType.Symbol, pin.type)
        assertEquals(LayerRank.Chrome.rank, pin.rank)
        assertEquals(SEARCH_HIGHLIGHT_SOURCE_ID, pin.source)
        assertTrue(pin.paint.isEmpty())
        assertEquals(
            mapOf(
                "icon-allow-overlap" to JsonPrimitive(true),
                "icon-ignore-placement" to JsonPrimitive(true),
                "icon-image" to JsonPrimitive("poi-icon-point-1c7ed6"),
                "icon-size" to JsonPrimitive(0.7),
            ),
            pin.layout,
        )
        assertEquals(listOf(GeoJsonPoint(listOf(6.86, 45.83))), features.features.map { it.geometry })
        assertEquals(listOf(JsonObject(emptyMap())), features.features.map { it.properties })
        val (_, fallback) =
            searchHighlightLayers(
                SearchHighlight.Place(listOf(6.86, 45.83)),
                HexColor("#E8590C"),
                hasPointGlyph = false,
            )
        assertEquals(JsonPrimitive("poi-icon-map-pin-e8590c"), fallback[0].layout["icon-image"])
    }

    @Test
    fun aPointOfInterestIsItsCategorysOwnPinInItsColourElseAPlacesPin() {
        val (features, layers) =
            searchHighlightLayers(
                SearchHighlight.Poi(listOf(6.8652, 45.8326), DataSchema.POICategory.Peak),
                accent,
                hasPointGlyph = true,
            )

        assertEquals(listOf("mountaya-search-highlight-poi"), ids(layers))
        assertEquals(JsonPrimitive("poi-icon-mountain-5d4037"), layers[0].layout["icon-image"])
        assertEquals(JsonObject(mapOf("category" to JsonPrimitive("PEAK"))), features.features.first().properties)
        for (category in listOf(null, DataSchema.POICategory("GLACIER"))) {
            val (unstyledFeatures, unstyled) =
                searchHighlightLayers(
                    SearchHighlight.Poi(listOf(6.8652, 45.8326), category),
                    accent,
                    hasPointGlyph = true,
                )
            assertEquals(listOf("mountaya-search-highlight-place"), ids(unstyled))
            assertEquals(JsonPrimitive("poi-icon-point-1c7ed6"), unstyled[0].layout["icon-image"])
            assertEquals(JsonObject(emptyMap()), unstyledFeatures.features.first().properties)
        }
    }

    @Test
    fun aWayWithALineIsThatLineHaloedAndCasedWithADotAtEachOfItsTwoEnds() {
        val coordinates = listOf(listOf(6.8, 45.8), listOf(6.85, 45.82), listOf(6.9, 45.85))
        val (features, layers) =
            searchHighlightLayers(
                SearchHighlight.Way(listOf(6.85, 45.82), listOf(6.8, 45.8, 6.9, 45.85), coordinates),
                HexColor("#e8590c"),
                hasPointGlyph = true,
            )

        assertEquals(
            listOf(
                "mountaya-search-highlight-halo",
                "mountaya-search-highlight-casing",
                "mountaya-search-highlight-line",
                "mountaya-search-highlight-endpoints",
            ),
            ids(layers),
        )
        assertTrue(layers.all { it.rank == 100 })
        val rounded = mapOf("line-cap" to JsonPrimitive("round"), "line-join" to JsonPrimitive("round"))
        assertEquals(
            mapOf(
                "line-blur" to JsonPrimitive(6.0),
                "line-color" to JsonPrimitive("#e8590c"),
                "line-opacity" to JsonPrimitive(0.25),
                "line-width" to JsonPrimitive(16.0),
            ),
            layers[0].paint,
        )
        assertEquals(rounded, layers[0].layout)
        assertEquals(
            mapOf("line-color" to JsonPrimitive("#ffffff"), "line-width" to JsonPrimitive(8.0)),
            layers[1].paint,
        )
        assertEquals(
            mapOf("line-color" to JsonPrimitive("#e8590c"), "line-width" to JsonPrimitive(5.0)),
            layers[2].paint,
        )
        val ends = layers[3]
        assertEquals(TileSchema.TileLayerType.Circle, ends.type)
        assertEquals(
            JsonArray(
                listOf(JsonPrimitive("=="), JsonArray(listOf(JsonPrimitive("geometry-type"))), JsonPrimitive("Point")),
            ),
            ends.filter,
            "the ends read the points alone",
        )
        assertEquals(
            mapOf(
                "circle-color" to JsonPrimitive("#e8590c"),
                "circle-radius" to JsonPrimitive(5.0),
                "circle-stroke-color" to JsonPrimitive("#ffffff"),
                "circle-stroke-width" to JsonPrimitive(2.0),
            ),
            ends.paint,
        )
        assertEquals(
            listOf(GeoJsonLineString(coordinates), GeoJsonPoint(listOf(6.8, 45.8)), GeoJsonPoint(listOf(6.9, 45.85))),
            features.features.map { it.geometry },
        )
    }

    @Test
    fun aWayOfNoLineIsItsExtentWashedAndOutlinedDashedAcrossTheAntimeridian() {
        val (features, layers) =
            searchHighlightLayers(
                SearchHighlight.Way(listOf(6.5, 45.5), listOf(6.0, 45.0, 7.0, 46.0), listOf(listOf(6.5, 45.5))),
                accent,
                hasPointGlyph = true,
            )

        assertEquals(
            listOf("mountaya-search-highlight-bounds-fill", "mountaya-search-highlight-bounds"),
            ids(layers),
            "a line of one position is no line",
        )
        assertEquals(
            mapOf("fill-color" to JsonPrimitive("#1c7ed6"), "fill-opacity" to JsonPrimitive(0.08)),
            layers[0].paint,
        )
        assertEquals(
            mapOf(
                "line-color" to JsonPrimitive("#1c7ed6"),
                "line-dasharray" to JsonArray(listOf(JsonPrimitive(2.0), JsonPrimitive(2.0))),
                "line-width" to JsonPrimitive(2.0),
            ),
            layers[1].paint,
        )
        assertEquals(
            GeoJsonPolygon(
                listOf(
                    listOf(
                        listOf(6.0, 45.0),
                        listOf(7.0, 45.0),
                        listOf(7.0, 46.0),
                        listOf(6.0, 46.0),
                        listOf(6.0, 45.0),
                    ),
                ),
            ),
            features.features.first().geometry,
        )
        val (across, _) =
            searchHighlightLayers(
                SearchHighlight.Way(listOf(179.9, -16.5), listOf(179.5, -17.0, -179.5, -16.0), null),
                accent,
                hasPointGlyph = true,
            )
        assertEquals(
            GeoJsonPolygon(
                listOf(
                    listOf(
                        listOf(179.5, -17.0),
                        listOf(180.5, -17.0),
                        listOf(180.5, -16.0),
                        listOf(179.5, -16.0),
                        listOf(179.5, -17.0),
                    ),
                ),
            ),
            across.features.first().geometry,
        )
    }

    @Test
    fun anExtentOfNoAreaOrOfAnEdgeAtNoNumberGivesWayToAPinAtTheAnchor() {
        for (bounds in listOf(
            listOf(6.0, 45.0, 6.0, 46.0),
            listOf(6.0, 46.0, 7.0, 45.0),
            listOf(Double.NaN, 45.0, 7.0, 46.0),
            listOf(180.0, 45.0, -180.0, 46.0),
            listOf(6.0, 45.0, 7.0),
            listOf(6.0, 45.0, Double.POSITIVE_INFINITY, 46.0),
        )) {
            val (features, layers) =
                searchHighlightLayers(
                    SearchHighlight.Way(listOf(6.5, 45.5), bounds, null),
                    accent,
                    hasPointGlyph = true,
                )
            assertEquals(listOf("mountaya-search-highlight-place"), ids(layers), "$bounds")
            assertEquals(GeoJsonPoint(listOf(6.5, 45.5)), features.features.first().geometry)
        }
        val (_, bare) =
            searchHighlightLayers(
                SearchHighlight.Way(listOf(6.5, 45.5), null, null),
                accent,
                hasPointGlyph = false,
            )
        assertEquals(JsonPrimitive("poi-icon-map-pin-1c7ed6"), bare[0].layout["icon-image"])
    }
}
