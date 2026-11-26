package com.mountaya.elements

import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.Url
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.GeolocateControlEngine
import com.mountaya.geo.GeolocateControlState
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayerRank
import com.mountaya.geo.LayersSelectionEngine
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.ReadoutPoint
import com.mountaya.geo.SearchHighlight
import com.mountaya.geo.WEB_MERCATOR_MAX_LATITUDE
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.labelLanguage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * The hit-tests a frame of a pointer moving over the map may cost: one, at the place it ended the frame on, over
 * every layer a hover is told of at once. A hit-test is a query of the renderer, and a pointer moves many times a
 * frame (`hover-hit-test` in the contract's `sdk/hot-paths.yaml`).
 */
private const val HOVER_HIT_TEST_BUDGET = 1

@OptIn(ExperimentalCoroutinesApi::class)
class MapEngineTest {
    /** Answers the Tile API's `getTileLayers` with two terrains, and records each request. */
    private class Catalogue : HttpExchange {
        val sent = mutableListOf<ExchangeRequest>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += request
            return ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(BODY.encodeToByteArray()),
            )
        }

        companion object {
            const val BODY = """{"data": {
                "elevation": {"id": "55763749-32b4-42d5-9f62-b6c51ef54d99", "tile_size": 512, "url": "https://tiles.mountaya.com/v1/elevation/tiles.json"},
                "itineraries": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                  "content_type": "application/x-protobuf", "id": "i", "locales": {}, "slug": "itineraries", "sources": []},
                "zones": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                  "content_type": "application/x-protobuf", "id": "z", "locales": {}, "slug": "zones", "sources": []},
                "overlays": [],
                "terrains": [
                  {"id": "t1", "slug": "outdoor", "locales": {}, "variants": {
                    "light": {"style": {"url": "https://tiles.mountaya.com/v1/terrains/outdoor/light/style"},
                      "tiles": {"size": 512, "url": "https://tiles.mountaya.com/v1/terrains/outdoor/light/tiles.json"}},
                    "dark": {"style": {"url": "https://tiles.mountaya.com/v1/terrains/outdoor/dark/style"},
                      "tiles": {"size": 512, "url": "https://tiles.mountaya.com/v1/terrains/outdoor/dark/tiles.json"}}}},
                  {"id": "t2", "slug": "winter", "locales": {}, "variants": {
                    "light": {"style": {"url": "https://tiles.mountaya.com/v1/terrains/winter/light/style"},
                      "tiles": {"size": 512, "url": "https://tiles.mountaya.com/v1/terrains/winter/light/tiles.json"}},
                    "dark": {"style": {"url": "https://tiles.mountaya.com/v1/terrains/winter/dark/style"},
                      "tiles": {"size": 512, "url": "https://tiles.mountaya.com/v1/terrains/winter/dark/tiles.json"}}}}
                ]
              }}"""
        }
    }

    /**
     * A client over [exchange]. Every answer here arrives whole, so each client reads and decodes it where
     * the call is, unconfined: a read on a thread of the platform's would leave the test's virtual time
     * free to spend an attempt's budget.
     */
    private fun client(
        language: String? = "fr",
        exchange: HttpExchange = Catalogue(),
    ): Mountaya =
        Mountaya(
            Credential.Publishable("pk_test_a"),
            language = language,
            exchange = exchange,
            dispatcher = Dispatchers.Unconfined,
        )

    private val line =
        MapLayer(
            id = "route",
            type = TileSchema.TileLayerType.Line,
            source = "route",
            rank = LayerRank.ItineraryLine.rank,
            paint = mapOf("line-color" to JsonPrimitive("#e64980"), "line-width" to JsonPrimitive(4)),
        )

    @Test
    fun anEngineOpensOnItsCameraAndItsStyleDrawingNothingAboveItsBaseMap() {
        val camera = CameraPose(listOf(6.86, 45.83), 12.0, 30.0, 15.0)
        val style = Url("https://example.com/style.json")
        val transform: (MapRequest) -> MapRequest = { it }
        val client = client()
        val map = MapEngine(client, style, camera = camera, transformRequest = transform)

        assertEquals(MapState(style, camera, RestSchema.Language.Fr), map.state.value)
        assertSame(client, map.client)
        assertSame(transform, map.transformRequest)
    }

    @Test
    fun theMarkerSpritesDrawFromTheBundledMarkerSetOrFromTheSetTheAppHandsIn() {
        val licensed =
            assertNotNull(
                GlyphSet.of(
                    "licensed",
                    GlyphSet.Paint.Stroke,
                    markup =
                        mapOf("hut" to "<path d=\"M0 0h1\"/>"),
                ),
                "the licensed set is a set",
            )
        val chained = assertNotNull(GlyphSet.chaining(listOf(licensed, GlyphSet.marker)), "the two sets chain")

        assertEquals(GlyphSet.marker, MapEngine(client()).glyphs)
        assertEquals(licensed, MapEngine(client(), glyphs = licensed).glyphs)
        assertEquals("licensed+marker", MapEngine(client(), glyphs = chained).glyphs.name)
    }

    @Test
    fun theLabelsSpeakTheLanguageNamedElseTheClientsElseTheDevices() {
        assertEquals(RestSchema.Language.De, MapEngine(client("de-CH")).state.value.labelLanguage)
        assertEquals(
            RestSchema.Language.Ja,
            MapEngine(client(), labelLanguage = RestSchema.Language.Ja).state.value.labelLanguage,
        )
        assertEquals(
            RestSchema.Language.En,
            MapEngine(client(), labelLanguage = RestSchema.Language("pt")).state.value.labelLanguage,
        )
        assertEquals(labelLanguage(preferredLanguage()), MapEngine(client(null)).state.value.labelLanguage)

        val map = MapEngine(client(), labelLanguage = RestSchema.Language.It)
        map.setLabelLanguage(null)
        assertEquals(RestSchema.Language.Fr, map.state.value.labelLanguage)
        map.setLabelLanguage(RestSchema.Language.Zh)
        assertEquals(RestSchema.Language.Zh, map.state.value.labelLanguage)
    }

    @Test
    fun aTerrainOfTheCatalogueIsTheBaseMapTheFirstUnlessNamedLightUntilDark() =
        runTest {
            val catalogue = Catalogue()
            val map = MapEngine(client(exchange = catalogue))
            map.setTerrain()
            assertEquals(Url("https://tiles.mountaya.com/v1/terrains/outdoor/light/style"), map.state.value.style)
            assertEquals("outdoor", map.state.value.terrain)
            assertEquals(TileSchema.TerrainVariant.Light, map.state.value.terrainVariant)

            map.setTerrainVariant(TileSchema.TerrainVariant.Dark)
            assertEquals(Url("https://tiles.mountaya.com/v1/terrains/outdoor/dark/style"), map.state.value.style)
            assertEquals(TileSchema.TerrainVariant.Dark, map.state.value.terrainVariant)

            map.setTerrain("winter")
            assertEquals(Url("https://tiles.mountaya.com/v1/terrains/winter/dark/style"), map.state.value.style)
            assertEquals("winter", map.state.value.terrain)

            assertEquals(
                MapException(MapException.Kind.TerrainNotFound, "moon"),
                assertFailsWith<MapException> { map.setTerrain("moon") },
            )
            assertEquals(Url("https://tiles.mountaya.com/v1/terrains/winter/dark/style"), map.state.value.style)
            assertTrue(catalogue.sent.all { it.url.toString().startsWith("https://tiles.mountaya.com/") })
            assertEquals(1, catalogue.sent.size, "the catalogue is read once for the scope")

            val own = Url("https://example.com/own.json")
            map.setStyle(own)
            assertNull(map.state.value.terrain, "a style of the app's own is no terrain of the catalogue")
            map.setTerrainVariant(TileSchema.TerrainVariant.Light)
            assertEquals(own, map.state.value.style, "under a style of the app's own nothing is switched")
            assertEquals(TileSchema.TerrainVariant.Light, map.state.value.terrainVariant)
        }

    @Test
    fun aBaseMapThatDoesNotArriveGivesTheMapBackTheOneShownItsContentKept() =
        runTest {
            val map = MapEngine(client())
            val surface = ScriptedMapSurface(map)
            map.setTerrain()
            surface.loadStyle()
            map.addLayer(line)
            map.setTerrain("winter")
            assertEquals("winter", map.state.value.terrain)
            surface.failStyle()
            assertEquals("outdoor", map.state.value.terrain)
            assertEquals(Url("https://tiles.mountaya.com/v1/terrains/outdoor/light/style"), map.state.value.style)
            assertEquals(
                listOf("route"),
                map.state.value.layers
                    .map { it.id },
            )
            surface.failStyle()
            assertEquals("outdoor", map.state.value.terrain, "the base map shown stays where nothing was asked")
        }

    @Test
    fun everyLayerIsRankedByTheOneRegistryWhateverOrderItWasAddedIn() {
        val map = MapEngine(client())
        map.addSource(MapSource("route", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
        map.addLayer(
            MapLayer(
                id = "cursor",
                type = TileSchema.TileLayerType.Circle,
                source = "route",
                rank = LayerRank.Chrome.rank,
            ),
        )
        map.addLayer(line)
        map.addLayer(
            MapLayer(
                id = "shade",
                type = TileSchema.TileLayerType.Hillshade,
                source = "route",
                rank = LayerRank.Hillshade.rank,
            ),
        )

        assertEquals(
            listOf("shade", "route", "cursor"),
            map.state.value.layers
                .map { it.id },
        )
        assertEquals(
            listOf("route"),
            map.state.value.sources
                .map { it.id },
        )
    }

    @Test
    fun aPatchReplacesEachMemberItStatesAndLeavesEachItMapsToNullAsHeld() {
        val map = MapEngine(client())
        map.addLayer(line)
        map.editLayer(
            "route",
            paint =
                mapOf(
                    "line-color" to JsonPrimitive("#228be6"),
                    "line-width" to null,
                    "line-opacity" to JsonPrimitive(0),
                ),
            layout = mapOf("visibility" to JsonPrimitive("none")),
        )

        val layer =
            map.state.value.layers
                .first()
        assertEquals(
            mapOf(
                "line-color" to JsonPrimitive("#228be6"),
                "line-width" to JsonPrimitive(4),
                "line-opacity" to JsonPrimitive(0),
            ),
            layer.paint,
        )
        assertEquals(mapOf("visibility" to JsonPrimitive("none")), layer.layout)

        map.editLayer("route", paint = mapOf("line-color" to JsonNull))
        assertEquals(
            JsonNull,
            map.state.value.layers
                .first()
                .paint["line-color"],
        )
    }

    @Test
    fun aPatchOfALayerNotDrawnIsRefusedAndTheMapIsLeftAsItWas() {
        val map = MapEngine(client())
        map.addLayer(line)
        val before = map.state.value

        assertEquals(
            MapException(MapException.Kind.LayerNotFound, "gone"),
            assertFailsWith<MapException> { map.editLayer("gone", paint = mapOf("line-width" to JsonPrimitive(2))) },
        )
        assertSame(before, map.state.value)
    }

    @Test
    fun aTileSpecificationsSourcesAndLayersAreDrawnEachLayerAtItsSourceLayersRank() {
        fun layer(
            id: String,
            sourceLayer: String,
            type: TileSchema.TileLayerType,
        ) = TileSchema.TileLayer(
            id,
            key = id,
            layout = null,
            paint = null,
            source_layer = sourceLayer,
            type = type,
            zoom_max = 22,
            zoom_min = 0,
        )
        val specification =
            TileSchema.TileSpecification(
                listOf(
                    TileSchema.TileSource(
                        id = "itineraries",
                        layers =
                            listOf(
                                layer(
                                    "itinerary-points",
                                    "itinerary_points",
                                    TileSchema.TileLayerType.Symbol,
                                ),
                                layer("itinerary-lines", "itinerary_lines", TileSchema.TileLayerType.Line),
                            ),
                        tiles = listOf("https://tiles.mountaya.com/v1/itineraries/{z}/{x}/{y}"),
                        type = TileSchema.TileSourceType.Vector,
                        zoom_max = 14,
                        zoom_min = 0,
                    ),
                    TileSchema.TileSource(
                        id = "zones",
                        layers = listOf(layer("zone-fills", "fills", TileSchema.TileLayerType.Fill)),
                        tiles = listOf("https://tiles.mountaya.com/v1/zones/{z}/{x}/{y}"),
                        type = TileSchema.TileSourceType.Vector,
                        zoom_max = 14,
                        zoom_min = 0,
                    ),
                ),
            )
        val map = MapEngine(client())
        map.setSpecification(specification, "content")

        assertEquals(
            listOf("itineraries", "zones"),
            map.state.value.sources
                .map { it.id },
        )
        assertEquals(
            listOf(
                "zone-fills",
                "itinerary-lines",
                "itinerary-lines__road-access-stripe",
                "itinerary-points",
                "itinerary-lines__road-access-icon",
            ),
            map.state.value.layers
                .map { it.id },
        )
        assertEquals(
            listOf(40, 50, 55, 90, 90),
            map.state.value.layers
                .map { it.rank },
        )
        assertEquals(listOf("content"), map.specifications)

        map.removeSource("itineraries")
        assertEquals(
            listOf("zones"),
            map.state.value.sources
                .map { it.id },
        )
        assertEquals(
            listOf("zone-fills"),
            map.state.value.layers
                .map { it.id },
        )

        map.removeSpecification("content")
        assertTrue(
            map.state.value.sources
                .isEmpty(),
        )
        assertTrue(
            map.state.value.layers
                .isEmpty(),
        )
        assertTrue(map.specifications.isEmpty())
    }

    @Test
    fun aCollectorIsToldTheStateNowThenEachOneThatDiffersAndNothingAfterClose() =
        runTest {
            val map = MapEngine(client())
            // An edit begun before the close, so placing the waypoint after it meets the close alone.
            map.beginWaypointEdit("w0", listOf(6.86, 45.83))
            val told = mutableListOf<List<String>>()
            val reading =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    map.state.collect { told += it.layers.map { layer -> layer.id } }
                }
            map.addLayer(line)
            map.addLayer(line)
            map.removeLayer("route")
            map.removeLayer("route")
            map.close()
            val closed = map.state.value
            map.addLayer(line)
            // After a close no command refuses anything either: it does nothing.
            map.editLayer("route", paint = mapOf("line-width" to JsonPrimitive(8)))
            map.editMarker("hut", label = "Refuge")
            map.addMarker(MapMarker("hut", listOf(0.0, 100.0)))
            map.setMaxBounds(listOf(5.0, 45.0, 7.0, 45.0))
            map.setZoomBounds(ZoomLimit.Zoom(13.0), ZoomLimit.Zoom(12.0))
            map.beginWaypointEdit("w1", listOf(0.0, 100.0))
            map.placeWaypoint(listOf(0.0, 100.0), identity)
            // Nor does a command it would take act: the state stays as the close left it.
            map.addMarker(MapMarker("hut", listOf(6.86, 45.83)))
            map.setMaxBounds(listOf(6.0, 45.0, 7.0, 46.0))
            map.setZoomBounds(ZoomLimit.Zoom(5.0), ZoomLimit.Zoom(12.0))
            map.placeWaypoint(listOf(6.9, 45.9), identity)
            map.beginWaypointEdit("w1")
            assertNull(map.endWaypointEdit())
            assertSame(closed, map.state.value)
            assertEquals(
                listOf(6.86, 45.83),
                map.state.value.waypointEdit
                    ?.position,
            )
            reading.cancel()

            assertEquals(listOf(emptyList(), listOf("route"), emptyList()), told)
            assertTrue(
                map.state.value.layers
                    .isEmpty(),
            )
        }

    @Test
    fun aMarkerIsShownAboveTheOthersOneUnderAnIdInUseReplacingItOnTop() {
        val map = MapEngine(client())

        map.addMarker(MapMarker("hut", listOf(6.86, 45.83, 2_100.0), label = "Refuge"))
        map.addMarker(MapMarker("summit", listOf(6.87, 45.83), color = "#e64980"))
        map.addMarker(MapMarker("hut", listOf(6.85, 45.82)))

        val markers = map.state.value.markers
        assertEquals(listOf("summit", "hut"), markers.map { it.id })
        assertEquals(listOf(6.85, 45.82), markers.last().position)
        assertNull(markers.last().label, "a replacement is the marker as added")
        map.addMarker(MapMarker("col", listOf(7.0, 46.0, 1_800.0)))
        assertEquals(
            listOf(7.0, 46.0),
            map.state.value.markers
                .last()
                .position,
            "an altitude is left out",
        )
    }

    @Test
    fun aMarkerPlacedNowhereAMapDrawsIsRefusedByNameTheMapLeftAsItWas() {
        val map = MapEngine(client())
        map.addMarker(MapMarker("hut", listOf(6.86, 45.83)))
        val before = map.state.value

        for (position in listOf(
            listOf(0.0, 100.0),
            listOf(Double.NaN, 45.0),
            listOf(6.0, Double.POSITIVE_INFINITY),
            listOf(6.0),
            emptyList(),
        )) {
            val refusal = assertFailsWith<MapException> { map.addMarker(MapMarker("hut", position)) }
            assertEquals(MapException(MapException.Kind.PositionRefused, "hut"), refusal)
        }
        assertEquals(
            MapException(MapException.Kind.PositionRefused, "hut"),
            assertFailsWith<MapException> { map.editMarker("hut", position = listOf(6.0, -91.0)) },
        )
        assertEquals(before, map.state.value)
        map.addMarker(MapMarker("east", listOf(190.0, 90.0)))
        assertEquals(
            listOf(190.0, 90.0),
            map.state.value.markers
                .last()
                .position,
            "a longitude past the range names a place",
        )
    }

    @Test
    fun anEditOfAMarkerChangesWhatItGivesAndKeepsWhatItLeavesNull() {
        val map = MapEngine(client())
        map.addMarker(MapMarker("hut", listOf(6.86, 45.83), color = "#e64980", label = "A"))
        map.addMarker(MapMarker("summit", listOf(6.87, 45.84)))

        map.editMarker("hut", label = "Refuge")
        map.editMarker("hut", position = listOf(6.9, 45.9))

        assertEquals(
            listOf(
                MapMarker("hut", listOf(6.9, 45.9), color = "#e64980", label = "Refuge"),
                MapMarker("summit", listOf(6.87, 45.84)),
            ),
            map.state.value.markers,
            "an edited marker keeps its place",
        )
        assertEquals(
            MapException(MapException.Kind.MarkerNotFound, "lake"),
            assertFailsWith<MapException> { map.editMarker("lake", label = "Lac") },
        )
        map.removeMarker("lake")
        map.removeMarker("hut")
        assertEquals(
            listOf("summit"),
            map.state.value.markers
                .map { it.id },
        )
        assertEquals(
            MapException(MapException.Kind.MarkerNotFound, "hut"),
            assertFailsWith<MapException> { map.editMarker("hut", label = "Refuge") },
        )
        map.clearMarkers()
        assertTrue(
            map.state.value.markers
                .isEmpty(),
        )
    }

    @Test
    fun aPanLockIsReadAsABoxIsATurnWideIsTheWholeWorldAndNullLiftsIt() {
        val map = MapEngine(client())
        val world = listOf(-179.9999999999, -60.0, 179.9999999999, 60.0)

        map.setMaxBounds(listOf(7.0, 46.0, 6.0, 45.0))
        assertEquals(listOf(6.0, 45.0, 7.0, 46.0), map.state.value.maxBounds, "a box drawn from its east corner")
        map.setMaxBounds(listOf(179.5, -17.0, 180.5, -16.0))
        assertEquals(listOf(179.5, -17.0, 180.5, -16.0), map.state.value.maxBounds, "across the antimeridian")
        map.setMaxBounds(listOf(5.0, 80.0, 6.0, 95.0))
        assertEquals(listOf(5.0, 80.0, 6.0, WEB_MERCATOR_MAX_LATITUDE), map.state.value.maxBounds)
        map.setMaxBounds(listOf(-200.0, -60.0, 200.0, 60.0))
        assertEquals(world, map.state.value.maxBounds)
        map.setMaxBounds(listOf(Double.NEGATIVE_INFINITY, -60.0, 10.0, 60.0))
        assertEquals(world, map.state.value.maxBounds)
        map.setMaxBounds(null)
        assertNull(map.state.value.maxBounds)
    }

    @Test
    fun aPanLockThatEnclosesNoAreaIsRefusedTheMapLeftAsItWas() {
        val map = MapEngine(client())
        map.setMaxBounds(listOf(6.0, 45.0, 7.0, 46.0))
        val before = map.state.value

        for (bounds in listOf(
            listOf(5.0, 45.0, 7.0, 45.0),
            listOf(5.0, 45.0, 5.0, 46.0),
            listOf(Double.NaN, 45.0, 6.0, 46.0),
            listOf(5.0, 91.0, 6.0, 95.0),
            listOf(6.0),
        )) {
            assertEquals(
                MapException(MapException.Kind.MaxBoundsRefused, bounds.toString()),
                assertFailsWith<MapException> { map.setMaxBounds(bounds) },
            )
        }
        assertEquals(before, map.state.value)
    }

    @Test
    fun aZoomRangeHoldsTheCameraBroughtToItsNearestEndAndAReleaseGoesBackTo0To25Point5() {
        val map = MapEngine(client(), camera = CameraPose(listOf(6.86, 45.83), 16.0, 0.0, 0.0))
        assertEquals(ZoomBounds(0.0, 25.5), map.state.value.zoomBounds, "MapLibre Native's own")

        map.setZoomBounds(ZoomLimit.Zoom(5.0), ZoomLimit.Zoom(12.0))
        assertEquals(ZoomBounds(5.0, 12.0), map.state.value.zoomBounds)
        assertEquals(12.0, map.state.value.camera.zoom)
        map.jump(CameraTarget(zoom = 2.0))
        assertEquals(5.0, map.state.value.camera.zoom)
        map.setZoomBounds(ZoomLimit.Zoom(8.0), ZoomLimit.Zoom(8.0))
        assertEquals(8.0, map.state.value.camera.zoom, "a range of one zoom holds the camera at it")
        map.setZoomBounds(max = ZoomLimit.Zoom(14.0))
        assertEquals(ZoomBounds(8.0, 14.0), map.state.value.zoomBounds, "the floor is kept")
        map.setZoomBounds(min = ZoomLimit.Released)
        assertEquals(ZoomBounds(0.0, 14.0), map.state.value.zoomBounds)
        map.setZoomBounds(ZoomLimit.Zoom(16.5), ZoomLimit.Released)
        assertEquals(ZoomBounds(16.5, 25.5), map.state.value.zoomBounds)
        assertEquals(16.5, map.state.value.camera.zoom)
        map.setZoomBounds(ZoomLimit.Released, ZoomLimit.Released)
        assertEquals(ZoomBounds(0.0, 25.5), map.state.value.zoomBounds)
        assertEquals(16.5, map.state.value.camera.zoom)
    }

    @Test
    fun aZoomRangeACameraCannotTakeIsRefusedByKindTheMapLeftAsItWas() {
        val map = MapEngine(client())
        map.setZoomBounds(ZoomLimit.Zoom(3.0), ZoomLimit.Zoom(15.0))
        val before = map.state.value

        val refusals =
            listOf(
                Triple(ZoomLimit.Zoom(Double.NaN), ZoomLimit.Kept, MapException.Kind.NumberRefused),
                Triple(ZoomLimit.Kept, ZoomLimit.Zoom(Double.POSITIVE_INFINITY), MapException.Kind.NumberRefused),
                Triple(ZoomLimit.Zoom(Double.NEGATIVE_INFINITY), ZoomLimit.Zoom(12.0), MapException.Kind.NumberRefused),
                Triple(ZoomLimit.Zoom(-1.0), ZoomLimit.Kept, MapException.Kind.ZoomBoundsRefused),
                Triple(ZoomLimit.Zoom(13.0), ZoomLimit.Zoom(12.0), MapException.Kind.ZoomBoundsRefused),
                Triple(ZoomLimit.Zoom(16.0), ZoomLimit.Kept, MapException.Kind.ZoomBoundsRefused),
                Triple(ZoomLimit.Kept, ZoomLimit.Zoom(2.0), MapException.Kind.ZoomBoundsRefused),
            )
        for ((min, max, kind) in refusals) {
            assertEquals(kind, assertFailsWith<MapException> { map.setZoomBounds(min, max) }.kind, "$min to $max")
        }
        assertEquals(before, map.state.value)
        map.setZoomBounds(ZoomLimit.Zoom(0.0), ZoomLimit.Zoom(0.0))
        assertEquals(ZoomBounds(0.0, 0.0), map.state.value.zoomBounds, "the renderer's floor is a floor")
    }

    /** Reads a longitude as an x and a latitude as a y, one degree one pixel. */
    private val identity = ScreenProjection(point = { it }, position = { it })

    @Test
    fun aWaypointIsPlacedWhereAClickFallsOnAnActiveSegmentWithin40PxOfIt() {
        val map = MapEngine(client())
        map.setActiveSegments(listOf(listOf(listOf(0.0, 10.0), listOf(100.0, 10.0))))

        map.beginWaypointEdit("w1")
        assertEquals(WaypointEdit("w1"), map.state.value.waypointEdit)
        map.placeWaypoint(listOf(50.0, 30.0), identity)
        assertEquals(WaypointEdit("w1", listOf(50.0, 10.0), isSnapped = true), map.state.value.waypointEdit)
        map.placeWaypoint(listOf(50.0, 51.0), identity)
        assertEquals(WaypointEdit("w1", listOf(50.0, 51.0)), map.state.value.waypointEdit)
        map.setActiveSegments(emptyList())
        map.placeWaypoint(listOf(50.0, 11.0), identity)
        assertEquals(
            false,
            map.state.value.waypointEdit
                ?.isSnapped,
            "no segment, no magnet",
        )

        assertEquals(WaypointEdit("w1", listOf(50.0, 11.0)), map.endWaypointEdit())
        assertNull(map.state.value.waypointEdit)
        assertNull(map.endWaypointEdit())
    }

    @Test
    fun anEditBegunGoesOnAnotherWaypointsBeginsInItsPlaceAndNoEditPlacesNothing() {
        val map = MapEngine(client())

        map.placeWaypoint(listOf(6.0, 45.0), identity)
        assertNull(map.state.value.waypointEdit)
        map.beginWaypointEdit("w1", listOf(6.0, 45.0))
        map.placeWaypoint(listOf(190.0, 46.0), identity)
        assertEquals(
            listOf(-170.0, 46.0),
            map.state.value.waypointEdit
                ?.position,
            "a click's longitude within [-180, 180]",
        )
        map.beginWaypointEdit("w1", listOf(7.0, 45.0))
        assertEquals(
            listOf(-170.0, 46.0),
            map.state.value.waypointEdit
                ?.position,
            "the edit begun goes on as it is",
        )
        map.beginWaypointEdit("w2")
        assertEquals(WaypointEdit("w2"), map.state.value.waypointEdit)
        assertEquals(
            MapException(MapException.Kind.PositionRefused, "w3"),
            assertFailsWith<MapException> { map.beginWaypointEdit("w3", listOf(6.0, 91.0)) },
        )
        assertEquals(
            MapException(MapException.Kind.PositionRefused, "w2"),
            assertFailsWith<MapException> { map.placeWaypoint(listOf(Double.NaN, 45.0), identity) },
        )
        assertEquals(WaypointEdit("w2"), map.state.value.waypointEdit)
    }

    @Test
    fun aCameraTheAppMovesOrTheRendererReportsIsTheStatesEveryMemberStated() {
        val map = MapEngine(client())
        map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 12.0, bearing = 0.0, pitch = 0.0))
        assertEquals(CameraPose(listOf(6.86, 45.83), 12.0, 0.0, 0.0), map.state.value.camera)

        val moved = CameraPose(listOf(7.0, 46.0), 13.0, 20.0, 90.0)
        map.cameraMoved(moved)
        assertEquals(moved, map.state.value.camera)
        map.setStyle(Url("https://example.com/style.json"))
        map.setStyle(null)
        assertEquals(null, map.state.value.style)
    }

    /** Answers each request with [status] where it states one, and a Data API query with the terrain sample it is asked for. */
    private class Answers(
        private val status: Int? = null,
    ) : HttpExchange {
        private companion object {
            /** An error envelope refusing a payment. */
            const val REFUSED = """{"errors":[{"extensions":{"code":"PAYMENT_REQUIRED"},"message":"no"}]}"""
        }

        val sent = mutableListOf<ExchangeRequest>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += request
            if (status != null) {
                return ExchangeResponse(
                    status,
                    mapOf("Content-Type" to "application/json"),
                    ExchangeBody(REFUSED.encodeToByteArray()),
                )
            }
            val body = request.body?.decodeToString().orEmpty()
            val answer =
                if (body.contains("\"query\"")) {
                    """{"data": {"terrainSamples": {"samples": [{"elevation": 1834}]}}}"""
                } else {
                    """{"errors": [{"message": "PersistedQueryNotFound", "extensions": {"code": "PERSISTED_QUERY_NOT_FOUND"}}]}"""
                }
            return ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(answer.encodeToByteArray()),
            )
        }
    }

    /** Answers one fix, counting how often it was asked. */
    private class Fixed(
        private val fix: GeolocationFix,
    ) : Geolocation {
        var asked = 0
            private set

        override suspend fun locate(): GeolocationFix {
            asked += 1
            return fix
        }

        override suspend fun permission(): GeolocationPermission = GeolocationPermission.Granted
    }

    /**
     * Closes the map it answers while the fix is on its way, as a reader leaving the screen does while the device
     * looks for one.
     */
    private class Closing : Geolocation {
        var map: MapEngine? = null

        override suspend fun locate(): GeolocationFix {
            map?.close()
            return GeolocationFix.Located(listOf(6.86, 45.83), 25.0)
        }

        override suspend fun permission(): GeolocationPermission = GeolocationPermission.Granted
    }

    /** An exchange answering every request with one tile specification, counting the requests it answered. */
    private class Specifications(
        specification: TileSchema.TileSpecification,
    ) : HttpExchange {
        private val body =
            "{\"data\": " + Json.encodeToString(TileSchema.TileSpecification.serializer(), specification) + "}"
        var sent = 0
            private set

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += 1
            return ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(body.encodeToByteArray()),
            )
        }
    }

    /** A map drawn on a scripted surface, its camera tilted and turned, its client reaching the Tile API at `https://tiles.test/`. */
    private fun drawn(
        geolocation: Geolocation = InertGeolocation(),
        exchange: HttpExchange = Catalogue(),
    ): Pair<MapEngine, ScriptedMapSurface> {
        val map =
            MapEngine(
                Mountaya(
                    Credential.Publishable("pk_test_a", SessionToken.Value("sess_a")),
                    baseUrls = BaseUrls(tiles = Url("https://tiles.test/")),
                    exchange = exchange,
                    dispatcher = Dispatchers.Unconfined,
                ),
                camera = CameraPose(listOf(6.86, 45.83), 12.0, 10.0, 20.0),
                labelLanguage = RestSchema.Language.En,
                geolocation = geolocation,
            )
        return map to ScriptedMapSurface(map)
    }

    private val point = GeoJsonPoint(listOf(6.86, 45.83))

    private fun feature(layer: String): QueriedFeature = QueriedFeature(emptyMap(), point, layer)

    /** Collects every event [map] tells from now on into a list, from a collector that begins at once. */
    private fun TestScope.listen(map: MapEngine): Pair<MutableList<MapEvent>, Job> {
        val told = mutableListOf<MapEvent>()
        val reading = launch(UnconfinedTestDispatcher(testScheduler)) { map.events().collect { told += it } }
        return told to reading
    }

    @Test
    fun aCameraMoveHandsTheRendererTheMembersItStatesAndTheStateHoldsWhereItLands() {
        val (map, surface) = drawn()
        map.jump(CameraTarget(center = listOf(197.5, 0.0), pitch = 0.0))
        map.ease(CameraTarget(zoom = 30.0))
        map.fly(CameraTarget(bearing = -30.0))

        assertEquals(
            listOf(
                ScriptedMapSurface.Move(CameraTarget(center = listOf(197.5, 0.0), pitch = 0.0), CameraMotion.Jump),
                ScriptedMapSurface.Move(CameraTarget(zoom = 30.0), CameraMotion.Ease(null)),
                ScriptedMapSurface.Move(CameraTarget(bearing = -30.0), CameraMotion.Fly),
            ),
            surface.moves,
        )
        assertEquals(
            CameraPose(listOf(197.5, 0.0), 25.5, 0.0, -30.0),
            map.state.value.camera,
            "the state holds the zoom within the window, the move hands it as stated",
        )
    }

    @Test
    fun aCameraMoveThatNamesNoPlaceOrNoNumberIsRefusedBeforeAnythingMoves() {
        val (map, surface) = drawn()
        val before = map.state.value

        for ((target, kind) in listOf(
            CameraTarget(center = listOf(6.0, 90.0001)) to MapException.Kind.PositionRefused,
            CameraTarget(center = listOf(6.86, 45.83, 1035.0)) to MapException.Kind.PositionRefused,
            CameraTarget(center = listOf(6.86, 45.83), zoom = Double.NaN) to MapException.Kind.NumberRefused,
            CameraTarget(bearing = Double.POSITIVE_INFINITY) to MapException.Kind.NumberRefused,
        )) {
            assertEquals(kind, assertFailsWith<MapException> { map.fly(target) }.kind, "$target")
        }
        assertTrue(surface.moves.isEmpty())
        assertEquals(before, map.state.value)
    }

    @Test
    fun aFitFramesOnTheSurfacesCanvasFacingNorthAndAMapNoViewDrawsFramesNothing() {
        val lonely = MapEngine(client(), geolocation = InertGeolocation())
        lonely.fitBounds(listOf(6.8, 45.8, 6.95, 45.9))
        assertEquals(0.0, lonely.state.value.camera.zoom, "no canvas, no fit")

        val (map, surface) = drawn()
        map.fitBounds(listOf(6.95, 45.9, 6.8, 45.8), CameraFit(padding = PixelPadding(48.0), maxZoom = 15.0))
        assertEquals(
            listOf(
                ScriptedMapSurface.Frame(
                    listOf(6.8, 45.8, 6.95, 45.9),
                    CameraFit(padding = PixelPadding(48.0), maxZoom = 15.0),
                ),
            ),
            surface.frames,
        )
        assertTrue(abs(map.state.value.camera.zoom - 11.269337026503) < 1e-9, "${map.state.value.camera}")
        assertEquals(0.0, map.state.value.camera.bearing)
        assertEquals(10.0, map.state.value.camera.pitch)
        for ((bounds, fit, kind) in listOf(
            Triple(listOf(6.8, 45.8, 6.95), CameraFit(), MapException.Kind.PositionRefused),
            Triple(listOf(Double.NaN, 45.8, 6.95, 45.9), CameraFit(), MapException.Kind.PositionRefused),
            Triple(
                listOf(6.8, 45.8, 6.95, 45.9),
                CameraFit(padding = PixelPadding(-1.0)),
                MapException.Kind.NumberRefused,
            ),
            Triple(
                listOf(6.8, 45.8, 6.95, 45.9),
                CameraFit(maxZoom = Double.POSITIVE_INFINITY),
                MapException.Kind.NumberRefused,
            ),
            Triple(
                listOf(6.8, 45.8, 6.95, 45.9),
                CameraFit(duration = (-250).milliseconds),
                MapException.Kind.NumberRefused,
            ),
        )) {
            assertEquals(kind, assertFailsWith<MapException> { map.fitBounds(bounds, fit) }.kind, "$bounds $fit")
        }
        assertEquals(1, surface.frames.size)
    }

    @Test
    fun aDrawingCallDrawsThenFramesTheBoxItsSpecificationStatesItsFitReadFirst() =
        runTest {
            val source =
                TileSchema.TileSource(
                    id = "collection",
                    layers =
                        listOf(
                            TileSchema.TileLayer(
                                "lines",
                                key = "lines",
                                layout = null,
                                paint = null,
                                source_layer = "itinerary_lines",
                                type = TileSchema.TileLayerType.Line,
                                zoom_max = 22,
                                zoom_min = 0,
                            ),
                        ),
                    tiles = listOf("https://tiles.test/c/{z}/{x}/{y}"),
                    type = TileSchema.TileSourceType.Vector,
                    zoom_max = 14,
                    zoom_min = 0,
                )
            val unboxed = Specifications(TileSchema.TileSpecification(listOf(source)))
            val (map, surface) = drawn(exchange = unboxed)
            assertEquals(
                MapException(MapException.Kind.NumberRefused, "padding"),
                assertFailsWith<MapException> {
                    map.setCollection(
                        "c",
                        fit = CameraFit(padding = PixelPadding(Double.NaN)),
                    )
                },
            )
            assertTrue(
                map.state.value.layers
                    .isEmpty(),
                "a refused fit draws nothing",
            )
            assertEquals(0, unboxed.sent, "a refused fit reads nothing")
            assertTrue(map.setCollection("c", fit = CameraFit()))
            assertTrue(surface.frames.isEmpty(), "a specification stating no box frames nothing")

            val boxed =
                Specifications(
                    TileSchema.TileSpecification(listOf(source)) { bbox = listOf(179.8, -17.0, -179.9, -16.0) },
                )
            val (across, framed) = drawn(exchange = boxed)
            assertTrue(across.setCollection("c", fit = CameraFit(maxZoom = 10.0)))
            val box = framed.frames.first().bounds
            assertTrue(box[0] == 179.8 && abs(box[2] - 180.1) < 1e-9 && box[3] == -16.0, "$box")
            assertTrue(across.setCollection("c"))
            assertEquals(1, framed.frames.size, "no fit asked, none framed")
            across.close()
            assertFalse(across.setCollection("c", fit = CameraFit()))
            assertEquals(2, boxed.sent, "a closed map reads nothing")
        }

    @Test
    fun aZoomControlStepsFromTheZoomHeldWithinTheWindowAndAResetTurnsNorthAndLevel() {
        val (map, surface) = drawn()
        map.setZoomBounds(ZoomLimit.Zoom(2.0), ZoomLimit.Zoom(12.5))
        map.zoomIn()
        assertEquals(12.5, map.state.value.camera.zoom)
        map.zoomOut()
        map.zoomOut()
        assertEquals(10.5, map.state.value.camera.zoom)
        map.resetNorth()
        assertEquals(CameraPose(listOf(6.86, 45.83), 10.5, 0.0, 0.0), map.state.value.camera)
        assertEquals(
            listOf(
                CameraTarget(zoom = 12.5),
                CameraTarget(zoom = 11.5),
                CameraTarget(zoom = 10.5),
                CameraTarget(bearing = 0.0, pitch = 0.0),
            ),
            surface.moves.map { it.target },
        )
        assertEquals<CameraMotion>(CameraMotion.Ease(1.seconds), surface.moves.last().motion)
        assertEquals<CameraMotion>(CameraMotion.Ease(250.milliseconds), surface.moves.first().motion)
        map.easeIntoZoomRange(ZoomBounds(14.0, 16.0))
        assertEquals(12.5, map.state.value.camera.zoom, "eased to the window's floor, held within the map's")
        map.easeIntoZoomRange(ZoomBounds(8.0, 13.0))
        map.easeIntoZoomRange(ZoomBounds(9.0, 3.0))
        assertEquals(5, surface.moves.size, "a camera inside a window, or before none, stays")
    }

    @Test
    fun aControlsMoveForAReaderWhoTurnedAnimationsOffLandsWhereItsEaseWouldAtOnce() {
        val (eased, easedSurface) = drawn()
        val (jumped, jumpedSurface) = drawn()
        for (move in listOf(ControlMove.ZoomIn, ControlMove.ResetNorth, ControlMove.ZoomOut)) {
            eased.controlMove(move)
            jumped.controlMove(move, jumps = true)
        }

        assertEquals(eased.state.value.camera, jumped.state.value.camera)
        assertEquals(easedSurface.moves.map { it.target }, jumpedSurface.moves.map { it.target })
        assertEquals(List(3) { CameraMotion.Jump }, jumpedSurface.moves.map { it.motion })
        assertEquals(
            listOf<CameraMotion>(
                CameraMotion.Ease(250.milliseconds),
                CameraMotion.Ease(1.seconds),
                CameraMotion.Ease(250.milliseconds),
            ),
            easedSurface.moves.map { it.motion },
        )
    }

    @Test
    fun aLocateForAReaderWhoTurnedAnimationsOffFramesTheSameBoxAtOnce() =
        runTest {
            val fixed = Fixed(GeolocationFix.Located(listOf(6.86, 45.83), 100.0))
            val (map, surface) = drawn(geolocation = fixed)

            map.locate()
            map.locate(jumps = true)

            val (eased, jumped) = surface.frames
            assertEquals(eased.bounds, jumped.bounds)
            assertEquals(CameraFit(maxZoom = 16.0), eased.fit)
            assertEquals(CameraFit(maxZoom = 16.0, duration = Duration.ZERO), jumped.fit)
        }

    @Test
    fun aZoomWindowPlaysOnTheRenderersOwnLimitsTheSurfacesWhereOneDrawsTheMap() =
        runTest {
            val map = MapEngine(client(), geolocation = InertGeolocation())
            val surface = ScriptedMapSurface(map, zoomLimits = ZoomBounds(-2.0, 22.0))
            map.setZoomBounds(ZoomLimit.Zoom(-2.0), ZoomLimit.Zoom(12.0))
            assertEquals(ZoomBounds(-2.0, 12.0), map.state.value.zoomBounds)
            map.setZoomBounds(max = ZoomLimit.Released)
            assertEquals(ZoomBounds(-2.0, 22.0), map.state.value.zoomBounds)
            assertEquals(-2.0, surface.zoomLimits.minZoom)

            var told = 0
            val reading = launch(UnconfinedTestDispatcher(testScheduler)) { map.state.collect { told += 1 } }
            map.setZoomBounds(min = ZoomLimit.Zoom(-2.0))
            map.setZoomBounds()
            map.setZoomBounds(min = ZoomLimit.Zoom(4.0))
            map.close()
            reading.cancel()
            assertEquals(2, told, "the state now, and one window moved")
        }

    @Test
    fun locatingFramesTheFixsAccuracyBoxAtZoom16AtMostAndARefusedFixMovesNothing() =
        runTest {
            val fixed = Fixed(GeolocationFix.Located(listOf(-73.9749, 40.7736), 100.0))
            val (map, surface) = drawn(geolocation = fixed)
            assertEquals(listOf(-73.9749, 40.7736), map.locate())
            val frame = surface.frames.last()
            assertEquals(CameraFit(maxZoom = 16.0), frame.fit)
            assertTrue(abs(frame.bounds[0] - -73.976086214142) < 1e-9 && abs(frame.bounds[3] - 40.774498315284) < 1e-9)

            for (accuracy in listOf(-5.0, Double.NaN, Double.POSITIVE_INFINITY)) {
                val (vague, vagueSurface) =
                    drawn(
                        geolocation = Fixed(GeolocationFix.Located(listOf(6.86, 45.83), accuracy)),
                    )
                vague.locate()
                assertEquals(
                    listOf(listOf(6.86, 45.83, 6.86, 45.83)),
                    vagueSurface.frames.map { it.bounds },
                    "$accuracy",
                )
            }
            for (reason in GeolocationRefusal.entries) {
                val (refused, refusedSurface) = drawn(geolocation = Fixed(GeolocationFix.Refused(reason)))
                val refusal = assertFailsWith<MapException> { refused.locate() }
                assertEquals(MapException(MapException.Kind.FixRefused, "locate", reason), refusal)
                assertTrue(refusedSurface.frames.isEmpty() && refusedSurface.moves.isEmpty())
            }
            val asking = Fixed(GeolocationFix.Located(listOf(6.86, 45.83), 25.0))
            val (closed, _) = drawn(geolocation = asking)
            closed.close()
            assertEquals(MapException.Kind.Closed, assertFailsWith<MapException> { closed.locate() }.kind)
            assertEquals(0, asking.asked, "a closed map asks the device nothing")

            val closing = Closing()
            val (left, leftSurface) = drawn(geolocation = closing)
            closing.map = left
            val camera = left.state.value.camera
            assertEquals(listOf(6.86, 45.83), left.locate())
            assertTrue(leftSurface.frames.isEmpty(), "a fix that arrives after the map closed frames nothing")
            assertEquals(camera, left.state.value.camera)
        }

    @Test
    fun aMapGivenNoGeolocationAsksTheDevicesWhereAViewReachedItAndAnswersNoApiWhereNoneDid() =
        runTest {
            val map = MapEngine(client())
            val surface = ScriptedMapSurface(map)
            assertEquals(
                MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.NoApi),
                assertFailsWith<MapException> { map.locate() },
            )
            val device = Fixed(GeolocationFix.Located(listOf(6.86, 45.83), 25.0))
            map.deviceGeolocation = device
            assertEquals(listOf(6.86, 45.83), map.locate())
            assertEquals(1, device.asked)
            assertEquals(1, surface.frames.size)

            val handed = Fixed(GeolocationFix.Refused(GeolocationRefusal.Denied))
            val own = MapEngine(client(), geolocation = handed)
            own.deviceGeolocation = device
            assertEquals(GeolocationRefusal.Denied, assertFailsWith<MapException> { own.locate() }.reason)
            assertEquals(1, handed.asked, "the app's own geolocation stands over the device's")
            assertEquals(1, device.asked)
        }

    @Test
    fun aPointQueryAsksAboutTheLayersPlacedInItsBandsTheLiveStyleHoldsNoneElse() {
        val (map, surface) = drawn()
        map.addLayer(MapLayer("line", TileSchema.TileLayerType.Line, "s", rank = 50))
        map.addLayer(MapLayer("fill", TileSchema.TileLayerType.Fill, "s", rank = 40))
        map.addLayer(MapLayer("hut", TileSchema.TileLayerType.Symbol, "s", rank = 80))
        surface.rendered = listOf(feature("hut"), feature("road"), feature("line"))

        assertEquals(listOf("hut", "line"), map.featuresAt(listOf(1.0, 2.0)).map { it.layer })
        assertEquals(listOf("line"), map.itinerariesAt(listOf(1.0, 2.0)).map { it.layer })
        assertEquals(listOf("hut"), map.poisAt(listOf(1.0, 2.0)).map { it.layer })
        assertTrue(map.zonesAt(listOf(1.0, 2.0)).isEmpty())
        assertEquals(
            listOf(listOf("line", "fill", "hut"), listOf("line"), listOf("hut"), listOf("fill")),
            surface.queries,
        )
        map.close()
        assertTrue(map.featuresAt(listOf(1.0, 2.0)).isEmpty())
        assertEquals(4, surface.queries.size, "a closed map asks the renderer nothing")
    }

    @Test
    fun aClickIsToldAndAgainWithTheMapsFeaturesItLandedOnAndAnEditTakesAGesture() =
        runTest {
            val (map, surface) = drawn()
            map.addLayer(MapLayer("line", TileSchema.TileLayerType.Line, "s", rank = 50))
            map.addMarker(MapMarker("hut", listOf(100.0, 80.0)))
            val (told, reading) = listen(map)
            surface.rendered = listOf(feature("line"))
            surface.click(listOf(110.0, 80.0), listOf(-540.5, 10.0))
            surface.rendered = emptyList()
            surface.click(listOf(200.0, 200.0), listOf(180.0, 45.0))
            surface.longClick(listOf(200.0, 200.0), listOf(-180.0, 45.0))
            map.beginWaypointEdit("w1")
            surface.click(listOf(5.0, 6.0), listOf(6.0, 45.0))
            surface.longClick(listOf(5.0, 6.0), listOf(6.0, 45.0))
            map.endWaypointEdit()
            map.close()
            reading.join()

            assertEquals<List<MapEvent>>(
                listOf(
                    MapEvent.Click(MapTap(listOf(179.5, 10.0), listOf(110.0, 80.0), "hut")),
                    MapEvent.FeatureClick(listOf(179.5, 10.0), listOf(feature("line"))),
                    MapEvent.Click(MapTap(listOf(180.0, 45.0), listOf(200.0, 200.0))),
                    MapEvent.LongClick(MapTap(listOf(-180.0, 45.0), listOf(200.0, 200.0))),
                ),
                told,
            )
        }

    @Test
    fun aHoverIsToldOnceAFrameWhereItEndedAndWithNoneAsItLeaves() =
        runTest {
            val (map, surface) = drawn()
            map.addLayer(MapLayer("point", TileSchema.TileLayerType.Circle, "s", rank = 90))
            map.addLayer(MapLayer("fill", TileSchema.TileLayerType.Fill, "s", rank = 40))
            surface.hover(listOf(1.0, 1.0), listOf(6.0, 45.0))
            surface.drawFrame()
            assertTrue(surface.queries.isEmpty(), "nothing is asked while nobody listens")

            val (told, reading) = listen(map)
            surface.rendered = listOf(feature("point"))
            surface.hover(listOf(1.0, 1.0), listOf(6.0, 45.0))
            surface.hover(listOf(2.0, 2.0), listOf(181.0, 45.0))
            surface.drawFrame()
            surface.drawFrame()
            surface.hover(listOf(3.0, 3.0), listOf(7.0, 46.0))
            surface.leave(listOf(4.0, 4.0), listOf(-190.0, 45.0))
            surface.drawFrame()
            map.close()
            reading.join()

            assertEquals<List<MapEvent>>(
                listOf(
                    MapEvent.FeatureHover(listOf(-179.0, 45.0), listOf(feature("point"))),
                    MapEvent.FeatureHover(listOf(170.0, 45.0), emptyList()),
                ),
                told,
            )
            assertEquals(listOf(listOf("point")), surface.queries)
        }

    @Test
    fun hotPathBudgetHoverHitTestFiveMovesAFrameForSixtyFramesCostAHitTestAFrameAndNoneWithNobodyListening() =
        runTest {
            val (map, surface) = drawn()
            val hovered = listOf("trail-casing", "trail", "trail-points")
            map.addLayer(MapLayer("trail-casing", TileSchema.TileLayerType.Line, "s", rank = 50))
            map.addLayer(MapLayer("trail", TileSchema.TileLayerType.Line, "s", rank = 50))
            map.addLayer(MapLayer("trail-points", TileSchema.TileLayerType.Symbol, "s", rank = 90))
            map.addLayer(MapLayer("zones", TileSchema.TileLayerType.Fill, "s", rank = 40))
            map.addLayer(MapLayer("huts", TileSchema.TileLayerType.Circle, "s", rank = 80))
            surface.rendered = listOf(feature("trail"))
            val frames = 60

            // The pointer moves five times a frame across the canvas, and each frame is drawn.
            fun burst() {
                for (frame in 0 until frames) {
                    for (move in 0 until 5) {
                        val step = (frame * 5 + move).toDouble()
                        surface.hover(listOf(step, 100.0), listOf(6.8 + step / 1_000, 45.83))
                    }
                    surface.drawFrame()
                }
            }

            // Nothing is asked of the renderer while nobody listens.
            burst()
            assertTrue(surface.queries.isEmpty(), "${surface.queries.size} hit-tests with nobody listening")

            val (told, reading) = listen(map)
            burst()
            map.close()
            reading.join()

            // The work happened: each frame told the features under the place the pointer ended it on.
            val hovers = told.filterIsInstance<MapEvent.FeatureHover>().filter { it.features.size == 1 }
            assertEquals(frames, hovers.size)
            assertEquals(listOf(6.8 + (frames * 5 - 1) / 1_000.0, 45.83), hovers.last().position)
            // One hit-test a frame, over every layer a hover is told of at once.
            assertTrue(
                surface.queries.size <= HOVER_HIT_TEST_BUDGET * frames,
                "${surface.queries.size} hit-tests over $frames frames",
            )
            assertTrue(surface.queries.all { it == hovered }, "${surface.queries.toSet()}")
        }

    @Test
    fun aRefusalOfAccessIsToldAndStandsUntilItsSourceAnswersAnyOtherIsAFailure() =
        runTest {
            val (map, surface) = drawn(exchange = Answers(status = 402))
            val (told, reading) = listen(map)
            assertFailsWith<Exception> { map.setTerrain() }
            assertTrue(map.state.value.isAccessRequired)
            map.addSource(MapSource("slope", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
            surface.failRequest("https://tiles.test/slope/1/2/3", 403, "slope")
            surface.failRequest("https://tiles.test/slope/1/2/4", 404, "slope")
            surface.failRequest("https://tiles.test/slope/1/2/5", null, "slope")
            surface.loadStyle()
            assertTrue(map.state.value.isAccessRequired, "the source's refusal stands past the style")
            surface.loadTile("slope")
            assertFalse(map.state.value.isAccessRequired)
            map.close()
            reading.join()

            assertEquals<List<MapEvent>>(
                listOf(
                    MapEvent.AccessRefused(402, "https://tiles.test/v1/tile-layers"),
                    MapEvent.AccessRefused(403, "https://tiles.test/slope/1/2/3"),
                    MapEvent.Failure("https://tiles.test/slope/1/2/4", 404),
                    MapEvent.Failure("https://tiles.test/slope/1/2/5", null),
                    MapEvent.AccessRestored,
                ),
                told,
            )
        }

    /**
     * A request that never left because the client's credential could not be read for it is a failure carrying what the
     * read threw, with no status whatever the request would have met, never a refusal of access and never given up as
     * one of a credential the map no longer sends (`map.events.access-refused`).
     */
    @Test
    fun aRequestWhoseCredentialCouldNotBeReadIsAFailureCarryingWhatTheReadThrew() =
        runTest {
            val (map, surface) = drawn()
            val (told, reading) = listen(map)
            val read = IllegalStateException("The token store is locked.")
            surface.failRequest("https://tiles.test/v1/zones/12/2130/1462", null, null, cause = read)
            surface.failRequest("https://tiles.test/v1/zones/12/2130/1463", 403, null, cause = read)
            surface.failRequest("https://tiles.test/v1/zones/12/2130/1464", 401, null, superseded = true, cause = read)
            assertFalse(map.state.value.isAccessRequired, "a request that never left refuses nothing")
            map.close()
            reading.join()

            assertEquals<List<MapEvent>>(
                listOf(
                    MapEvent.Failure("https://tiles.test/v1/zones/12/2130/1462", null, read),
                    MapEvent.Failure("https://tiles.test/v1/zones/12/2130/1463", null, read),
                    MapEvent.Failure("https://tiles.test/v1/zones/12/2130/1464", null, read),
                ),
                told,
            )
            assertTrue(told.all { (it as MapEvent.Failure).cause === read })
        }

    /**
     * A 401 or a 403 of a request that left under a credential the client no longer sends refuses one the map gave
     * up: it is told to nobody, stands for nothing and ends nothing, and the engine answers it gave it up so, which a
     * renderer asks a style again on. A 402, the entitlement's, and every other failure are told as they are.
     */
    @Test
    fun aRefusalOfACredentialTheClientNoLongerSendsIsToldToNobodyAndStandsForNothing() =
        runTest {
            val (map, surface) = drawn()
            val (told, reading) = listen(map)
            map.addSource(MapSource("slope", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))

            assertTrue(map.requestFailed("https://tiles.test/slope/1/2/3", 401, "slope", superseded = true))
            assertTrue(map.requestFailed("https://tiles.test/slope/1/2/4", 403, null, superseded = true))
            assertFalse(map.state.value.isAccessRequired, "a refusal of a credential given up stands for nothing")
            surface.failRequest("https://tiles.test/slope/1/2/5", 401, "slope")
            assertTrue(map.state.value.isAccessRequired)
            surface.failRequest("https://tiles.test/slope/1/2/6", 403, "slope", superseded = true)
            assertTrue(map.state.value.isAccessRequired, "nor does it end one standing")
            assertFalse(map.requestFailed("https://tiles.test/slope/1/2/7", 402, "slope", superseded = true))
            assertFalse(map.requestFailed("https://tiles.test/slope/1/2/8", 404, "slope", superseded = true))
            assertFalse(map.requestFailed("https://tiles.test/slope/1/2/9", null, "slope", superseded = true))
            assertFalse(map.requestFailed("https://tiles.test/slope/1/2/10", 401, "slope"))
            surface.loadTile("slope")
            assertFalse(map.state.value.isAccessRequired)
            map.close()
            reading.join()

            assertEquals<List<MapEvent>>(
                listOf(
                    MapEvent.AccessRefused(401, "https://tiles.test/slope/1/2/5"),
                    MapEvent.AccessRefused(402, "https://tiles.test/slope/1/2/7"),
                    MapEvent.Failure("https://tiles.test/slope/1/2/8", 404),
                    MapEvent.Failure("https://tiles.test/slope/1/2/9", null),
                    MapEvent.AccessRefused(401, "https://tiles.test/slope/1/2/10"),
                    MapEvent.AccessRestored,
                ),
                told,
            )
            assertFalse(
                map.requestFailed("https://tiles.test/x", 401, null, superseded = true),
                "a closed map gives nothing up",
            )
        }

    /**
     * A style is one of the map's own requests: refused, it is told once, as a refusal of access, and its failing to
     * load, which gives the map back the base map shown, tells nothing more; failing for any other reason, or with no
     * answer, it is told once, as a failure.
     */
    @Test
    fun aStyleRefusedIsToldOnceAsARefusalAndItsFailingToLoadTellsNothingMore() =
        runTest {
            val style = "https://tiles.test/v1/terrains/outdoor/light/style"
            val failures =
                listOf(
                    401 to MapEvent.AccessRefused(401, style),
                    402 to MapEvent.AccessRefused(402, style),
                    403 to MapEvent.AccessRefused(403, style),
                    404 to MapEvent.Failure(style, 404),
                    null to MapEvent.Failure(style, null),
                )
            for ((status, expected) in failures) {
                val (map, surface) = drawn()
                val (told, reading) = listen(map)
                surface.failRequest(style, status, null)
                surface.failStyle()
                map.close()
                reading.join()

                assertEquals<List<MapEvent>>(listOf(expected), told, "a style failing with $status")
            }
        }

    /**
     * A base map given back where the one a switch asked for did not arrive answers nothing as it loads again: the
     * refusal of the style that did not arrive stands until a style the map asks for loads.
     */
    @Test
    fun aBaseMapGivenBackAfterARefusedSwitchEndsNoRefusal() =
        runTest {
            val (map, surface) = drawn()
            val (told, reading) = listen(map)
            val shown = Url("https://tiles.test/outdoors.json")
            map.setStyle(shown)
            surface.loadStyle()
            map.setStyle(Url("https://tiles.test/satellite.json"))
            surface.failRequest("https://tiles.test/satellite.json", 402, null)
            surface.failStyle()
            assertEquals(shown, map.state.value.style)
            surface.loadStyle()
            assertTrue(map.state.value.isAccessRequired, "the base map given back ends no refusal")
            map.setStyle(Url("https://tiles.test/winter.json"))
            surface.loadStyle()
            assertFalse(map.state.value.isAccessRequired, "a style the map asks for ends it")
            map.close()
            reading.join()

            assertEquals<List<MapEvent>>(
                listOf(MapEvent.AccessRefused(402, "https://tiles.test/satellite.json"), MapEvent.AccessRestored),
                told,
            )
        }

    @Test
    fun theElevationIsTheDataApisSampleAskedForItsElevationAloneClosedOrNot() =
        runTest {
            val answers = Answers()
            val (map, _) = drawn(exchange = answers)
            map.close()
            assertEquals(1834.0, map.elevationAt(listOf(6.86, 45.83)))
            val body =
                answers.sent
                    .last()
                    .body
                    ?.decodeToString()
                    .orEmpty()
            assertTrue(body.contains("\"locations\":[[6.86,45.83]]"), body)
            assertTrue(body.contains("samples") && body.contains("elevation") && !body.contains("slope"), body)
        }

    @Test
    fun aLabelLanguageRelabelsTheBaseMapAloneAndAgainOnEachStyleASwitchBringsIn() {
        val (map, surface) = drawn()
        surface.baseLabels =
            listOf(
                ScriptedMapSurface.Label("place-label", JsonPrimitive("{name}")),
                ScriptedMapSurface.Label("road-shield", JsonPrimitive("{ref}")),
            )
        map.addLayer(
            MapLayer(
                "waypoint-label",
                TileSchema.TileLayerType.Symbol,
                "s",
                rank = 90,
                layout = mapOf("text-field" to JsonPrimitive("{name}")),
            ),
        )
        map.setLabelLanguage(RestSchema.Language.Fr)
        assertEquals(listOf("place-label"), surface.relabelled)
        surface.baseLabels =
            listOf(
                ScriptedMapSurface.Label("water-label", JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name")))),
            )
        surface.loadStyle()
        assertEquals(listOf("place-label", "water-label"), surface.relabelled)
    }

    @Test
    fun aClosedMapIsInertNoMoveNoFitNoQueryNoProjectionNoEventNoState() =
        runTest {
            val (map, surface) = drawn()
            val (told, reading) = listen(map)
            map.close()
            reading.join()
            val ended = map.state.value
            map.jump(CameraTarget(zoom = 4.0))
            map.fitBounds(listOf(6.8, 45.8, 6.95, 45.9))
            map.zoomIn()
            map.setZoomBounds(ZoomLimit.Zoom(2.0), ZoomLimit.Zoom(5.0))
            map.setLabelLanguage(RestSchema.Language.De)
            surface.click(listOf(1.0, 2.0), listOf(6.86, 45.83))
            surface.failRequest("https://tiles.test/x", 402, null)

            assertTrue(map.project(listOf(6.86, 45.83)).all { it.isNaN() })
            assertTrue(map.unproject(listOf(1.0, 2.0)).all { it.isNaN() })
            assertTrue(surface.moves.isEmpty() && surface.frames.isEmpty() && surface.relabelled.isEmpty())
            assertSame(ended, map.state.value)
            val after = mutableListOf<MapEvent>()
            map.events().collect { after += it }
            assertTrue(told.isEmpty() && after.isEmpty(), "$told $after")
            assertEquals(1, map.credits.size)
        }

    // Content.

    /**
     * Answers the Tile API's catalogue, with two classic overlays and an enrichment, and each specification, with
     * one source drawing a line `line-` then what it names, recording the path and the query of every request.
     */
    private class Contents : HttpExchange {
        val paths = mutableListOf<String>()
        val queries = mutableListOf<String>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val address = request.url.toString()
            val path = "/" + address.substringBefore('?').substringAfter("://").substringAfter('/')
            paths += path
            queries += address.substringAfter('?', "")
            val parts = path.split('/').filter { it.isNotEmpty() }
            val body =
                if (parts.lastOrNull() == "tile-layers") {
                    CATALOGUE
                } else {
                    val name = if (parts.size == 4) parts[2] else parts.getOrElse(1) { "" }
                    """{"data": {"sources": [{"id": "source-$name", "layers": [{"id": "line-$name",
                      "key": "", "layout": null, "paint": null, "source_layer": "itinerary_lines",
                      "type": "line", "zoom_max": 22, "zoom_min": 0}],
                      "tiles": ["https://tiles.test/$name/{z}/{x}/{y}"], "type": "vector",
                      "zoom_max": 14, "zoom_min": 0}]}}"""
                }
            return ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(body.encodeToByteArray()),
            )
        }

        private companion object {
            const val CATALOGUE = """{"data": {
              "elevation": {"id": "e", "tile_size": 512, "url": "https://tiles.test/elevation.json"},
              "itineraries": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "i", "locales": {}, "slug": "itineraries", "sources": []},
              "zones": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "z", "locales": {}, "slug": "zones", "sources": []},
              "overlays": [
                {"id": "c", "locales": {}, "slug": "conditions", "children": [
                  {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                    "content_type": "application/x-protobuf", "id": "slope", "locales": {}, "slug": "slope",
                    "sources": [{"id": "slope", "layers": [{"id": "slope-layer", "key": "slope",
                      "layout": null, "paint": null, "source_layer": "slope", "type": "fill",
                      "zoom_max": 22, "zoom_min": 14}], "tiles": ["https://tiles.test/slope/{z}/{x}/{y}"],
                      "type": "vector", "zoom_max": 14, "zoom_min": 0}]},
                  {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                    "content_type": "application/x-protobuf", "id": "aspect", "locales": {}, "slug": "aspect",
                    "sources": [{"id": "aspect", "layers": [{"id": "aspect-layer", "key": "aspect",
                      "layout": null, "paint": null, "source_layer": "aspect", "type": "fill",
                      "zoom_max": 22, "zoom_min": 0}], "tiles": ["https://tiles.test/aspect/{z}/{x}/{y}"],
                      "type": "vector", "zoom_max": 14, "zoom_min": 0}]}]},
                {"id": "e", "locales": {}, "slug": "enrichment", "children": [
                  {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                    "content_type": "application/x-protobuf", "id": "contours", "locales": {},
                    "slug": "contours", "sources": [{"id": "contours", "layers": [{"id": "contours-layer",
                      "key": "contours", "layout": null, "paint": null, "source_layer": "contours",
                      "type": "line", "zoom_max": 22, "zoom_min": 0}],
                      "tiles": ["https://tiles.test/contours/{z}/{x}/{y}"], "type": "vector",
                      "zoom_max": 14, "zoom_min": 0}]}]}],
              "terrains": []}}"""
        }
    }

    private fun layerIds(map: MapEngine): List<String> =
        map.state.value.layers
            .map { it.id }

    private fun layerOf(
        map: MapEngine,
        id: String,
    ): MapLayer? =
        map.state.value.layers
            .firstOrNull { it.id == id }

    private fun fillSpecification(
        id: String,
        key: String,
        sourceLayer: String,
        type: TileSchema.TileSourceType = TileSchema.TileSourceType.Vector,
    ) = TileSchema.TileSpecification(
        listOf(
            TileSchema.TileSource(
                id = "$id-source",
                layers =
                    listOf(
                        TileSchema.TileLayer(
                            id,
                            key = key,
                            layout = null,
                            paint = null,
                            source_layer = sourceLayer,
                            type = TileSchema.TileLayerType.Fill,
                            zoom_max = 22,
                            zoom_min = 0,
                        ),
                    ),
                tiles = listOf("https://tiles.test/$id/{z}/{x}/{y}"),
                type = type,
                zoom_max = 14,
                zoom_min = 0,
            ),
        ),
    )

    @Test
    fun eachKindOfContentIsASlotOfItsOwnDrawnByIdSwitchedOrCleared() =
        runTest {
            val exchange = Contents()
            val (map, _) = drawn(exchange = exchange)
            assertTrue(map.setItinerary("a"))
            assertTrue(map.setCollection("c"))
            map.setItineraries(true)
            map.setItineraries(true)
            map.setZones(false)
            assertEquals(
                listOf(
                    "line-a",
                    "line-c",
                    "line-itineraries",
                    "line-a__road-access-stripe",
                    "line-c__road-access-stripe",
                    "line-itineraries__road-access-stripe",
                    "line-a__road-access-icon",
                    "line-c__road-access-icon",
                    "line-itineraries__road-access-icon",
                ),
                layerIds(map),
            )
            assertEquals(
                listOf(
                    "/v1/itineraries/a/specification",
                    "/v1/collections/c/specification",
                    "/v1/itineraries/specification",
                ),
                exchange.paths,
                "a toggle switched on while it is on reads nothing again",
            )
            assertTrue(map.setItinerary("b"))
            assertFalse("line-a" in layerIds(map), "the latest request takes the slot's place")
            assertEquals(
                listOf("source-c", "source-itineraries", "source-b"),
                map.state.value.sources
                    .map { it.id },
            )
            map.clearItinerary()
            map.clearCollection()
            map.setItineraries(false)
            assertTrue(
                map.state.value.layers
                    .isEmpty(),
            )
            assertTrue(
                map.state.value.sources
                    .isEmpty(),
            )
            map.close()
            assertFalse(map.setItinerary("a"), "a closed map draws nothing")
            assertEquals(4, exchange.paths.size, "and reads nothing")
        }

    @Test
    fun anItineraryDrawnWithAFitReadsItsGeometryBesideItAndAReadThatFailsDrawsNothing() =
        runTest {
            val exchange = Contents()
            val (map, surface) = drawn(exchange = exchange)
            assertEquals(
                MapException(MapException.Kind.NumberRefused, "maxZoom"),
                assertFailsWith<MapException> { map.setItinerary("a", CameraFit(maxZoom = Double.POSITIVE_INFINITY)) },
            )
            assertTrue(exchange.paths.isEmpty(), "a refused fit reads nothing")
            // The exchange answers the itinerary's own read with no itinerary, which fails the call.
            assertFails { map.setItinerary("a", CameraFit()) }
            assertEquals(setOf("/v1/itineraries/a/specification", "/v1/itineraries/a"), exchange.paths.toSet())
            assertTrue(
                map.state.value.layers
                    .isEmpty(),
                "a call that failed drew nothing",
            )
            assertTrue(surface.frames.isEmpty())
        }

    @Test
    fun aSpecificationDrawnUnderANameReplacesWhatItDrewAndKeepsItsPlaceAmongKeys() {
        val map = MapEngine(client())
        map.setSpecification(fillSpecification("relief", "hillshade", "x"), "relief")
        map.setSpecification(fillSpecification("rain", "weather", "y"), "weather")
        map.setSpecification(fillSpecification("contours", "contours", "z"), "relief")
        assertEquals(listOf("relief", "weather"), map.specifications)
        assertEquals(listOf("contours", "weather"), map.drawnLayerKeys())
        assertEquals(
            listOf("rain-source", "contours-source"),
            map.state.value.sources
                .map { it.id },
        )

        val refused = fillSpecification("slope", "slope", "s", TileSchema.TileSourceType("geojson"))
        val before = map.state.value
        assertEquals(
            MapException(MapException.Kind.TypeRefused, "geojson"),
            assertFailsWith<MapException> { map.setSpecification(refused, "relief") },
        )
        assertEquals(before, map.state.value, "a refused specification draws nothing")

        map.setLayerVisibility("rain", false)
        map.removeSpecification("weather")
        assertTrue(map.isLayerVisible("rain"), "a clear forgets the app's word on its layers")
        map.clearSpecifications()
        assertTrue(map.specifications.isEmpty())
        assertTrue(
            map.state.value.layers
                .isEmpty(),
        )
    }

    @Test
    fun theAppsWordOnALayerIsKeptByItsIdBeforeItIsDrawnAndAcrossASwitch() {
        val map = MapEngine(client())
        map.setLayerVisibility("route", false)
        assertFalse(map.isLayerVisible("route"))
        map.addLayer(line)
        assertEquals(
            true,
            map.state.value.layers
                .firstOrNull()
                ?.isHidden,
            "born hidden",
        )
        map.setStyle(Url("https://example.com/other.json"))
        assertEquals(
            true,
            map.state.value.layers
                .firstOrNull()
                ?.isHidden,
            "across a base-map switch",
        )
        map.setLayerVisibility("route", true)
        assertEquals(
            JsonPrimitive("visible"),
            map.state.value.layers
                .firstOrNull()
                ?.layout
                ?.get("visibility"),
        )
        map.setLayerVisibility("route", false)
        map.removeLayer("route")
        assertTrue(map.isLayerVisible("route"), "a layer taken off forgets the word")
        assertTrue(map.isLayerVisible("café"))
        map.setLayerVisibility("café", false)
        assertTrue(map.isLayerVisible("café"), "an id is matched as written")
    }

    @Test
    fun aConceptIsNamedByItsKeyListedOnceAndShownWhileNoneOfItsLayersIsHidden() {
        val map = MapEngine(client())

        fun layer(
            id: String,
            key: String,
            sourceLayer: String,
            type: TileSchema.TileLayerType,
        ) = TileSchema.TileLayer(
            id,
            key = key,
            layout = null,
            paint = null,
            source_layer = sourceLayer,
            type = type,
            zoom_max = 22,
            zoom_min = 0,
        )
        map.setSpecification(
            TileSchema.TileSpecification(
                listOf(
                    TileSchema.TileSource(
                        id = "pois",
                        layers =
                            listOf(
                                layer(
                                    "food-icon",
                                    "poi_food",
                                    "poi_food",
                                    TileSchema.TileLayerType.Symbol,
                                ),
                                layer(
                                    "food-label",
                                    "poi_food",
                                    "poi_food",
                                    TileSchema.TileLayerType.Symbol,
                                ),
                                layer("relief", "hillshade", "x", TileSchema.TileLayerType.Hillshade),
                                layer("unnamed", "", "parking", TileSchema.TileLayerType.Symbol),
                            ),
                        tiles = listOf("https://tiles.test/pois/{z}/{x}/{y}"),
                        type = TileSchema.TileSourceType.Vector,
                        zoom_max = 14,
                        zoom_min = 0,
                    ),
                ),
            ),
            "pois",
        )

        assertEquals(listOf("poi_food", "hillshade"), map.drawnLayerKeys(), "the empty key names no concept")
        assertEquals(listOf("poi_food"), map.drawnLayerKeys(LayerRank.Poi.rank))
        assertEquals(listOf("hillshade"), map.drawnLayerKeys(LayerRank.Hillshade.rank))
        assertTrue(map.drawnLayerKeys(LayerRank.Zone.rank).isEmpty())
        assertEquals(listOf("food-icon", "food-label"), map.layerIds("poi_food"))
        assertTrue(map.layerIds("contours").isEmpty())
        assertTrue(map.isKeyVisible("contours"), "a key that draws nothing reads as shown")
        map.setLayerVisibility("food-label", false)
        assertFalse(map.isKeyVisible("poi_food"))
        map.setKeyVisibility("poi_food", true)
        assertTrue(map.isKeyVisible("poi_food"))
        map.setKeyVisibility("poi_food", false)
        assertTrue(
            map.state.value.layers
                .filter { it.key == "poi_food" }
                .all { it.isHidden },
            "every layer of the concept is hidden",
        )
    }

    @Test
    fun theRestrictedStretchesAreShownUntilTheAppHidesThemAndAWordByIdStands() =
        runTest {
            val (map, _) = drawn(exchange = Contents())
            assertTrue(map.isAccessRestrictionsVisible)
            map.setItinerary("a")
            map.setAccessRestrictionsVisibility(false)
            assertTrue(
                map.state.value.layers
                    .filter { "__road-access-" in it.id }
                    .all { it.isHidden },
            )
            map.setCollection("c")
            assertEquals(
                true,
                layerOf(map, "line-c__road-access-icon")?.isHidden,
                "an itinerary drawn after is born hidden",
            )
            map.setLayerVisibility("line-a__road-access-stripe", false)
            map.setAccessRestrictionsVisibility(true)
            assertEquals(true, layerOf(map, "line-a__road-access-stripe")?.isHidden)
            assertEquals(false, layerOf(map, "line-a__road-access-icon")?.isHidden)
            map.setLayerVisibility("line-c__road-access-stripe", true)
            map.setAccessRestrictionsVisibility(false)
            map.setLayerVisibility("line-c__road-access-stripe", true)
            assertEquals(
                true,
                layerOf(map, "line-c__road-access-stripe")?.isHidden,
                "a stretch is drawn only while the stretches are shown",
            )
        }

    @Test
    fun theReadoutsBandIsTheFirstFeatureOfTheClassicOverlaysOwnLayersStatingAValue() =
        runTest {
            val (map, surface) = drawn(exchange = Contents())

            fun band(
                layer: String,
                min: Double?,
                max: Double?,
            ): QueriedFeature =
                QueriedFeature(
                    listOfNotNull(
                        min?.let { "minValue" to JsonPrimitive(it) },
                        max?.let { "maxValue" to JsonPrimitive(it) },
                    ).toMap(),
                    point,
                    layer,
                )
            surface.rendered = listOf(band("contours-layer", 3.0, 4.0), band("slope-layer", 1.0, 2.0))
            assertNull(map.classicOverlayBandAt(listOf(1.0, 2.0)), "a map drawing no classic overlay states no band")
            map.setOverlay("slope")
            map.addEnrichment("contours")

            assertEquals(
                ReadoutPoint.Band(1.0, 2.0),
                map.classicOverlayBandAt(listOf(1.0, 2.0)),
                "an enrichment's feature above the overlay's is no band",
            )
            surface.rendered = listOf(band("slope-layer", null, null), band("slope-layer", null, 5.0))
            assertEquals(ReadoutPoint.Band(null, 5.0), map.classicOverlayBandAt(listOf(1.0, 2.0)))
            surface.rendered = listOf(band("contours-layer", 3.0, 4.0))
            assertNull(map.classicOverlayBandAt(listOf(1.0, 2.0)), "the overlay draws nothing there")
            map.clearOverlay()
            surface.rendered = listOf(band("slope-layer", 1.0, 2.0))
            assertNull(map.classicOverlayBandAt(listOf(1.0, 2.0)), "a cleared overlay states no band")
        }

    @Test
    fun aClassicOverlayReplacesTheOneDrawnEnrichmentsStackEachLookedUpByItsKind() =
        runTest {
            val exchange = Contents()
            val (map, surface) = drawn(exchange = exchange)
            map.setOverlay("slope")
            assertEquals(listOf("slope-layer"), layerIds(map))
            val eased = surface.moves.last()
            assertEquals(CameraTarget(zoom = 14.0), eased.target)
            assertEquals<CameraMotion>(
                CameraMotion.Ease(250.milliseconds),
                eased.motion,
                "the camera eases into the zooms the overlay paints across",
            )
            map.setOverlay("aspect")
            map.addEnrichment("contours")
            map.addEnrichment("contours")
            assertEquals(listOf("aspect-layer", "contours-layer"), layerIds(map))
            assertEquals(listOf("contours"), map.enrichments)
            assertEquals(
                MapException(MapException.Kind.KindMismatch, "contours"),
                assertFailsWith<MapException> { map.setOverlay("contours") },
            )
            assertEquals(
                MapException(MapException.Kind.KindMismatch, "slope"),
                assertFailsWith<MapException> { map.addEnrichment("slope") },
            )
            assertEquals(
                MapException(MapException.Kind.OverlayNotFound, "snow"),
                assertFailsWith<MapException> { map.addEnrichment("snow") },
            )
            assertEquals(listOf("aspect-layer", "contours-layer"), layerIds(map), "a refusal draws nothing")
            map.clearOverlay()
            map.clearEnrichments()
            assertTrue(
                map.state.value.layers
                    .isEmpty(),
            )
            assertTrue(map.enrichments.isEmpty())
            assertEquals(listOf("/v1/tile-layers"), exchange.paths, "the catalogue is read once")
        }

    /**
     * Answers the Tile API's `getTileLayers` with overlays a server stamps, `snow-depth` and `wind`, one it does not,
     * `slope`, and an enrichment it stamps, `radar`, each one vector source of one fill layer named after its slug.
     */
    private class Stamped : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse =
            ExchangeResponse(200, mapOf("Content-Type" to "application/json"), ExchangeBody(BODY.encodeToByteArray()))

        private companion object {
            fun overlay(
                slug: String,
                server: Boolean,
                tiles: String,
            ): String =
                """{"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": $server}},
                  "content_type": "application/x-protobuf", "id": "$slug", "locales": {}, "slug": "$slug",
                  "sources": [{"id": "$slug", "layers": [{"id": "$slug-layer", "key": "$slug", "layout": null,
                    "paint": null, "source_layer": "$slug", "type": "fill", "zoom_max": 22, "zoom_min": 0}],
                    "tiles": [$tiles], "type": "vector", "zoom_max": 14, "zoom_min": 0}]}"""

            val BODY = """{"data": {
              "elevation": {"id": "e", "tile_size": 512, "url": "https://tiles.test/elevation.json"},
              "itineraries": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "i", "locales": {}, "slug": "itineraries", "sources": []},
              "zones": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "z", "locales": {}, "slug": "zones", "sources": []},
              "overlays": [
                {"id": "c", "locales": {}, "slug": "conditions", "children": [
                  ${overlay("snow-depth", true, "\"https://tiles.test/snow-depth/{z}/{x}/{y}?key=pk\"")},
                  ${overlay(
                "wind",
                true,
                "\"https://tiles.test/wind/{z}/{x}/{y}\", \"https://tiles.test/gusts/{z}/{x}/{y}\"",
            )},
                  ${overlay("slope", false, "\"https://tiles.test/slope/{z}/{x}/{y}\"")}]},
                {"id": "e", "locales": {}, "slug": "enrichment", "children": [
                  ${overlay("radar", true, "\"https://tiles.test/radar/{z}/{x}/{y}\"")}]}],
              "terrains": []}}"""
        }
    }

    /** The addresses the map draws the source [id] from, as it hands them its renderer. */
    private fun tilesOf(
        map: MapEngine,
        id: String,
    ): List<String>? =
        (
            map.state.value.sources
                .firstOrNull { it.id == id }
                ?.kind as? MapSource.Kind.Vector
        )?.tiles

    @Test
    fun aPinAsksTheOverlayTheServerStampsAgainInPlaceItsLayersStandingAndNoneDrawsTheManifestsOwn() =
        runTest {
            val (map, _) = drawn(exchange = Stamped())
            map.setOverlay("snow-depth")
            map.addLayer(MapLayer("mine", TileSchema.TileLayerType.Fill, "snow-depth", rank = LayerRank.Overlay.rank))
            val layers = map.state.value.layers
            assertEquals(listOf("https://tiles.test/snow-depth/{z}/{x}/{y}?key=pk"), tilesOf(map, "snow-depth"))
            assertNull(map.instant)

            map.setInstant(Instant.parse("2026-03-14T09:00:00Z"))
            assertEquals(
                listOf("https://tiles.test/snow-depth/{z}/{x}/{y}?key=pk&timestamp=2026-03-14T09%3A00%3A00.000Z"),
                tilesOf(map, "snow-depth"),
            )
            assertEquals(Instant.parse("2026-03-14T09:00:00Z"), map.instant)
            assertEquals(map.instant, map.pinnedInstant.value, "a readout open follows the pin")
            assertEquals(layers, map.state.value.layers, "the overlay's layers stand where they stood")
            map.setInstant(Instant.parse("2026-03-15T19:30:00+01:00"))
            assertEquals(
                listOf("https://tiles.test/snow-depth/{z}/{x}/{y}?key=pk&timestamp=2026-03-15T18%3A30%3A00.000Z"),
                tilesOf(map, "snow-depth"),
            )
            assertEquals(Instant.parse("2026-03-15T18:30:00Z"), map.pinnedInstant.value)
            map.setInstant(null)

            assertEquals(listOf("https://tiles.test/snow-depth/{z}/{x}/{y}?key=pk"), tilesOf(map, "snow-depth"))
            assertEquals(layers, map.state.value.layers)
            assertNull(map.pinnedInstant.value)
        }

    @Test
    fun anOverlayDrawnUnderAPinIsStampedAsItIsDrawnAndAnEnrichmentOrAnOverlayTheServerDoesNotStampNever() =
        runTest {
            val (map, _) = drawn(exchange = Stamped())
            val stamp = "timestamp=2026-03-14T09%3A00%3A00.000Z"
            map.setInstant(Instant.parse("2026-03-14T09:00:00Z"))
            map.setOverlay("wind")
            assertEquals(
                listOf("https://tiles.test/wind/{z}/{x}/{y}?$stamp", "https://tiles.test/gusts/{z}/{x}/{y}?$stamp"),
                tilesOf(map, "wind"),
            )
            map.addEnrichment("radar")
            assertEquals(listOf("https://tiles.test/radar/{z}/{x}/{y}"), tilesOf(map, "radar"))
            map.setOverlay("slope")
            assertEquals(listOf("https://tiles.test/slope/{z}/{x}/{y}"), tilesOf(map, "slope"))
            assertNull(tilesOf(map, "wind"), "the overlay replaced is drawn no more")
            map.setInstant(Instant.parse("2026-03-15T00:00:00Z"))
            assertEquals(listOf("https://tiles.test/slope/{z}/{x}/{y}"), tilesOf(map, "slope"))
            assertEquals(listOf("https://tiles.test/radar/{z}/{x}/{y}"), tilesOf(map, "radar"))

            // A cleared overlay leaves the pin held, and the overlay drawn again is asked for it.
            map.clearOverlay()
            assertEquals(Instant.parse("2026-03-15T00:00:00Z"), map.instant)
            map.setOverlay("snow-depth")
            assertEquals(
                listOf("https://tiles.test/snow-depth/{z}/{x}/{y}?key=pk&timestamp=2026-03-15T00%3A00%3A00.000Z"),
                tilesOf(map, "snow-depth"),
            )
        }

    @Test
    fun aBaseMapSwitchKeepsTheAddressesAskedForAndAClosedMapPinsNothing() =
        runTest {
            val (map, surface) = drawn(exchange = Stamped())
            map.setOverlay("snow-depth")
            map.setInstant(Instant.parse("2026-03-14T09:00:00Z"))
            val stamped = tilesOf(map, "snow-depth")
            map.setStyle(Url("https://tiles.test/another.json"))
            surface.loadStyle()

            assertEquals(stamped, tilesOf(map, "snow-depth"))
            map.close()
            map.setInstant(null)
            assertEquals(Instant.parse("2026-03-14T09:00:00Z"), map.instant)
            assertEquals(stamped, tilesOf(map, "snow-depth"))
        }

    @Test
    fun theCatalogueIsReadOnceForTheScopeAndToldAndMovingTheScopeDropsIt() =
        runTest {
            val exchange = Contents()
            val (map, _) = drawn(exchange = exchange)
            val (told, reading) = listen(map)
            assertNull(map.layersManifest)
            val first = map.loadLayers()
            val again = map.loadLayers()
            assertEquals(first, again)
            assertEquals(first, map.layersManifest)
            map.setLayersOrganization("")
            map.setLayersOrganization(null)
            assertEquals(first, map.layersManifest, "naming the scope held changes nothing")
            map.setLayersOrganization("org-1")
            assertNull(map.layersManifest)
            map.loadLayers()
            assertEquals(listOf("", "organization_id=org-1"), exchange.queries)
            map.close()
            assertEquals(first, map.loadLayers(), "a closed map answers the catalogue it held")
            reading.join()
            assertEquals(
                listOf<MapEvent>(
                    MapEvent.LayersChange(first),
                    MapEvent.LayersChange(null),
                    MapEvent.LayersChange(first),
                ),
                told,
            )

            val (closed, _) = drawn(exchange = exchange)
            closed.close()
            assertEquals(
                MapException(MapException.Kind.Closed, "loadLayers"),
                assertFailsWith<MapException> { closed.loadLayers() },
            )
            assertEquals(2, exchange.paths.size, "a closed map reads nothing more")
        }

    @Test
    fun aMarkerCarriesItsGlyphAnEditChangesItAndAPlaceOfFourNumbersIsRefused() {
        val map = MapEngine(client())
        map.addMarker(MapMarker("hut", listOf(6.86, 45.83), glyph = "hut"))

        map.editMarker("hut", glyph = "mountain")

        assertEquals(listOf(MapMarker("hut", listOf(6.86, 45.83), glyph = "mountain")), map.state.value.markers)
        assertEquals(
            MapException(MapException.Kind.PositionRefused, "col"),
            assertFailsWith<MapException> { map.addMarker(MapMarker("col", listOf(6.86, 45.83, 2_000.0, 1.0))) },
        )
    }

    @Test
    fun aGeometrySlotDrawsWhatItHoldsInItsColourAnEditKeepsWhatItLeavesNull() {
        val map = MapEngine(client())
        val stroke = GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)))

        map.setGeometry(stroke, GeometrySlot.Preview)
        assertEquals(SlotGeometry(stroke, "#2b8a3e"), map.geometry(GeometrySlot.Preview))
        assertEquals(listOf("mountaya-preview-casing", "mountaya-preview-line"), layerIds(map))
        assertEquals(
            listOf("mountaya-preview"),
            map.state.value.sources
                .map { it.id },
        )

        map.editGeometry(GeometrySlot.Preview, color = "#123456")
        assertEquals(SlotGeometry(stroke, "#123456"), map.geometry(GeometrySlot.Preview))
        assertEquals(
            JsonPrimitive("#123456"),
            map.state.value.layers
                .last()
                .paint["line-color"],
        )

        map.setGeometry(point, GeometrySlot.Highlight)
        assertEquals(
            listOf(
                "mountaya-preview-casing",
                "mountaya-preview-line",
                "mountaya-highlight-casing",
                "mountaya-highlight-line",
                "mountaya-highlight-point",
            ),
            layerIds(map),
            "the highlight draws at the chrome band, above the preview",
        )
    }

    @Test
    fun aGeometrySlotHoldingNoneDrawsNothingAndAClearGivesTheSlotItsOwnColour() {
        val map = MapEngine(client())
        map.setGeometry(point, GeometrySlot.Preview)
        map.setGeometry(point, GeometrySlot.Highlight, "#654321")

        map.setGeometry(null, GeometrySlot.Preview, "#abcdef")
        assertEquals(SlotGeometry(null, "#abcdef"), map.geometry(GeometrySlot.Preview))
        assertEquals(
            listOf("mountaya-highlight"),
            map.state.value.sources
                .map { it.id },
        )

        map.clearGeometry(GeometrySlot.Highlight)
        assertEquals(SlotGeometry(null, "#f59f00"), map.geometry(GeometrySlot.Highlight))
        assertTrue(
            map.state.value.layers
                .isEmpty(),
        )
        assertTrue(
            map.state.value.sources
                .isEmpty(),
        )
        assertEquals(SlotGeometry(null, "#e9501e"), map.geometry(GeometrySlot.Emphasis))
    }

    @Test
    fun aPickedResultIsDrawnAboveEverythingElseInTheAccentOneAtATime() {
        val map = MapEngine(client())
        map.setGeometry(point, GeometrySlot.Highlight)

        map.setSearchHighlight(SearchHighlight.Place(listOf(6.86, 45.83)))
        assertEquals(
            listOf("mountaya-highlight", "mountaya-search-highlight"),
            map.state.value.sources
                .map { it.id },
        )
        val pin =
            map.state.value.layers
                .last()
        assertEquals("mountaya-search-highlight-place", pin.id)
        assertEquals(LayerRank.Chrome.rank, pin.rank)
        assertEquals(
            JsonPrimitive("poi-icon-point-1c7ed6"),
            pin.layout["icon-image"],
            "the standard accent where none is handed",
        )

        map.setSearchHighlight(
            SearchHighlight.Way(listOf(6.85, 45.82), null, listOf(listOf(6.8, 45.8), listOf(6.9, 45.85))),
            HexColor("#e8590c"),
        )
        assertEquals(
            listOf(
                "mountaya-search-highlight-halo",
                "mountaya-search-highlight-casing",
                "mountaya-search-highlight-line",
                "mountaya-search-highlight-endpoints",
            ),
            layerIds(map).filter { it.startsWith("mountaya-search-highlight") },
            "the latest call replaces the one before",
        )
        assertTrue(map.geometry(GeometrySlot.Highlight).geometry != null, "the geometry slot is left alone")

        map.setSearchHighlight(null)
        assertEquals(
            listOf("mountaya-highlight"),
            map.state.value.sources
                .map { it.id },
        )
        assertTrue(layerIds(map).none { it.startsWith("mountaya-search-highlight") })
    }

    @Test
    fun aPickedResultIsKeptAcrossABaseMapSwitchAndAClosedMapDrawsNone() {
        val map = MapEngine(client(), style = Url("https://tiles.test/a/style"))
        map.setSearchHighlight(SearchHighlight.Poi(listOf(6.86, 45.83), DataSchema.POICategory.Hut))
        val drawn = map.state.value.layers

        map.setStyle(Url("https://tiles.test/b/style"))
        assertEquals(drawn, map.state.value.layers)
        assertEquals(
            listOf("mountaya-search-highlight"),
            map.state.value.sources
                .map { it.id },
        )

        val glyphless =
            MapEngine(
                client(),
                glyphs =
                    assertNotNull(
                        GlyphSet.of(
                            "no-point",
                            GlyphSet.Paint.Stroke,
                            markup =
                                mapOf("map-pin" to "<path d=\"M0 0h1\"/>"),
                        ),
                        "the set with no point is a set",
                    ),
            )
        glyphless.setSearchHighlight(SearchHighlight.Place(listOf(6.86, 45.83)))
        assertEquals(
            JsonPrimitive("poi-icon-map-pin-1c7ed6"),
            glyphless.state.value.layers
                .first()
                .layout["icon-image"],
        )

        map.close()
        map.setSearchHighlight(null)
        assertEquals(drawn, map.state.value.layers, "a closed map is inert")
        val closed = MapEngine(client())
        closed.close()
        closed.setSearchHighlight(SearchHighlight.Place(listOf(6.86, 45.83)))
        assertTrue(
            closed.state.value.layers
                .isEmpty(),
        )
    }

    /**
     * Answers the Data API: a query sent with its persisted digest alone is refused, as the protocol asks, and one
     * sending its full document is recorded and answered, or held until it is cancelled. Isochrones answer a band
     * per value of the query's `range`, suggestions three candidates. A held query fails with the cancellation of
     * the coroutine that sent it, which it records.
     */
    private class Queries(
        private val holding: Boolean = false,
    ) : HttpExchange {
        val kinds = mutableListOf<String>()
        val inputs = mutableListOf<JsonObject>()
        val cancelled = mutableListOf<Int>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val body = request.body?.decodeToString()?.let { Json.parseToJsonElement(it) as? JsonObject }
            val document =
                (body?.get("query") as? JsonPrimitive)?.content
                    ?: return answer(
                        """{"errors": [{"message": "PersistedQueryNotFound", "extensions": {"code": "PERSISTED_QUERY_NOT_FOUND"}}]}""",
                    )
            val words = document.split(Regex("[^A-Za-z]+")).filter { it.isNotEmpty() }
            val kind = words.getOrNull(words.indexOf("query") + 1)?.lowercase().orEmpty()
            val input = (body["variables"] as? JsonObject)?.get("input") as? JsonObject ?: JsonObject(emptyMap())
            val index = kinds.size
            kinds += kind
            inputs += input
            val ranges = (input["range"] as? JsonArray).orEmpty().map { it.jsonPrimitive.content }
            if (!holding) return answer(answered(kind, ranges))
            try {
                awaitCancellation()
            } finally {
                cancelled += index
            }
        }

        private companion object {
            const val ROUTE =
                """{"distance": 1000, "duration": 600, "geometry": {"coordinates": [[6.86, 45.83], [6.9, 45.9]], "type": "LineString"}}"""

            fun answered(
                kind: String,
                ranges: List<String>,
            ): String =
                if (kind == "isochrones") {
                    val bands =
                        ranges.map {
                            """{"center": [6.86, 45.83], "geometry": {"coordinates": [[[6.85, 45.82], [6.87, 45.82], [6.87, 45.84], [6.85, 45.82]]], "type": "Polygon"}, "value": $it}"""
                        }
                    """{"data": {"isochrones": {"isochrones": [${bands.joinToString(",")}]}}}"""
                } else {
                    """{"data": {"suggestions": {"routes": [$ROUTE, $ROUTE, $ROUTE]}}}"""
                }

            fun answer(text: String): ExchangeResponse =
                ExchangeResponse(
                    200,
                    mapOf("Content-Type" to "application/json"),
                    ExchangeBody(text.encodeToByteArray()),
                )
        }
    }

    /** What each feature the result [id] draws is marked with under [property]. */
    private fun marks(
        map: MapEngine,
        id: String,
        property: String,
    ): List<JsonPrimitive?> {
        val drawn =
            map.state.value.sources
                .firstOrNull { it.id == "mountaya-$id" }
                ?.kind as? MapSource.Kind.GeoJson
        return drawn
            ?.features
            ?.features
            .orEmpty()
            .map { it.properties[property] as? JsonPrimitive }
    }

    private val suggestionsInput =
        DataSchema.SuggestionsInput(DataSchema.ActivitySlug.HikingAndTrail, 3, 10_000.0, listOf(6.86, 45.83))

    private val isochronesInput =
        DataSchema.IsochronesInput(
            DataSchema.ActivitySlug.HikingAndTrail,
            listOf(listOf(6.86, 45.83)),
            listOf(600.0, 1_200.0),
            DataSchema.RangeType.Time,
        )

    private fun queried(exchange: HttpExchange): MapEngine =
        MapEngine(
            Mountaya(
                Credential.Publishable("pk_test_a", SessionToken.Value("sess_a")),
                exchange = exchange,
                dispatcher = Dispatchers.Unconfined,
            ),
        )

    @Test
    fun aResultHandedOverIsDrawnUnderAnIdOfItsOwnRemovedByItAndClearedByKind() =
        runTest {
            val map = MapEngine(client())
            val band =
                DataSchema.Isochrone {
                    value = 600.0
                    geometry =
                        GeoJsonPolygon(
                            listOf(
                                listOf(
                                    listOf(6.85, 45.82),
                                    listOf(6.87, 45.82),
                                    listOf(6.87, 45.84),
                                    listOf(6.85, 45.82),
                                ),
                            ),
                        )
                }
            val reach = DataSchema.IsochronesResult { isochrones = listOf(band) }
            val found =
                DataSchema.GeocodeResult {
                    places =
                        listOf(DataSchema.Place { point = listOf(6.86, 45.83) }, DataSchema.Place { name = "Nowhere" })
                }

            val first = map.addIsochrones(reach)
            val second = map.addIsochrones(reach)
            val places = map.addPlaces(found)

            assertEquals("analysis:isochrones:0", first.id)
            assertEquals("analysis:isochrones:1", second.id)
            assertEquals("analysis:places:2", places.id)
            assertEquals(
                listOf<Double?>(0.0),
                marks(map, places.id, "mtyaIndex").map { it?.doubleOrNull },
                "a place that names no point draws nothing",
            )
            assertEquals(
                listOf(40, 40),
                map.state.value.layers
                    .filter { it.source == "mountaya-${first.id}" }
                    .map { it.rank },
            )
            map.removeAnalysis(first.id)
            assertEquals(
                listOf("mountaya-${second.id}", "mountaya-${places.id}"),
                map.state.value.sources
                    .map { it.id },
            )
            map.clearIsochrones()
            assertEquals(
                listOf("mountaya-${places.id}"),
                map.state.value.sources
                    .map { it.id },
            )
            map.setPointsOfInterest(DataSchema.PoisResult { pois = emptyList() })
            assertEquals(
                "mountaya-analysis:pois",
                map.state.value.sources
                    .last()
                    .id,
            )
            map.clearPointsOfInterest()
            map.clearPlaces()
            assertTrue(
                map.state.value.sources
                    .isEmpty(),
            )
            assertTrue(
                map.state.value.layers
                    .isEmpty(),
            )
        }

    @Test
    fun anEditAsksAgainWithItsPatchAndARestyleRedrawsTheResultHeldAskingNothing() =
        runTest {
            val exchange = Queries()
            val map = queried(exchange)
            val suggested = map.addSuggestions(suggestionsInput)
            assertEquals("analysis:suggestions:0", suggested.id)
            assertEquals(
                listOf<Boolean?>(true, false, false),
                marks(map, suggested.id, "chosen").map { it?.booleanOrNull },
            )

            map.editSuggestions(suggested.id, chosen = 2)
            assertEquals(listOf("suggestions"), exchange.kinds, "a restyle asks nothing")
            assertEquals(
                listOf<Boolean?>(false, false, true),
                marks(map, suggested.id, "chosen").map { it?.booleanOrNull },
            )

            map.editSuggestions(
                suggested.id,
                patch = { DataSchema.SuggestionsInput(it.activity, it.candidates, 20_000.0, it.start) },
            )
            assertEquals(listOf("suggestions", "suggestions"), exchange.kinds)
            assertEquals(
                20_000.0,
                exchange.inputs
                    .last()["distance"]
                    ?.jsonPrimitive
                    ?.doubleOrNull,
            )
            assertEquals(
                listOf<Boolean?>(false, false, true),
                marks(map, suggested.id, "chosen").map { it?.booleanOrNull },
                "the options are kept",
            )

            val reach = map.addIsochrones(isochronesInput)
            assertEquals(listOf<Double?>(600.0, 1_200.0), marks(map, reach.id, "value").map { it?.doubleOrNull })
            map.editIsochrones(
                reach.id,
                patch = { DataSchema.IsochronesInput(it.activity, it.locations, listOf(300.0), it.rangeType) },
            )
            assertEquals(listOf<Double?>(300.0), marks(map, reach.id, "value").map { it?.doubleOrNull })
        }

    @Test
    fun anEditOfNoResultOfAnotherKindsOrAskingAgainOfOneHandedOverIsRefused() =
        runTest {
            val map = MapEngine(client())
            val handed = map.addSuggestions(DataSchema.SuggestionsResult { routes = emptyList() })

            assertEquals(
                MapException(MapException.Kind.KindMismatch, handed.id),
                assertFailsWith<MapException> { map.editIsochrones(handed.id) },
            )
            assertEquals(
                MapException(MapException.Kind.AnalysisNotFound, "analysis:places:9"),
                assertFailsWith<MapException> { map.editPlaces("analysis:places:9") },
            )
            assertEquals(
                MapException(MapException.Kind.NoInput, handed.id),
                assertFailsWith<MapException> {
                    map.editSuggestions(
                        handed.id,
                        patch = { DataSchema.SuggestionsInput(it.activity, it.candidates, 1.0, it.start) },
                    )
                },
            )
            map.editSuggestions(handed.id, chosen = 1)
        }

    @Test
    fun aRemovalOrAClearCancelsTheQueryItsResultHasOutAndTheCallIsCancelled() =
        runTest {
            val exchange = Queries(holding = true)
            val map = queried(exchange)

            val first = async { map.addIsochrones(isochronesInput) }
            runCurrent()
            assertEquals(1, exchange.kinds.size, "the query is out")
            map.removeAnalysis("analysis:isochrones:0")
            assertTrue(runCatching { first.await() }.exceptionOrNull() is CancellationException)
            assertEquals(listOf(0), exchange.cancelled)

            val second = async { map.addIsochrones(isochronesInput) }
            runCurrent()
            assertEquals(2, exchange.kinds.size, "the query is out")
            map.clearIsochrones()
            assertTrue(runCatching { second.await() }.exceptionOrNull() is CancellationException)
            assertEquals(listOf(0, 1), exchange.cancelled)
            assertTrue(
                map.state.value.sources
                    .isEmpty(),
            )
        }

    @Test
    fun aClosedMapStillAnswersAResultAndDrawsAndHoldsNothing() =
        runTest {
            val map = MapEngine(client())
            map.close()

            val handle = map.addPlaces(DataSchema.GeocodeResult { places = emptyList() })

            assertEquals("analysis:places:0", handle.id)
            assertEquals(
                MapException(MapException.Kind.AnalysisNotFound, handle.id),
                assertFailsWith<MapException> { map.editPlaces(handle.id) },
            )
            map.setGeometry(point, GeometrySlot.Preview)
            assertNull(map.geometry(GeometrySlot.Preview).geometry)
        }

    // The controls: a layers selection and a locate control stand over a map, which answers what they read.

    /** Answers [fix] and reads its permission as [permission], counting the fixes asked for. */
    private class Standing(
        private val fix: GeolocationFix,
        private val permission: GeolocationPermission,
    ) : Geolocation {
        var asked = 0
            private set

        override suspend fun locate(): GeolocationFix {
            asked += 1
            return fix
        }

        override suspend fun permission(): GeolocationPermission = permission
    }

    @Test
    fun aMapAnswersTheTerrainItShowsAndWhetherItsStandingPermissionRefusesAFix() =
        runTest {
            val (map, _) = drawn()
            assertNull(map.terrain)
            map.setTerrain()
            assertEquals("outdoor", map.terrain)
            assertEquals(map.state.value.terrain, map.terrain)

            for ((permission, refused) in listOf(
                GeolocationPermission.Denied to true,
                GeolocationPermission.Granted to false,
                GeolocationPermission.Prompt to false,
                GeolocationPermission.Unknown to false,
            )) {
                val (standing, _) =
                    drawn(
                        geolocation = Standing(GeolocationFix.Refused(GeolocationRefusal.Denied), permission),
                    )
                assertEquals(refused, standing.isLocationRefused(), permission.id)
            }
            map.close()
            assertTrue(map.isLocationRefused(), "a closed map still reads its geolocation, an inert one")
        }

    @Test
    fun aLayersSelectionDrivesTheMapOneClassicOverlayStackedEnrichmentsAndRefusals() =
        runTest {
            val (map, _) = drawn(exchange = Contents())
            val selection = LayersSelectionEngine(backgroundScope, map)
            runCurrent()
            assertEquals(map.layersManifest, selection.state.value.catalogue)
            assertNull(selection.state.value.terrain, "the catalogue holds no terrain the map shows")

            selection.setOverlay("slope")
            selection.toggleEnrichment("contours")
            runCurrent()
            assertEquals(listOf("contours"), map.enrichments)
            assertTrue("slope-layer" in layerIds(map))
            selection.setOverlay("slope")
            runCurrent()
            assertFalse("slope-layer" in layerIds(map), "a second press clears the overlay")

            selection.setOverlay("snow")
            runCurrent()
            assertEquals("snow", selection.state.value.overlay, "a refused command keeps what was asked")
            assertEquals<Throwable?>(
                MapException(MapException.Kind.OverlayNotFound, "snow"),
                selection.state.value.refusal,
            )
            selection.toggleEnrichment("contours")
            runCurrent()
            assertNull(selection.state.value.refusal, "a later command that settles ends the refusal")
            assertTrue(map.enrichments.isEmpty())
            selection.close()
        }

    @Test
    fun aLocateControlStandsOverTheMapBusyUntilItsFixSettlesBlockedOnItsWord() =
        runTest {
            val (prompting, _) =
                drawn(
                    geolocation =
                        Standing(
                            GeolocationFix.Refused(GeolocationRefusal.Denied),
                            GeolocationPermission.Prompt,
                        ),
                )
            val control = GeolocateControlEngine(backgroundScope, prompting)
            val failures = mutableListOf<Throwable>()
            runCurrent()

            control.locate(onError = { failures += it })
            assertEquals(GeolocateControlState(isBusy = true, isBlocked = false, isLive = true), control.state.value)
            runCurrent()
            assertEquals(GeolocateControlState(isBusy = false, isBlocked = false, isLive = true), control.state.value)
            assertEquals<List<Throwable>>(
                listOf(MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.Denied)),
                failures,
            )

            val (refusing, _) =
                drawn(
                    geolocation =
                        Standing(
                            GeolocationFix.Refused(GeolocationRefusal.Denied),
                            GeolocationPermission.Denied,
                        ),
                )
            control.configure(refusing)
            runCurrent()
            assertEquals(GeolocateControlState(isBusy = false, isBlocked = true, isLive = true), control.state.value)
            control.close()
        }
}
