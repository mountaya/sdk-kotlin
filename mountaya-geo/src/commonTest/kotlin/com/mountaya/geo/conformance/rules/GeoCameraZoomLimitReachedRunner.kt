package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.zoom-limit-reached`: which end of its zoom `window`, `{ max, min }`, a camera at the case's
 * `zoom` stands at, spelled as the fixture spells it. The controls each end disables are held to
 * `zoomControlsDisabled`'s, each spelled as `ZoomStep` spells it. The rule is what a zoom control reads,
 * which a consumer only observes, so its entry runs from Geo's own tests.
 */
object GeoCameraZoomLimitReachedRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(
            case,
            mapOf(
                "disabledControls" to
                    FixtureValue.Object(
                        ZoomWindowEnd.entries.map { end ->
                            end.id to FixtureValue.Array(zoomControlsDisabled(end).map { FixtureValue.Text(it.id) })
                        },
                    ),
            ),
        )
        val input = case.fields
        val window = input.objectOrNull("window") ?: throw AssertionError("the input states no `window`")
        val reached = zoomLimitReached(input.number("zoom"), ZoomBounds(window.number("min"), window.number("max")))
        return ConformanceOutcome.Value(FixtureValue.Text(reached.id))
    }
}
