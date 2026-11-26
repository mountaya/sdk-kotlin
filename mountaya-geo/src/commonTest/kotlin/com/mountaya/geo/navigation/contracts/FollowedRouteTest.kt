package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FollowedRouteTest {
    private val line = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0))

    @Test
    fun twoFollowedRoutesAreEqualByWhatTheyHold() {
        val route = followedRouteOf(NavigationRoute(line, duration = 600.0))

        assertEquals(route, followedRouteOf(NavigationRoute(line.map { it.toList() }, duration = 600.0)))
        assertEquals(
            route.hashCode(),
            followedRouteOf(NavigationRoute(line.map { it.toList() }, duration = 600.0)).hashCode(),
        )
        assertNotEquals(route, followedRouteOf(NavigationRoute(line, duration = 601.0)))
        assertNotEquals(route, followedRouteOf(NavigationRoute(line.reversed(), duration = 600.0)))
        assertEquals(
            "FollowedRoute(line=[[0.0, 0.0], [0.01, 0.0]], steps=[])",
            FollowedRoute(line, emptyList()).toString(),
        )
    }

    /** Measured once, when followed, so a position read along it never measures the line again. */
    @Test
    fun aFollowedRouteIsMeasuredOnceAsEveryWalkOfALineMeasuresIt() {
        val route = FollowedRoute(listOf(listOf(0.0, 0.0), listOf(Double.NaN, 1.0), listOf(0.01, 0.0)), emptyList())

        assertEquals(cumulativeMeters(route.line), route.measured)
        assertEquals(route.measured[0], route.measured[1])
        assertEquals(route.line.size, route.measured.size)
    }

    /**
     * The measure is published, so a guidance plan or a host's own engine reads a step's length off it
     * rather than measuring the line again.
     */
    @Test
    fun aStepsLengthIsTheMeasureAtItsLastVertexLessTheOneAtItsFirst() {
        val route = FollowedRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.01, 0.01)), emptyList())

        assertEquals(cumulativeMeters(route.line.subList(1, 3)).last(), route.measured[2] - route.measured[1], 1e-9)
        assertEquals(0.0, route.measured[0])
    }

    /**
     * The line is held unboxed once, the vertices that name a place alone, so a reading of a position
     * walks plain numbers.
     */
    @Test
    fun theVerticesThatNameAPlaceAreHeldUnboxedWithTheirIndexes() {
        val route =
            FollowedRoute(
                listOf(listOf(Double.NaN, 1.0), listOf(6.0, 45.0), listOf(7.0), listOf(6.5, 45.5, 1_200.0)),
                emptyList(),
            )

        assertContentEquals(intArrayOf(1, 3), route.placed)
        assertContentEquals(doubleArrayOf(6.0, 6.5), route.longitudes)
        assertContentEquals(doubleArrayOf(45.0, 45.5), route.latitudes)
        assertContentEquals(cumulativeMeters(route.line).toDoubleArray(), route.meters)
    }
}
