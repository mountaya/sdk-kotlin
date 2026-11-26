package com.mountaya.conformance

import com.mountaya.geo.MapPaint
import kotlinx.coroutines.test.TestScope

/**
 * `geo.style.map-paint`: the paint the case's `patch` resolves to, the shipped paint with each member the
 * patch states set on a copy through `MapPaint`'s own builder, a member written null left as shipped
 * ([fixtureMapPaint]). The shipped paint is held to the one Geo publishes, `MapPaint.standard`.
 */
object GeoStyleMapPaintRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, emptyMap(), values = mapOf("paint" to writtenMapPaint(MapPaint.standard)))
        return ConformanceOutcome.Value(writtenMapPaint(fixtureMapPaint(case.fields["patch"])))
    }
}
