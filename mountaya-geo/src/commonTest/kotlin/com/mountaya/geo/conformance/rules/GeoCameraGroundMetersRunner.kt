package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.ground-meters`: the ground distance to each vertex of a route with the case's `line`
 * and its `jumps`, none where the case states none.
 *
 * A route's jump is a vertex index, an `Int`, so a jump the fixture writes as no whole number (or
 * past what an `Int` holds) cannot be built into a route at all: it is no jump by construction,
 * which is what the rule answers for it (CONTRACT-GAPS), and the runner builds the route without
 * it. Every whole-numbered jump, zero and past the line included, reaches the rule as written.
 */
object GeoCameraGroundMetersRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val jumps =
            input["jumps"]?.let { fixturePosition(it, "jumps") }.orEmpty().mapNotNull { jump ->
                jump.toInt().takeIf { it.toDouble() == jump }
            }
        val route = RouteGeometry(input.items("line").map { fixturePosition(it, "line") }, jumps = jumps)
        return ConformanceOutcome.Value(FixtureValue.Array(groundMeters(route).map { FixtureValue.Number(it) }))
    }
}
