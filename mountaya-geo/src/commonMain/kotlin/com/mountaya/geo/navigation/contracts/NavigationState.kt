package com.mountaya.geo

/**
 * NavigationState is what a navigation reports: the status it is in, the route it follows, where the
 * traveller stands on it, and why the last route request it acted on failed. A renderer draws it and
 * decides none of it, so a Mountaya map and a host's own screen show one navigation.
 *
 * Two are equal where every member is, a route and a reading by what they hold, so an engine reports a
 * state only where it differs from the one it holds.
 */
public class NavigationState(
    /** The status the navigation is in. */
    public val status: NavigationStatus,
    /** The route followed, or null where the navigation holds none. */
    public val route: FollowedRoute? = null,
    /** Where the traveller stands on [route], from the last position read on it, or null before any. */
    public val progress: NavigationProgress? = null,
    /** Why the last route request the navigation acted on failed, or null where none did since the last start. */
    public val error: NavigationError? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is NavigationState && other.status == status && other.route == route && other.progress == progress &&
            other.error == error

    override fun hashCode(): Int =
        ((status.hashCode() * 31 + route.hashCode()) * 31 + progress.hashCode()) * 31 + error.hashCode()

    override fun toString(): String = "NavigationState(${status.id}, route=$route, progress=$progress, error=$error)"

    /** The state every navigation starts in. */
    public companion object {
        /**
         * The state holding no route, no reading and no error: where a navigation starts, and where a stop leaves it.
         */
        public val idle: NavigationState = NavigationState(NavigationStatus.Idle)
    }
}
