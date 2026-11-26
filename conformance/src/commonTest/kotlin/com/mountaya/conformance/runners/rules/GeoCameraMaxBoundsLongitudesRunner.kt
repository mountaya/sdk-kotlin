package com.mountaya.conformance

import com.mountaya.geo.maxBoundsLongitudes
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.max-bounds-longitudes`: the `[west, east]` edges a pan lock with the case's `west` and
 * `east` is handed to a renderer with. The turn is held to the one the rule brings a lock back by,
 * and the half span of the whole-world lock to that lock's east edge around the prime meridian.
 */
object GeoCameraMaxBoundsLongitudesRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "degreesPerTurn" to 365.0 - maxBoundsLongitudes(365.0, 367.0)[0],
                "worldLockHalfSpanDegrees" to maxBoundsLongitudes(-180.0, 180.0)[1],
            ),
        )
        val input = case.fields
        val edges = maxBoundsLongitudes(input.number("west"), input.number("east"))
        return ConformanceOutcome.Value(FixtureValue.Array(edges.map { FixtureValue.Number(it) }))
    }
}
