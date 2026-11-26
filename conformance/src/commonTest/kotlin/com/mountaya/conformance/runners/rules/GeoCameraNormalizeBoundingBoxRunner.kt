package com.mountaya.conformance

import com.mountaya.geo.normalizeBoundingBox
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.normalize-bounding-box`: the case's `bounds` in canonical form. The Web Mercator limit
 * is held to the latitude the rule holds a box reaching a pole to.
 */
object GeoCameraNormalizeBoundingBoxRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf("webMercatorMaxLatitude" to normalizeBoundingBox(listOf(0.0, -90.0, 1.0, 90.0))[3]),
        )
        val box = normalizeBoundingBox(fixturePosition(case.fields["bounds"], "bounds"))
        return ConformanceOutcome.Value(FixtureValue.Array(box.map { FixtureValue.Number(it) }))
    }
}
