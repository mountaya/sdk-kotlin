package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.max-bounds-refusal`: whether a pan lock with the case's `edges`, `[west, south, east,
 * north]`, is refused before it reaches a renderer. The class of the refusal is the map engine's,
 * which the contract's ladder does not name, so the fixture expects whether the lock is refused,
 * and the rule a map engine refuses through answers exactly that.
 */
object GeoCameraMaxBoundsRefusalRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome =
        ConformanceOutcome.Value(FixtureValue.Bool(refusesMaxBounds(fixturePosition(case.fields["edges"], "edges"))))
}
