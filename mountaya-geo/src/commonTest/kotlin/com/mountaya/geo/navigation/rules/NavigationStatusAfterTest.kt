package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationStatusAfterTest {
    private fun after(
        status: NavigationStatus,
        vararg events: NavigationEvent,
    ): NavigationStatus = events.fold(status, ::navigationStatusAfter)

    @Test
    fun aStartRoutesAndAStopIdlesFromEveryStatus() {
        for (status in NavigationStatus.entries) {
            assertEquals(NavigationStatus.Routing, navigationStatusAfter(status, NavigationEvent.Start), "$status")
            assertEquals(NavigationStatus.Idle, navigationStatusAfter(status, NavigationEvent.Stop), "$status")
        }
    }

    /** A first route that fails leaves nothing to follow; a new one that fails leaves the route held. */
    @Test
    fun aFirstRouteThatFailsFailsAndANewOneLeavesTheTravellerOffRoute() {
        assertEquals(NavigationStatus.Failed, after(NavigationStatus.Routing, NavigationEvent.RouteFailed))
        assertEquals(NavigationStatus.OffRoute, after(NavigationStatus.Rerouting, NavigationEvent.RouteFailed))
        assertEquals(
            NavigationStatus.Rerouting,
            after(NavigationStatus.Rerouting, NavigationEvent.RouteFailed, NavigationEvent.Reroute),
        )
    }

    @Test
    fun aRerouteMovesOnlyATravellerOffTheRoute() {
        assertEquals(
            listOf(NavigationStatus.OffRoute),
            NavigationStatus.entries.filter { navigationStatusAfter(it, NavigationEvent.Reroute) != it },
        )
        assertEquals(
            NavigationStatus.Rerouting,
            navigationStatusAfter(NavigationStatus.OffRoute, NavigationEvent.Reroute),
        )
    }

    /** While a new route is awaited the answer decides, and an answer nobody awaits moves nothing. */
    @Test
    fun aPositionMovesNothingWhileANewRouteIsAwaited() {
        assertEquals(
            NavigationStatus.Rerouting,
            after(NavigationStatus.Rerouting, NavigationEvent.OnRoute, NavigationEvent.OffRoute),
        )
        assertEquals(NavigationStatus.Navigating, after(NavigationStatus.Rerouting, NavigationEvent.RouteReceived))
    }

    @Test
    fun anAnswerTheNavigationIsNotAwaitingMovesNothing() {
        val awaiting = setOf(NavigationStatus.Routing, NavigationStatus.Rerouting)
        for (status in NavigationStatus.entries - awaiting) {
            assertEquals(status, navigationStatusAfter(status, NavigationEvent.RouteReceived), "$status")
            assertEquals(status, navigationStatusAfter(status, NavigationEvent.RouteFailed), "$status")
        }
    }

    @Test
    fun aTravellerArrivesOnlyFromARouteTheyFollow() {
        assertEquals(
            listOf(NavigationStatus.Navigating, NavigationStatus.OffRoute, NavigationStatus.Rerouting),
            NavigationStatus.entries.filter {
                navigationStatusAfter(it, NavigationEvent.Arrived) ==
                    NavigationStatus.Arrived &&
                    it != NavigationStatus.Arrived
            },
        )
        assertEquals(
            NavigationStatus.Arrived,
            after(NavigationStatus.Arrived, NavigationEvent.OffRoute, NavigationEvent.OnRoute),
        )
    }

    @Test
    fun aJourneyWithAReroute() {
        val journey =
            listOf(
                NavigationEvent.Start,
                NavigationEvent.RouteReceived,
                NavigationEvent.OffRoute,
                NavigationEvent.Reroute,
                NavigationEvent.RouteReceived,
                NavigationEvent.Arrived,
            ).runningFold(NavigationStatus.Idle, ::navigationStatusAfter)

        assertEquals(
            listOf(
                NavigationStatus.Idle,
                NavigationStatus.Routing,
                NavigationStatus.Navigating,
                NavigationStatus.OffRoute,
                NavigationStatus.Rerouting,
                NavigationStatus.Navigating,
                NavigationStatus.Arrived,
            ),
            journey,
        )
    }
}
