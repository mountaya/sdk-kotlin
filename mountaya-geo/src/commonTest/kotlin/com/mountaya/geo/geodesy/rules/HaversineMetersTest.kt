package com.mountaya.geo

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class HaversineMetersTest {
    @Test
    fun theSphereIsTheMeanEarthsAndItsDegreeIsTheRadiusTimesPiOverOneHundredAndEighty() {
        assertEquals(6_371_000.0, EARTH_RADIUS_METERS)
        assertEquals(111_194.92664455874, METERS_PER_DEGREE_MEAN_SPHERE)
    }

    @Test
    fun oneDegreeOfTheEquatorIsOneDegreeOfTheMeanSphere() {
        assertEquals(METERS_PER_DEGREE_MEAN_SPHERE, haversineMeters(listOf(0.0, 0.0), listOf(1.0, 0.0)), 1e-6)
    }

    @Test
    fun aPositionIsNoDistanceFromItselfAndTheAntipodesAreHalfTheCircumference() {
        assertEquals(0.0, haversineMeters(listOf(6.8652, 45.9237), listOf(6.8652, 45.9237)))
        assertEquals(PI * EARTH_RADIUS_METERS, haversineMeters(listOf(0.0, 90.0), listOf(0.0, -90.0)), 1e-6)
    }

    @Test
    fun theAntimeridianIsCrossedTheShortWayRound() {
        assertEquals(22_238.985328911924, haversineMeters(listOf(179.9, 0.0), listOf(-179.9, 0.0)), 1e-6)
    }

    /** The term is clamped into 0 to 1 before its root, so two antipodes never measure NaN. */
    @Test
    fun twoAntipodesWrittenAnyWayMeasureHalfTheCircumference() {
        assertEquals(PI * EARTH_RADIUS_METERS, haversineMeters(listOf(0.0, 0.0), listOf(180.0, 0.0)), 1e-6)
        assertEquals(PI * EARTH_RADIUS_METERS, haversineMeters(listOf(-37.5, 12.25), listOf(142.5, -12.25)), 1e-6)
    }

    @Test
    fun aPositionThatNamesNoPlaceMeasuresNaN() {
        assertEquals(Double.NaN, haversineMeters(listOf(6.88), listOf(6.0, 45.0)))
        assertEquals(Double.NaN, haversineMeters(listOf(6.0, 45.0), emptyList()))
        assertEquals(Double.NaN, haversineMeters(listOf(Double.NaN, 45.0), listOf(6.0, 45.0)))
        assertEquals(Double.NaN, haversineMeters(listOf(6.0, 45.0), listOf(6.0, Double.POSITIVE_INFINITY)))
    }

    @Test
    fun anAltitudeIsNoPartOfTheDistance() {
        assertEquals(
            haversineMeters(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)),
            haversineMeters(listOf(6.8652, 45.9237, 1035.0), listOf(6.9271, 45.9682, 1252.0)),
        )
    }
}
