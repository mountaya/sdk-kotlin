package com.mountaya.conformance

import com.mountaya.geo.MIN_RANGE_SPAN_METERS
import com.mountaya.geo.clampRange
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.window-clamp`: the window a profile draws for the case's `window` over an itinerary of its
 * `totalDistance`. The narrowest window is held to the published constant.
 */
object GeoProfileWindowClampRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("minSpanMeters" to MIN_RANGE_SPAN_METERS))
        val input = case.fields
        val window = fixtureWindow(input["window"]) ?: throw AssertionError("the case states no `window`")
        val drawn = clampRange(window, input.number("totalDistance"))
        return ConformanceOutcome.Value(
            objectValue(
                "from" to FixtureValue.Number(drawn.from),
                "to" to FixtureValue.Number(drawn.to),
            ),
        )
    }
}
