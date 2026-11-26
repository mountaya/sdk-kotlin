package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.elements.ZoomLimit
import com.mountaya.geo.ZoomBounds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * `map.camera.zoom-window`: how a map's zoom window moves, call by call, through the engine's
 * `setZoomBounds(min, max)` on the scripted surface playing the renderer the case names, its `floor` and
 * `ceiling`, the window `held` set first: a limit left out is kept, one written null released, one a number
 * moved to it, and one written as text is refused by the type before any call. A call is told where it moved
 * the state, which the engine's state flow then reports, and the flow is held to one state for each such call.
 * MapLibre Native's limits are held to the engine's own on no surface, the window a map opens with and the one
 * a release goes back to; MapLibre GL JS's are what the cases that name them play on.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapCameraZoomWindowRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val native = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val opened = native.state.value.zoomBounds
        native.setZoomBounds(ZoomLimit.Zoom(4.0), ZoomLimit.Zoom(6.0))
        native.setZoomBounds(ZoomLimit.Released, ZoomLimit.Released)
        val released = native.state.value.zoomBounds
        if (opened != released) throw AssertionError("a map opens on $opened, and releases to $released")
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "maplibreNative" to
                        MapFixtures.objectOf(
                            "ceiling" to FixtureValue.Number(released.maxZoom),
                            "floor" to FixtureValue.Number(released.minZoom),
                        ),
                ),
            inputs = setOf("maplibreGlJs"),
        )

        val input = case.fields
        val renderer = ZoomBounds(input.number("floor"), input.number("ceiling"))
        val held = MapFixtures.window(input, "held")
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        ScriptedMapSurface(map, zoomLimits = renderer)
        map.setZoomBounds(ZoomLimit.Zoom(held.minZoom), ZoomLimit.Zoom(held.maxZoom))
        var reported = -1
        val reading =
            scope.launch(
                UnconfinedTestDispatcher(scope.testScheduler),
            ) { map.state.collect { reported += 1 } }

        var told = 0
        val answers =
            input.items("calls").map { written ->
                val call = written as? FixtureValue.Object ?: throw AssertionError("a call is not an object")
                val before = map.state.value
                val min = MapFixtures.limit(call["min"])
                val max = MapFixtures.limit(call["max"])
                val refused =
                    if (min == null || max == null) {
                        "number-refused"
                    } else {
                        try {
                            map.setZoomBounds(min, max)
                            null
                        } catch (refusal: MapException) {
                            MapFixtures.kind(refusal)
                        }
                    }
                val moved = map.state.value != before
                if (moved) told += 1
                MapFixtures.objectOf(
                    "refused" to (refused?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    "told" to FixtureValue.Bool(moved),
                    "window" to MapFixtures.windowAnswer(map.state.value.zoomBounds),
                )
            }
        map.close()
        reading.cancel()
        if (reported != told) throw AssertionError("$told calls moved the window, and $reported states were told")
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }
}
