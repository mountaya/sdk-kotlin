package com.mountaya.geo

/**
 * NavigationStatus is the status a navigation is in, one of a closed set, so a renderer draws every
 * status there is and a test names every transition (`geo.navigation.status`).
 *
 * The entries are declared in the order the contract lists them, from [Idle]: asking for a first
 * route, following one, off it, asking for a new one from there, at its end, and with no route to
 * follow after a first request failed. What moves a navigation from one to another is the
 * contract's closed table, which every engine holds.
 */
public enum class NavigationStatus(
    /** The status's id, as the contract spells it. */
    public val id: String,
) {
    /** Following nothing, and asking for nothing: where a navigation starts, and where a stop leaves it. */
    Idle("idle"),

    /** Asking for a first route, with none to follow yet. */
    Routing("routing"),

    /** Following a route, on it. */
    Navigating("navigating"),

    /** Following a route, more than the off-route threshold from it. */
    OffRoute("off-route"),

    /** Asking for a new route from off the one held, and still following that one. */
    Rerouting("rerouting"),

    /** Standing on the route with no distance left to its end. */
    Arrived("arrived"),

    /** The failure of the request for a first route, which leaves no route to follow. */
    Failed("failed"),
}
