package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsOffRouteTest {
    private val route = listOf(GeoJsonLineString(listOf(listOf(6.86, 45.85), listOf(6.9, 45.85))))

    @Test
    fun theThresholdIsFiftyMetersForEveryone() {
        assertEquals(50.0, OFF_ROUTE_METERS)
    }

    @Test
    fun aTravellerIsOnRouteAtTheThresholdAndOffPastIt() {
        assertFalse(isOffRoute(50.0))
        assertFalse(isOffRoute(49.99))
        assertTrue(isOffRoute(50.01))
        assertFalse(isOffRoute(0.0))
    }

    /**
     * A distance that is no number is past no threshold; the corridor never answers one, since a
     * position that names no place is infinitely far.
     */
    @Test
    fun aDistanceThatIsNoNumberIsPastNoThresholdAndInfinityIsPastEvery() {
        assertFalse(isOffRoute(Double.NaN))
        assertTrue(isOffRoute(Double.POSITIVE_INFINITY))
        assertTrue(isOffRouteAt(listOf(Double.NaN, 45.85), route))
    }

    @Test
    fun aTravellerIsMeasuredToTheRouteAsACorridorIs() {
        assertFalse(isOffRouteAt(listOf(6.88, 45.85), route))
        assertFalse(isOffRouteAt(listOf(6.88, 45.85 + 49.0 / METERS_PER_DEGREE_MEAN_SPHERE), route))
        assertTrue(isOffRouteAt(listOf(6.88, 45.85 + 51.0 / METERS_PER_DEGREE_MEAN_SPHERE), route))
        assertFalse(
            isOffRouteAt(
                listOf(179.9999, 0.0),
                listOf(GeoJsonLineString(listOf(listOf(-179.9998, -1.0), listOf(-179.9998, 1.0)))),
            ),
        )
    }

    /** A route of one vertex has no segment to be near, and neither has a route of no line. */
    @Test
    fun aRouteWithNoSegmentIsOffEverywhere() {
        assertTrue(isOffRouteAt(listOf(6.88, 45.85), listOf(GeoJsonLineString(listOf(listOf(6.88, 45.85))))))
        assertTrue(isOffRouteAt(listOf(6.88, 45.85), emptyList()))
    }
}
