package com.mountaya.geo

import com.mountaya.geo.NavigationStatus.Arrived
import com.mountaya.geo.NavigationStatus.Failed
import com.mountaya.geo.NavigationStatus.Idle
import com.mountaya.geo.NavigationStatus.Navigating
import com.mountaya.geo.NavigationStatus.OffRoute
import com.mountaya.geo.NavigationStatus.Rerouting
import com.mountaya.geo.NavigationStatus.Routing

/**
 * Returns the status [event] moves a navigation in [status] to: the contract's closed table, written
 * out cell by cell rather than derived, so it reads as the contract states it
 * (`geo.navigation.status`).
 *
 * A `start` moves every status to `routing` and a `stop` every status to `idle`. A first route that
 * fails is `failed`, since there is nothing to follow, while a new route that fails leaves the
 * traveller `off-route`, still on the route they hold: a reroute fails where the signal does, which
 * is where a traveller most needs the route they already have. While a new route is awaited a
 * position on or off the route moves nothing, so the answer decides, and an answer the navigation is
 * not awaiting moves nothing either.
 */
internal fun navigationStatusAfter(
    status: NavigationStatus,
    event: NavigationEvent,
): NavigationStatus =
    when (status) {
        Idle -> {
            when (event) {
                NavigationEvent.Start -> Routing
                NavigationEvent.RouteReceived -> Idle
                NavigationEvent.RouteFailed -> Idle
                NavigationEvent.OnRoute -> Idle
                NavigationEvent.OffRoute -> Idle
                NavigationEvent.Reroute -> Idle
                NavigationEvent.Arrived -> Idle
                NavigationEvent.Stop -> Idle
            }
        }

        Routing -> {
            when (event) {
                NavigationEvent.Start -> Routing
                NavigationEvent.RouteReceived -> Navigating
                NavigationEvent.RouteFailed -> Failed
                NavigationEvent.OnRoute -> Routing
                NavigationEvent.OffRoute -> Routing
                NavigationEvent.Reroute -> Routing
                NavigationEvent.Arrived -> Routing
                NavigationEvent.Stop -> Idle
            }
        }

        Navigating -> {
            when (event) {
                NavigationEvent.Start -> Routing
                NavigationEvent.RouteReceived -> Navigating
                NavigationEvent.RouteFailed -> Navigating
                NavigationEvent.OnRoute -> Navigating
                NavigationEvent.OffRoute -> OffRoute
                NavigationEvent.Reroute -> Navigating
                NavigationEvent.Arrived -> Arrived
                NavigationEvent.Stop -> Idle
            }
        }

        OffRoute -> {
            when (event) {
                NavigationEvent.Start -> Routing
                NavigationEvent.RouteReceived -> OffRoute
                NavigationEvent.RouteFailed -> OffRoute
                NavigationEvent.OnRoute -> Navigating
                NavigationEvent.OffRoute -> OffRoute
                NavigationEvent.Reroute -> Rerouting
                NavigationEvent.Arrived -> Arrived
                NavigationEvent.Stop -> Idle
            }
        }

        Rerouting -> {
            when (event) {
                NavigationEvent.Start -> Routing
                NavigationEvent.RouteReceived -> Navigating
                NavigationEvent.RouteFailed -> OffRoute
                NavigationEvent.OnRoute -> Rerouting
                NavigationEvent.OffRoute -> Rerouting
                NavigationEvent.Reroute -> Rerouting
                NavigationEvent.Arrived -> Arrived
                NavigationEvent.Stop -> Idle
            }
        }

        Arrived -> {
            when (event) {
                NavigationEvent.Start -> Routing
                NavigationEvent.RouteReceived -> Arrived
                NavigationEvent.RouteFailed -> Arrived
                NavigationEvent.OnRoute -> Arrived
                NavigationEvent.OffRoute -> Arrived
                NavigationEvent.Reroute -> Arrived
                NavigationEvent.Arrived -> Arrived
                NavigationEvent.Stop -> Idle
            }
        }

        Failed -> {
            when (event) {
                NavigationEvent.Start -> Routing
                NavigationEvent.RouteReceived -> Failed
                NavigationEvent.RouteFailed -> Failed
                NavigationEvent.OnRoute -> Failed
                NavigationEvent.OffRoute -> Failed
                NavigationEvent.Reroute -> Failed
                NavigationEvent.Arrived -> Failed
                NavigationEvent.Stop -> Idle
            }
        }
    }
