package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.off-route`: whether a traveller is off the route. A case with `meters` holds the
 * threshold to a distance already measured; one with a `line` and a `position` measures the traveller
 * to the route as a corridor is measured. The fixture's threshold is held to `OFF_ROUTE_METERS`.
 */
object GeoNavigationOffRouteRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNavigationConstants(case, mapOf("offRouteMeters" to FixtureValue.Number(OFF_ROUTE_METERS)))
        val input = case.fields
        val meters = input.numberOrNull("meters")
        val off =
            if (meters != null) {
                isOffRoute(meters)
            } else {
                val line = GeoJsonLineString(input.items("line").map { fixturePosition(it, "line") })
                isOffRouteAt(fixturePosition(input["position"], "position"), listOf(line))
            }
        return ConformanceOutcome.Value(FixtureValue.Bool(off))
    }
}
