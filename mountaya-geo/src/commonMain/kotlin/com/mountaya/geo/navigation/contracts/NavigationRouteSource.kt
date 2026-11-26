package com.mountaya.geo

/**
 * NavigationRouteSource is how a navigation asks for a route, and it is the seam that keeps the geo
 * layer from calling the network: the host's own call, the Data API's `directions` through its
 * client or a saved itinerary's segment, answering the [NavigationRoute] to follow.
 *
 * A route is read from the surface that wrote it: its steps through `readDirectionsSteps` for the
 * Data API and `readItineraryDirectionsSteps` for a REST directions segment. What it throws is the
 * failure the navigation records, in [NavigationError.RouteFailed]. The coroutine asking is cancelled
 * where the navigation abandons the request, by a stop or a close, so a source that suspends ends its
 * own call then, and its answer is dropped whatever it is.
 *
 * ```kotlin
 * val source = NavigationRouteSource<List<List<Double>>> { waypoints -> routeFromYourClient(waypoints) }
 * ```
 *
 * @param Request What the host asks a route for, handed to the source as the host made it.
 */
public fun interface NavigationRouteSource<in Request> {
    /** Answers the route to follow for [request], or throws what the navigation records as its failure. */
    public suspend fun route(request: Request): NavigationRoute
}
