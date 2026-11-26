package com.mountaya.conformance

import com.mountaya.geo.itinerarySegments
import kotlinx.coroutines.test.TestScope

/**
 * `geo.data.itinerary-segments`: the legs of the case's `segments` with its `groups` collapsed. Each
 * segment and group is built as the REST API's, from the members the fixture writes and a placeholder for
 * every other the type requires. A leg states no running total and no difficulty, so the runner writes the
 * `distance`, `ascent`, `descent` and `difficulty` of its metrics as the null the fixture states for every
 * leg.
 */
object GeoDataItinerarySegmentsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val segments = case.fields.items("segments").map { fixtureSegment(it as? FixtureValue.Object ?: notAnObject()) }
        val groups = case.fields.items("groups").map { fixtureGroup(it as? FixtureValue.Object ?: notAnObject()) }
        return ConformanceOutcome.Value(FixtureValue.Array(itinerarySegments(segments, groups).map(::writtenLeg)))
    }

    private fun notAnObject(): Nothing = throw AssertionError("a segment or a group is not an object")
}
