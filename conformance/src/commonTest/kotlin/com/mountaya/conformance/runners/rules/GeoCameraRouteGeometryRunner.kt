package com.mountaya.conformance

import com.mountaya.geo.bearingDegrees
import com.mountaya.geo.cumulativeMeters
import com.mountaya.geo.positionAtMeters
import com.mountaya.geo.routeLengthMeters
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.route-geometry`: one of the measurements a fly-through is planned on, the one the case's input
 * names as its `measure`: `length` and `cumulative` of its `line`, `bearing` from `a` to `b`, or `position`,
 * the position `meters` along its `line`.
 */
object GeoCameraRouteGeometryRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val answer =
            when (val measure = input.text("measure")) {
                "length" -> {
                    FixtureValue.Number(routeLengthMeters(line(input)))
                }

                "cumulative" -> {
                    numbers(cumulativeMeters(line(input)))
                }

                "bearing" -> {
                    FixtureValue.Number(
                        bearingDegrees(fixturePosition(input["a"], "a"), fixturePosition(input["b"], "b")),
                    )
                }

                "position" -> {
                    numbers(positionAtMeters(line(input), input.number("meters")))
                }

                else -> {
                    throw AssertionError("`$measure` is no measure of a route this runner knows")
                }
            }
        return ConformanceOutcome.Value(answer)
    }

    private fun line(input: FixtureValue.Object) = input.items("line").map { fixturePosition(it, "line") }

    private fun numbers(values: List<Double>) = FixtureValue.Array(values.map { FixtureValue.Number(it) })
}
