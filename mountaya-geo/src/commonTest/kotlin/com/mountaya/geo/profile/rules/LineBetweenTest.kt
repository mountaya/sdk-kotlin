package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonLineString
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LineBetweenTest {
    private val meridian =
        points(0.0 to listOf(6.0, 45.0), 100.0 to listOf(6.0, 45.0009), 200.0 to listOf(6.0, 45.0018))

    private fun points(vararg entries: Pair<Double, List<Double>>): List<ElevationPoint> =
        entries.mapIndexed { index, (distance, lngLat) -> ElevationPoint(distance, 0.0, lngLat, index) }

    private fun coordinates(geometry: GeoJsonGeometry?): List<List<Double>>? =
        (geometry as? GeoJsonLineString)?.coordinates

    /** Two positions hold as many numbers, each within the tolerance of the other's. */
    private fun assertClose(
        expected: List<Double>,
        actual: List<Double>,
        tolerance: Double,
    ) {
        assertEquals(expected.size, actual.size)
        for ((wanted, read) in expected.zip(actual)) assertEquals(wanted, read, tolerance)
    }

    @Test
    fun aStretchStartsAndEndsOnTheCurveTheSamplesBetweenItHeld() {
        val line =
            assertNotNull(
                coordinates(lineBetween(meridian, 50.0, 150.0)),
                "the line between the two distances holds coordinates",
            )

        assertEquals(3, line.size)
        assertClose(listOf(6.0, 45.00045), line[0], 1e-9)
        assertEquals(listOf(6.0, 45.0009), line[1])
        assertClose(listOf(6.0, 45.00135), line[2], 1e-9)
        assertEquals(
            listOf(listOf(6.0, 45.0), listOf(6.0, 45.0009), listOf(6.0, 45.0018)),
            coordinates(lineBetween(meridian, -100.0, 300.0)),
        )
        assertEquals(2, coordinates(lineBetween(meridian, 120.0, 180.0))?.size)
    }

    @Test
    fun noLengthBackwardsPastTheSamplesAndNoSampleAreNoLine() {
        assertNull(lineBetween(meridian, 100.0, 100.0))
        assertNull(lineBetween(meridian, 150.0, 50.0))
        assertNull(lineBetween(meridian, 1_000.0, 1_000.0))
        assertNull(lineBetween(emptyList(), 0.0, 200.0))
        assertNull(lineBetween(meridian, Double.NaN, 200.0))
    }

    @Test
    fun whereTwoLinesMeetAStretchStartsOnTheLaterAndEndsOnTheEarlier() {
        val joined =
            points(
                0.0 to listOf(6.0, 45.0),
                100.0 to listOf(6.0, 45.0009),
                100.0 to listOf(6.1, 45.1),
                200.0 to listOf(6.1, 45.1009),
            )

        assertEquals(listOf(listOf(6.1, 45.1), listOf(6.1, 45.1009)), coordinates(lineBetween(joined, 100.0, 200.0)))
        assertEquals(listOf(listOf(6.0, 45.0), listOf(6.0, 45.0009)), coordinates(lineBetween(joined, 0.0, 100.0)))
    }

    @Test
    fun acrossTheAntimeridianTheStretchIsReadTheShortWayWithinTheLongitudeRange() {
        val across = points(0.0 to listOf(179.9, 0.0), 100.0 to listOf(-179.9, 0.0))
        val line =
            assertNotNull(
                coordinates(lineBetween(across, 25.0, 75.0)),
                "the line between the two distances holds coordinates",
            )

        assertClose(listOf(179.95, 0.0), line[0], 1e-9)
        assertClose(listOf(-179.95, 0.0), line[1], 1e-9)
        assertEquals(180.0, abs(assertNotNull(positionAt(across, 50.0), "a position stands at the distance")[0]))
    }
}
