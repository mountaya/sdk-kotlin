package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import kotlin.math.nextDown
import kotlin.math.nextUp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(InternalMountayaApi::class)
class PositionAtMetersTest {
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

    private val nowhere = listOf(Double.NaN, Double.NaN)

    private fun assertPosition(
        expected: List<Double>,
        actual: List<Double>,
    ) {
        assertEquals(2, actual.size, "$actual")
        assertEquals(expected[0], actual[0], 1e-9, "the longitude of $actual")
        assertEquals(expected[1], actual[1], 1e-9, "the latitude of $actual")
    }

    @Test
    fun aDistanceIsInterpolatedBetweenTheTwoVerticesThatBracketIt() {
        assertPosition(listOf(6.902563628035973, 45.942381814017985), positionAtMeters(route, 3_558.7542541420835))
        val equator = listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(2.0, 0.0))
        assertPosition(listOf(1.5, 0.0), positionAtMeters(equator, 1.5 * METERS_PER_DEGREE_MEAN_SPHERE))
        assertPosition(listOf(1.0, 0.0), positionAtMeters(equator, METERS_PER_DEGREE_MEAN_SPHERE))
    }

    @Test
    fun aDistanceIsHeldToTheLine() {
        assertEquals(listOf(6.8652, 45.9237), positionAtMeters(route, 0.0))
        assertEquals(listOf(6.8652, 45.9237), positionAtMeters(route, -50.0))
        assertEquals(listOf(6.8652, 45.9237), positionAtMeters(route, Double.NEGATIVE_INFINITY))
        assertEquals(listOf(6.9271, 45.9682), positionAtMeters(route, 1e9))
        assertEquals(listOf(6.9271, 45.9682), positionAtMeters(route, Double.POSITIVE_INFINITY))
    }

    /** Its longitude continues from the leg's first vertex: past 180 eastward, below -180 westward. */
    @Test
    fun aLegAcrossTheAntimeridianIsInterpolatedTheShortWayRound() {
        val eastward = listOf(listOf(179.9, -16.8), listOf(-179.9, -16.8))
        val westward = listOf(listOf(-179.9, -16.8), listOf(179.9, -16.8))

        assertPosition(listOf(180.05, -16.8), positionAtMeters(eastward, 15_967.360018304962))
        assertPosition(listOf(-180.05, -16.8), positionAtMeters(westward, 15_967.360018304962))
        assertPosition(listOf(180.0, -16.8), positionAtMeters(eastward, 21_289.81335773995 / 2.0))
    }

    @Test
    fun anAltitudeIsNotCarriedAndAVertexThatNamesNoPlaceIsBridged() {
        assertEquals(listOf(6.0, 45.0), positionAtMeters(listOf(listOf(6.0, 45.0, 1_000.0), listOf(7.0, 45.0)), 0.0))
        val bridged = listOf(listOf(0.0, 0.0), listOf(Double.NaN, 0.0), listOf(5.0), listOf(2.0, 0.0))
        assertPosition(listOf(1.0, 0.0), positionAtMeters(bridged, METERS_PER_DEGREE_MEAN_SPHERE))
    }

    @Test
    fun aLineOfOnePlaceIsThatPlace() {
        assertEquals(listOf(6.0, 45.0), positionAtMeters(listOf(listOf(6.0, 45.0)), 100.0))
        assertEquals(listOf(6.0, 45.0), positionAtMeters(listOf(listOf(6.0, 45.0), listOf(6.0, 45.0)), 100.0))
    }

    @Test
    fun aLineThatNamesNoPlaceOrADistanceAtNoNumberHasNoPosition() {
        assertEquals(nowhere, positionAtMeters(emptyList(), 0.0))
        assertEquals(nowhere, positionAtMeters(listOf(listOf(6.0), listOf(Double.NaN, 45.0)), 0.0))
        assertEquals(nowhere, positionAtMeters(route, Double.NaN))
    }

    /** Every line the tests above read, as a route followed, each with what reading it along its line answers. */
    private val lines =
        listOf(
            route,
            listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(2.0, 0.0)),
            listOf(listOf(179.9, -16.8), listOf(-179.9, -16.8)),
            listOf(listOf(-179.9, -16.8), listOf(179.9, -16.8), listOf(179.7, -16.9)),
            listOf(listOf(0.0, 0.0), listOf(Double.NaN, 0.0), listOf(5.0), listOf(2.0, 0.0)),
            listOf(listOf(Double.NaN, 1.0), listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.0), listOf(7.0, 45.0)),
            listOf(listOf(6.0, 45.0)),
            listOf(listOf(6.0, 45.0), listOf(6.0, 45.0)),
            listOf(listOf(6.0), listOf(Double.NaN, 45.0)),
        )

    /**
     * Read along a route followed, a position is the one its line answers, number for number: at, before and past
     * each vertex's own distance, between two, beyond both ends, and at no number.
     */
    @Test
    fun aPositionAlongARouteFollowedIsTheOneItsLineAnswersNumberForNumber() {
        var read = 0
        for (line in lines) {
            val followed =
                assertNotNull(followedRouteOf(NavigationRoute(line)), "the route is one a navigation follows")
            val distances =
                followed.measured.flatMap { at -> listOf(at, at.nextUp(), at.nextDown(), at + 0.5, at * 0.37) } +
                    listOf(0.0, -0.0, -1.0, 1e12, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
            for (meters in distances) {
                assertEquals(
                    positionAtMeters(line, meters),
                    positionAtMeters(followed, meters),
                    "$meters m along $line",
                )
                read += 1
            }
        }
        assertTrue(read > 100, "$read positions read")
    }

    /**
     * The route holds its line's measure, so a position read along it measures nothing, where one read along the line
     * measures it every time (`replay-measure`).
     */
    @Test
    fun aPositionAlongARouteFollowedMeasuresNothingWhereOneAlongItsLineMeasuresIt() {
        val followed = assertNotNull(followedRouteOf(NavigationRoute(route)), "the route is one a navigation follows")
        val along = (0..60).map { it * 100.0 }

        val byRoute = MeasureProbe.measuring { along.forEach { positionAtMeters(followed, it) } }
        val byLine = MeasureProbe.measuring { along.forEach { positionAtMeters(route, it) } }

        assertEquals(emptyList(), byRoute)
        assertEquals(along.size, byLine.size, "the control: each read along the line measures it")
    }
}
