package com.mountaya.conformance

import com.mountaya.geo.boundsOf
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.bounds-of`: the box around every position of the case's `geometries`, each built as
 * the client's GeoJSON type, or null where nothing is bounded.
 */
object GeoCameraBoundsOfRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val geometries = case.fields.items("geometries").map(::fixtureGeometry)
        val box = boundsOf(geometries) ?: return ConformanceOutcome.Value(FixtureValue.Null)
        return ConformanceOutcome.Value(FixtureValue.Array(box.map { FixtureValue.Number(it) }))
    }
}
