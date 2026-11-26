package com.mountaya.conformance

import com.mountaya.elements.GeolocationFix
import com.mountaya.elements.GeolocationRefusal
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.elements.locateFixWait
import com.mountaya.elements.locateMaxFixAge
import com.mountaya.geo.LOCATE_MAX_ZOOM
import kotlinx.coroutines.test.TestScope

/**
 * `map.camera.locate`: what locating does, through the engine's `locate` over a geolocation of the runner's
 * own, answering the case's `fix` and recording whether it was asked, and the scripted surface, which records
 * the box the camera is asked to frame and the fit's ceiling. A map the case closes is closed before it
 * locates. The ceiling is held to the published `LOCATE_MAX_ZOOM`, and the fixture's `maxFixAgeMs` and
 * `fixWaitMs` to the device geolocation's own, `locateMaxFixAge` and `locateFixWait`.
 */
object MapCameraLocateRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules =
                mapOf(
                    "fixWaitMs" to locateFixWait.inWholeMilliseconds.toDouble(),
                    "maxFixAgeMs" to locateMaxFixAge.inWholeMilliseconds.toDouble(),
                    "maxZoom" to LOCATE_MAX_ZOOM,
                ),
        )
        val input = case.fields
        val written = input.objectOrNull("fix") ?: throw AssertionError("the input states no `fix`")
        val fix =
            if (written.bool("ok")) {
                GeolocationFix.Located(
                    fixturePosition(written["position"], "position"),
                    written.number("accuracyMeters"),
                )
            } else {
                val reason = written.text("reason")
                GeolocationFix.Refused(
                    GeolocationRefusal.entries.firstOrNull { it.id == reason }
                        ?: throw AssertionError("`$reason` is no reason a geolocation refuses with"),
                )
            }
        val geolocation = MapFixtures.Fix(fix)
        val map = MapEngine(MapFixtures.client(), geolocation = geolocation)
        val surface = ScriptedMapSurface(map)
        if (input.bool("closed")) map.close()

        var refused: FixtureValue = FixtureValue.Null
        var reason: FixtureValue = FixtureValue.Null
        try {
            map.locate()
        } catch (refusal: MapException) {
            refused = FixtureValue.Text(MapFixtures.kind(refusal))
            refusal.reason?.let { reason = FixtureValue.Text(it.id) }
        }
        val framed =
            surface.frames.lastOrNull()?.let { frame ->
                MapFixtures.objectOf(
                    "bounds" to MapFixtures.numbers(frame.bounds),
                    "maxZoom" to (frame.fit.maxZoom?.let { FixtureValue.Number(it) } ?: FixtureValue.Null),
                )
            } ?: FixtureValue.Null
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "asked" to FixtureValue.Bool(geolocation.asked),
                "framed" to framed,
                "reason" to reason,
                "refused" to refused,
            ),
        )
    }
}
