package com.mountaya.conformance

import com.mountaya.geo.lineBetween
import kotlinx.coroutines.test.TestScope

/** `geo.profile.line-between`: the stretch of the case's `series` between its `from` and its `to` as a GeoJSON line, or null. */
object GeoProfileLineBetweenRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val line = lineBetween(fixtureSeries(input["series"]).points, input.number("from"), input.number("to"))
        return ConformanceOutcome.Value(line?.let(::writtenGeometry) ?: FixtureValue.Null)
    }
}
