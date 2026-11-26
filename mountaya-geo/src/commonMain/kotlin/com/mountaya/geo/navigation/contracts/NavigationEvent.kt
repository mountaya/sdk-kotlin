package com.mountaya.geo

/**
 * NavigationEvent is everything a navigation is told, one of a closed set, in the order the contract
 * lists them: a route asked for, the answer to the request it acts on, a position read against the
 * route followed, a new route asked for from the traveller's position, the arrival, and the end
 * (`geo.navigation.status`).
 *
 * Internal, as the table it moves a status by is: a consumer observes the status an engine reports,
 * and tells the engine what to do through its commands, never by naming an event.
 */
internal enum class NavigationEvent(
    /** The event's id, as the contract spells it. */
    val id: String,
) {
    /** A route was asked for, to a new destination. */
    Start("start"),

    /** The route request the navigation acts on answered a route. */
    RouteReceived("route-received"),

    /** The route request the navigation acts on failed. */
    RouteFailed("route-failed"),

    /** A position was read within the off-route threshold of the route followed. */
    OnRoute("on-route"),

    /** A position was read past the off-route threshold of the route followed. */
    OffRoute("off-route"),

    /** A new route was asked for, from where the traveller is. */
    Reroute("reroute"),

    /** The traveller arrived. */
    Arrived("arrived"),

    /** The navigation ended. */
    Stop("stop"),
}
