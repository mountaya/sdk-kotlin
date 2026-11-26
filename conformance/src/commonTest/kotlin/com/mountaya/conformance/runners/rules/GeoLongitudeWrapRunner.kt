package com.mountaya.conformance

import com.mountaya.geo.longitudeDelta
import com.mountaya.geo.wrapLongitude
import com.mountaya.geo.wrapLongitudePastRange
import kotlinx.coroutines.test.TestScope

/**
 * `geo.longitude.wrap`: `wrapLongitude`, `wrapLongitudePastRange` or `longitudeDelta`, the one the case's input
 * names as its `operation`: `wrap`, `pastRange` or `delta`.
 */
object GeoLongitudeWrapRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val answer =
            when (val operation = input.text("operation")) {
                "wrap" -> wrapLongitude(input.number("longitude"))
                "pastRange" -> wrapLongitudePastRange(input.number("longitude"))
                "delta" -> longitudeDelta(input.number("from"), input.number("to"))
                else -> throw AssertionError("`$operation` is no operation on a longitude this runner knows")
            }
        return ConformanceOutcome.Value(FixtureValue.Number(answer))
    }
}
