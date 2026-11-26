package com.mountaya.conformance

import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.CameraFit
import com.mountaya.elements.CameraTarget
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapSource
import com.mountaya.elements.QueriedFeature
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.elements.ZoomLimit
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.LayerRank
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * `map.engine.closed`: what a closed map does, through the engine on the scripted surface: before it closes it
 * draws an itinerary's line under the point `[1, 2]`, holds its zoom window to 8 to 12, labels in French, hears
 * a refusal of access to the itinerary's source and is collected for its events; then the case's call is made
 * on the closed map, or its gesture played on the surface. Kotlin's names for the calls are the engine's: `jump`
 * for `jumpTo`, `zoomIn`, `setZoomBounds` for `setConstraints`, `setItinerary`, `featuresAt`, `project`, `unproject`, the state's zoom bounds, label language and standing refusal for the
 * three getters, `elevationAt` and `close`. `changed` is a camera move or a fit the surface was handed, or a
 * state the engine reported; `read` a request its client sent; `told` an event a flow collected after the close
 * is told, the one collected before it having ended there.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapEngineClosedRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val call = input.textOrNull("call")
        val gesture = input.textOrNull("renderer")
        val args = (input["args"] as? FixtureValue.Array)?.items.orEmpty()
        val exchange =
            MapFixtures.Recording(
                MapFixtures.Recording.Answer.Data(
                    "terrainSamples",
                    MapFixtures.objectOf(
                        "samples" to
                            FixtureValue.Array(
                                listOf(MapFixtures.objectOf("elevation" to FixtureValue.Number(1834.0))),
                            ),
                    ),
                ),
            )
        val map = MapEngine(MapFixtures.client(exchange = exchange), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        map.addSource(MapSource("itinerary", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
        map.addLayer(
            MapLayer(
                "itinerary-line",
                TileSchema.TileLayerType.Line,
                "itinerary",
                rank = LayerRank.ItineraryLine.rank,
            ),
        )
        surface.rendered = listOf(QueriedFeature(emptyMap(), GeoJsonPoint(listOf(6.86, 45.83)), "itinerary-line"))
        map.setZoomBounds(ZoomLimit.Zoom(8.0), ZoomLimit.Zoom(12.0))
        map.setLabelLanguage(RestSchema.Language.Fr)
        surface.failRequest("https://tiles.test/x", 402, "itinerary")
        val before = mutableListOf<MapEvent>()
        val listening =
            scope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
                map.events().collect {
                    before +=
                        it
                }
            }
        map.close()
        val ended = map.state.value
        val moves = surface.moves.size
        val frames = surface.frames.size
        val sentBefore = exchange.sent.size

        var answer: FixtureValue = FixtureValue.Null
        when {
            gesture != null -> {
                if (gesture != "click") throw AssertionError("no gesture is called `$gesture`")
                surface.click(listOf(1.0, 2.0), listOf(6.86, 45.83))
            }

            call != null -> {
                answer = call(call, args, map)
            }

            else -> {
                throw AssertionError("the input states neither a call nor a gesture")
            }
        }
        val read = exchange.sent.size > sentBefore
        val changed = map.state.value != ended || surface.moves.size != moves || surface.frames.size != frames
        val told = mutableListOf<MapEvent>()
        map.events().collect { told += it }
        // The flow collected before the close holds only what was told before it, and has ended.
        listening.join()
        if (before.isNotEmpty()) throw AssertionError("a flow collected before the close was told $before")
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "answer" to answer,
                "changed" to FixtureValue.Bool(changed),
                "read" to FixtureValue.Bool(read),
                "told" to textArray(told.map { it.toString() }),
            ),
        )
    }

    /** Makes the closed map's [call] with its [args], and answers what it answers as the fixtures write it. */
    private suspend fun call(
        call: String,
        args: List<FixtureValue>,
        map: MapEngine,
    ): FixtureValue {
        fun position(): List<Double> =
            fixturePosition(args.firstOrNull() ?: throw AssertionError("`$call` is given no argument"), call)
        return when (call) {
            "jumpTo" -> {
                val zoom = (args.firstOrNull() as? FixtureValue.Object)?.numberOrNull("zoom")
                map.jump(CameraTarget(zoom = zoom))
                FixtureValue.Null
            }

            "zoomIn" -> {
                map.zoomIn()
                FixtureValue.Null
            }

            "setConstraints" -> {
                val window =
                    (args.firstOrNull() as? FixtureValue.Object)?.objectOrNull("zoom")
                        ?: throw AssertionError("`setConstraints` states no zoom window")
                map.setZoomBounds(ZoomLimit.Zoom(window.number("min")), ZoomLimit.Zoom(window.number("max")))
                FixtureValue.Null
            }

            "setItinerary" -> {
                val id =
                    (args.firstOrNull() as? FixtureValue.Text)?.value
                        ?: throw AssertionError("`setItinerary` is given no id")
                MapFixtures.objectOf("drawn" to FixtureValue.Bool(map.setItinerary(id)))
            }

            "featuresAt" -> {
                FixtureValue.Array(map.featuresAt(position()).map(MapFixtures::answer))
            }

            "project" -> {
                MapFixtures.numbers(map.project(position()))
            }

            "unproject" -> {
                MapFixtures.numbers(map.unproject(position()))
            }

            "getZoomLimits" -> {
                MapFixtures.windowAnswer(map.state.value.zoomBounds)
            }

            "getLanguage" -> {
                FixtureValue.Text(map.state.value.labelLanguage.value)
            }

            "isAccessRequired" -> {
                FixtureValue.Bool(map.state.value.isAccessRequired)
            }

            "elevationAt" -> {
                map.elevationAt(position())?.let { FixtureValue.Number(it) } ?: FixtureValue.Null
            }

            "destroy" -> {
                map.close()
                FixtureValue.Null
            }

            else -> {
                throw AssertionError("no call is called `$call`")
            }
        }
    }
}
