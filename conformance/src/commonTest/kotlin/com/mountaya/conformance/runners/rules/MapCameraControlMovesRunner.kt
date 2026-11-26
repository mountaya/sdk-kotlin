package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.elements.ZoomLimit
import com.mountaya.geo.CameraPose
import com.mountaya.geo.ZoomBounds
import kotlinx.coroutines.test.TestScope

/**
 * `map.camera.control-moves`: where a map control moves the camera, through the engine's `zoomIn`, `zoomOut`
 * and `resetNorth`, on the scripted surface playing a renderer whose floor lets the case's window be held,
 * MapLibre GL JS's -2: the camera the case states, held to its window, moved, and the camera the state then
 * holds. The renderer is handed only the members that move.
 */
object MapCameraControlMovesRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val camera = input.objectOrNull("camera") ?: throw AssertionError("the input states no `camera`")
        val pose =
            CameraPose(
                fixtureNumbers(camera.items("center")),
                camera.number("zoom"),
                camera.number("pitch"),
                camera.number("bearing"),
            )
        val window = MapFixtures.window(input, "window")
        val map = MapEngine(MapFixtures.client(), camera = pose, geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map, zoomLimits = ZoomBounds(-2.0, 22.0))
        map.setZoomBounds(ZoomLimit.Zoom(window.minZoom), ZoomLimit.Zoom(window.maxZoom))
        val handed =
            when (val move = input.text("move")) {
                "zoom-in" -> {
                    map.zoomIn()
                    setOf("zoom")
                }

                "zoom-out" -> {
                    map.zoomOut()
                    setOf("zoom")
                }

                "reset-north" -> {
                    map.resetNorth()
                    setOf("bearing", "pitch")
                }

                else -> {
                    throw AssertionError("no control moves `$move`")
                }
            }
        val moved = surface.moves.lastOrNull()?.target ?: throw AssertionError("the renderer was handed no move")
        val members = (MapFixtures.target(moved) as? FixtureValue.Object)?.keys.orEmpty().toSet()
        if (members != handed) throw AssertionError("the renderer was handed ${surface.moves}")
        return ConformanceOutcome.Value(MapFixtures.camera(map.state.value.camera))
    }

    /** The numbers [items] holds, failing with a sentence on any other value. */
    private fun fixtureNumbers(items: List<FixtureValue>): List<Double> =
        items.map {
            (it as? FixtureValue.Number)?.value
                ?: throw AssertionError("`center` holds a value that is no number")
        }
}
