package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class GroundMetersTest {
    private val degree = METERS_PER_DEGREE_MEAN_SPHERE
    private val equator = listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(2.0, 0.0), listOf(4.0, 0.0))

    private fun assertDistances(
        expected: List<Double>,
        actual: List<Double>,
    ) {
        assertEquals(expected.size, actual.size)
        expected.indices.forEach { assertEquals(expected[it], actual[it], 1e-6, "vertex $it of $actual") }
    }

    @Test
    fun aRouteWithNoJumpIsItsLengthAlongTheLine() {
        assertEquals(cumulativeMeters(equator), groundMeters(RouteGeometry(equator)))
    }

    /** The vertex a jump lands on stands at the distance of the vertex it left. */
    @Test
    fun aJumpChargesNothingForItsLeg() {
        assertDistances(
            listOf(0.0, degree, degree, 3.0 * degree),
            groundMeters(RouteGeometry(equator, jumps = listOf(2))),
        )
        assertDistances(listOf(0.0, 0.0, degree, degree), groundMeters(RouteGeometry(equator, jumps = listOf(1, 3))))
    }

    @Test
    fun aJumpTheLineHasNoLegToIsNone() {
        for (jumps in listOf(listOf(0), listOf(4), listOf(-1), listOf(Int.MAX_VALUE))) {
            assertEquals(cumulativeMeters(equator), groundMeters(RouteGeometry(equator, jumps = jumps)), "for $jumps")
        }
        assertDistances(
            listOf(0.0, degree, degree, 3.0 * degree),
            groundMeters(RouteGeometry(equator, jumps = listOf(2, 2))),
        )
    }

    @Test
    fun aLineWithNoVertexHasNoDistance() {
        assertEquals(emptyList(), groundMeters(RouteGeometry(emptyList(), jumps = listOf(1))))
    }
}
