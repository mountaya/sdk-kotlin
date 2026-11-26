package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CumulativeMetersTest {
    private val route =
        listOf(
            listOf(6.8652, 45.9237),
            listOf(6.8752, 45.9287),
            listOf(6.8852, 45.9337),
            listOf(6.8952, 45.9387),
            listOf(6.9052, 45.9437),
            listOf(6.9152, 45.9487),
            listOf(6.9271, 45.9682),
        )

    private fun assertDistances(
        expected: List<Double>,
        actual: List<Double>,
    ) {
        assertEquals(expected.size, actual.size)
        expected.indices.forEach { assertEquals(expected[it], actual[it], 1e-6, "vertex $it of $actual") }
    }

    @Test
    fun eachVertexStandsAtTheGreatCircleDistanceAlongTheLine() {
        assertDistances(
            listOf(
                0.0,
                952.543779427426,
                1_905.0309496519576,
                2_857.4615076298287,
                3_809.835450319783,
                4_762.152774680362,
                7_117.508508284167,
            ),
            cumulativeMeters(route),
        )
        assertEquals(
            listOf(0.0, METERS_PER_DEGREE_MEAN_SPHERE, 2.0 * METERS_PER_DEGREE_MEAN_SPHERE),
            cumulativeMeters(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(2.0, 0.0))),
        )
    }

    @Test
    fun aLegAcrossTheAntimeridianIsTheShortWayRound() {
        assertDistances(
            listOf(0.0, 21_289.81335773995),
            cumulativeMeters(listOf(listOf(179.9, -16.8), listOf(-179.9, -16.8))),
        )
    }

    /** A vertex that names no place keeps its index, standing at the distance of the last that did. */
    @Test
    fun aVertexThatNamesNoPlaceIsBridged() {
        assertDistances(
            listOf(0.0, 0.0, 135_786.09062692837),
            cumulativeMeters(listOf(listOf(6.0, 45.0), listOf(Double.NaN, 45.0), listOf(7.0, 46.0))),
        )
        assertDistances(
            listOf(0.0, 0.0, 135_786.09062692837),
            cumulativeMeters(listOf(listOf(6.0), listOf(6.0, 45.0), listOf(7.0, 46.0))),
        )
        assertDistances(
            listOf(0.0, 135_786.09062692837, 135_786.09062692837),
            cumulativeMeters(listOf(listOf(6.0, 45.0), listOf(7.0, 46.0), listOf(7.0, Double.POSITIVE_INFINITY))),
        )
    }

    @Test
    fun anAltitudeIsNoPartOfTheDistance() {
        assertEquals(
            cumulativeMeters(listOf(listOf(6.0, 45.0), listOf(7.0, 46.0))),
            cumulativeMeters(listOf(listOf(6.0, 45.0, 400.0), listOf(7.0, 46.0, 4_000.0))),
        )
    }

    @Test
    fun aLineOfOneVertexOrNoneHasNoLength() {
        assertEquals(listOf(0.0), cumulativeMeters(listOf(listOf(6.0, 45.0))))
        assertEquals(emptyList(), cumulativeMeters(emptyList()))
    }
}
