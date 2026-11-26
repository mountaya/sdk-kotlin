package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.ZoomBounds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScriptedMapSurfaceTest {
    private fun engine(): MapEngine =
        MapEngine(Mountaya(Credential.Publishable("pk_test_a")), geolocation = InertGeolocation())

    private val point = GeoJsonPoint(listOf(6.86, 45.83))

    @Test
    fun itRecordsEachMoveAndFitTheEngineHandsItAndLandsAFitAsCameraForBoundsDoes() {
        val map = engine()
        val surface = ScriptedMapSurface(map, canvas = listOf(800.0, 600.0))
        map.ease(CameraTarget(zoom = 12.0, pitch = 20.0))
        assertEquals(
            listOf(ScriptedMapSurface.Move(CameraTarget(zoom = 12.0, pitch = 20.0), CameraMotion.Ease(null))),
            surface.moves,
        )

        map.fitBounds(listOf(6.8, 45.8, 6.95, 45.9), CameraFit(padding = PixelPadding(48.0), maxZoom = 15.0))
        assertEquals(listOf(listOf(6.8, 45.8, 6.95, 45.9)), surface.frames.map { it.bounds })
        assertTrue(abs(map.state.value.camera.zoom - 11.269337026503) < 1e-9, "${map.state.value.camera}")
        assertEquals(20.0, map.state.value.camera.pitch, "a fit keeps the pitch")
        assertEquals(0.0, map.state.value.camera.bearing, "a fit faces north")
        assertEquals(ZoomBounds(0.0, 25.5), surface.zoomLimits)
    }

    @Test
    fun itAnswersAQueryWithTheFeaturesTheLayersAskedAboutDrewAsTheTestStatesThem() {
        val map = engine()
        val surface = ScriptedMapSurface(map)
        map.addLayer(MapLayer("line", TileSchema.TileLayerType.Line, "s", rank = 50))
        surface.rendered =
            listOf(
                QueriedFeature(mapOf("a" to JsonPrimitive(1)), point, "line"),
                QueriedFeature(emptyMap(), point, "base-road"),
            )

        assertEquals(listOf("line"), map.featuresAt(listOf(1.0, 2.0)).map { it.layer })
        assertEquals(listOf(listOf("line")), surface.queries)
        surface.liveLayers = emptyList()
        assertTrue(map.featuresAt(listOf(1.0, 2.0)).isEmpty())
        assertEquals(listOf(listOf("line")), surface.queries, "nothing left to ask, nothing asked")
        assertEquals(listOf(3.0, 4.0), map.project(listOf(3.0, 4.0)), "a position read as its point")
    }

    @Test
    fun itPlaysWhatHappensOnACanvasWhichTheEngineTells() =
        runTest {
            val map = engine()
            val surface = ScriptedMapSurface(map)
            surface.baseLabels = listOf(ScriptedMapSurface.Label("place-label", JsonPrimitive("{name}")))
            val told = mutableListOf<MapEvent>()
            val reading = launch(UnconfinedTestDispatcher(testScheduler)) { map.events().collect { told += it } }
            surface.click(listOf(1.0, 2.0), listOf(190.0, 45.0))
            surface.longClick(listOf(1.0, 2.0), listOf(6.0, 45.0))
            surface.hover(listOf(1.0, 2.0), listOf(6.0, 45.0))
            surface.drawFrame()
            surface.leave(listOf(1.0, 2.0), listOf(6.0, 45.0))
            surface.failRequest("https://tiles.test/a", 403, null)
            surface.loadTile("any")
            surface.loadStyle()
            map.close()
            reading.join()

            assertEquals<List<MapEvent>>(
                listOf(
                    MapEvent.Click(MapTap(listOf(-170.0, 45.0), listOf(1.0, 2.0))),
                    MapEvent.LongClick(MapTap(listOf(6.0, 45.0), listOf(1.0, 2.0))),
                    MapEvent.FeatureHover(listOf(6.0, 45.0), emptyList()),
                    MapEvent.FeatureHover(listOf(6.0, 45.0), emptyList()),
                    MapEvent.AccessRefused(403, "https://tiles.test/a"),
                    MapEvent.AccessRestored,
                ),
                told,
            )
            assertEquals(listOf("place-label"), surface.relabelled)
        }

    @Test
    fun itPlaysTheDocumentOfTheBaseMapAskedForNotArrivingWhichGivesTheMapBack() {
        val map = engine()
        val surface = ScriptedMapSurface(map)
        val shown = Url("https://tiles.test/shown.json")
        val asked = Url("https://tiles.test/asked.json")
        map.setStyle(shown)
        surface.loadStyle()
        map.setStyle(asked)
        surface.failStyle()
        assertEquals(shown, map.state.value.style)
        map.setStyle(asked)
        surface.loadStyle()
        surface.failStyle()
        assertEquals(asked, map.state.value.style, "a style that loaded is the one shown")
    }

    /** The base map's sources are the test's to state, and the map credits each drawn one as a renderer reads it. */
    @Test
    fun itReportsTheBaseMapsSourcesTheTestStatesWhoseCreditsTheMapShows() {
        val map = engine()
        val surface = ScriptedMapSurface(map)
        val alone = map.credits
        surface.baseSources =
            listOf(
                ScriptedMapSurface.BaseSource("&copy; OpenStreetMap contributors", isDrawn = true),
                ScriptedMapSurface.BaseSource("Imagery &copy; Satellite", isDrawn = false),
                ScriptedMapSurface.BaseSource(null, isDrawn = true),
            )

        assertEquals(alone + "&copy; OpenStreetMap contributors", map.credits)
        assertEquals(ScriptedMapSurface.BaseSource(null, true), ScriptedMapSurface.BaseSource(null, true))
    }
}
