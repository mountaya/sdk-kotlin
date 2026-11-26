package com.mountaya.conformance

import com.mountaya.geo.committedPace
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.committed-pace`: the committed pace over the case's `segments`, each built as the REST
 * API's segment with its `distance` and `duration` as the fixture writes them, a number JSON cannot
 * carry included, and a placeholder for every other member.
 */
object GeoProfileCommittedPaceRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val segments =
            case.fields.items("segments").map { item ->
                fixtureSegment(item as? FixtureValue.Object ?: throw AssertionError("a segment is not an object"))
            }
        return ConformanceOutcome.Value(numberOrNull(committedPace(segments)))
    }
}
