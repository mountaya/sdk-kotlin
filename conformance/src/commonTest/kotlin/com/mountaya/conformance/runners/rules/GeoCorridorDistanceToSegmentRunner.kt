package com.mountaya.conformance

import com.mountaya.geo.distanceToSegment
import kotlinx.coroutines.test.TestScope

/**
 * `geo.corridor.distance-to-segment`: the planar distance from the case's `point` to the segment
 * from `start` to `end`, in the degrees the cases answer in; the description's meters are a gap the
 * contract has yet to close, and the measure in meters is `geo.corridor.meters-to-route`.
 */
object GeoCorridorDistanceToSegmentRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val distance =
            distanceToSegment(
                fixturePosition(input["point"], "point"),
                fixturePosition(input["start"], "start"),
                fixturePosition(input["end"], "end"),
            )
        return ConformanceOutcome.Value(FixtureValue.Number(distance))
    }
}
