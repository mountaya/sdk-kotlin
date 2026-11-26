package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.accuracy-bounds`: the box a device fix at the case's `center` with an accuracy of
 * `radiusMeters` is framed by. The fixture's circumference is held to the sphere Geo publishes and its
 * ceiling to the zoom a fix is framed at at most, both published, since a map's locate frames a fix by
 * them. The registry leaves the entry to the module's own tests (`api: false`).
 */
object GeoCameraAccuracyBoundsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(
            case,
            mapOf(
                "circumferenceMeters" to FixtureValue.Number(WEB_MERCATOR_CIRCUMFERENCE_METERS),
                "maxZoom" to FixtureValue.Number(LOCATE_MAX_ZOOM),
            ),
        )
        val input = case.fields
        val box = accuracyBounds(fixturePosition(input["center"], "center"), input.number("radiusMeters"))
        return ConformanceOutcome.Value(FixtureValue.Array(box.map { FixtureValue.Number(it) }))
    }
}
