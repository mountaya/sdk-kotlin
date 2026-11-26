package com.mountaya.conformance

import com.mountaya.geo.AngleUnit
import com.mountaya.geo.EARTH_RADIUS_METERS
import com.mountaya.geo.convertAngle
import com.mountaya.geo.haversineMeters
import kotlinx.coroutines.test.TestScope

/**
 * `geo.geodesy.haversine`: the great-circle distance in meters between the case's positions `a` and
 * `b`. The fixture's radius is held to `EARTH_RADIUS_METERS` and its degree to the angle conversion.
 */
object GeoGeodesyHaversineRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "degreesToRadians" to convertAngle(1.0, AngleUnit.Degree, AngleUnit.Radian),
                "earthRadiusMeters" to EARTH_RADIUS_METERS,
            ),
        )
        val input = case.fields
        val distance = haversineMeters(fixturePosition(input["a"], "a"), fixturePosition(input["b"], "b"))
        return ConformanceOutcome.Value(FixtureValue.Number(distance))
    }
}
