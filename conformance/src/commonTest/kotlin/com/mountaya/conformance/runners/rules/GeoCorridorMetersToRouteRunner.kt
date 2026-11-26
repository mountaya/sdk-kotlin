package com.mountaya.conformance

import com.mountaya.client.GeoJsonLineString
import com.mountaya.geo.METERS_PER_DEGREE_MEAN_SPHERE
import com.mountaya.geo.metersToRoute
import kotlinx.coroutines.test.TestScope

/**
 * `geo.corridor.meters-to-route`: the distance in meters from the case's `point` to its `lines`. The
 * fixture writes no distance as null, "where an SDK answers its infinity", so this SDK's
 * `Double.POSITIVE_INFINITY` is answered as null and every finite distance as itself. The fixture's
 * mean-sphere degree is held to `METERS_PER_DEGREE_MEAN_SPHERE`.
 */
object GeoCorridorMetersToRouteRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("metersPerDegreeMeanSphere" to METERS_PER_DEGREE_MEAN_SPHERE))
        val input = case.fields
        val lines =
            input.items("lines").map { item ->
                val line = item as? FixtureValue.Object ?: throw AssertionError("a line is not an object")
                if (line.textOrNull("type") != "LineString") throw AssertionError("a line is not a LineString")
                GeoJsonLineString(line.items("coordinates").map { fixturePosition(it, "coordinates") })
            }
        val distance = metersToRoute(fixturePosition(input["point"], "point"), lines)
        return ConformanceOutcome.Value(
            if (distance == Double.POSITIVE_INFINITY) FixtureValue.Null else FixtureValue.Number(distance),
        )
    }
}
