package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.window-spans`: the case's `spans` a chart draws under itself over its `window`, each its place in
 * the list, its two ends in order and where its start and its end are drawn.
 */
object GeoProfileWindowSpansRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val spans =
            input.items("spans").map { item ->
                val span = item as? FixtureValue.Object ?: throw AssertionError("a span is not an object")
                ProfileRange(span.number("from"), span.number("to"))
            }
        val placed = placeSpans(spans, input.number("totalDistance"), profileWindow(input["window"]))
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                placed.map {
                    profileObject(
                        "end" to FixtureValue.Number(it.end),
                        "from" to FixtureValue.Number(it.from),
                        "index" to FixtureValue.Number(it.index.toDouble()),
                        "start" to FixtureValue.Number(it.start),
                        "to" to FixtureValue.Number(it.to),
                    )
                },
            ),
        )
    }
}
