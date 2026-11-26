package com.mountaya.conformance

import com.mountaya.geo.isochroneColor
import com.mountaya.geo.isochroneRamp
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.isochrone-color`: the fill of the band at the case's `index`, a whole number, which the
 * rule's `Int` holds; one of another shape fails the case. The ramp is held to the one Geo publishes.
 */
object GeoLayersIsochroneColorRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, emptyMap(), values = mapOf("ramp" to textArray(isochroneRamp)))
        val index = wholeOrNull(case.fields, "index") ?: throw AssertionError("the case states no `index`")
        return ConformanceOutcome.Value(FixtureValue.Text(isochroneColor(index)))
    }
}
