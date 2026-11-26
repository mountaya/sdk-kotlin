package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class NavigationStateTest {
    private val line = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0))

    @Test
    fun aNavigationStartsIdleHoldingNothing() {
        val idle = NavigationState.idle

        assertEquals(NavigationStatus.Idle, idle.status)
        assertNull(idle.route)
        assertNull(idle.progress)
        assertNull(idle.error)
        assertEquals("NavigationState(idle, route=null, progress=null, error=null)", idle.toString())
    }

    /**
     * By what they hold, so a route landing equal to the one held, or a reading that did not move,
     * is no change an engine reports.
     */
    @Test
    fun twoStatesAreEqualWhereEveryMemberIs() {
        val route = followedRouteOf(NavigationRoute(line, duration = 600.0))
        val held = NavigationState(NavigationStatus.Navigating, route)

        assertEquals(
            held,
            NavigationState(NavigationStatus.Navigating, followedRouteOf(NavigationRoute(line, duration = 600.0))),
        )
        assertEquals(held.hashCode(), NavigationState(NavigationStatus.Navigating, route).hashCode())
        assertNotEquals(held, NavigationState(NavigationStatus.OffRoute, route))
        assertNotEquals(held, NavigationState(NavigationStatus.Navigating, route, error = NavigationError.EmptyRoute()))
        assertNotEquals(held, NavigationState(NavigationStatus.Navigating))
    }
}
