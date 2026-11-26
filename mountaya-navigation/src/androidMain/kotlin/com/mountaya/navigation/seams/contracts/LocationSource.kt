package com.mountaya.navigation

import com.mountaya.geo.FollowedRoute

/**
 * LocationSource is where a navigation's positions come from: the platform's, a replay along the
 * route followed, none at all, or the app's own. An engine starts it when a navigation starts and
 * stops it when the navigation stops, and hands each position it states to the engine as
 * `NavigationEngine.setPosition` takes one: `[longitude, latitude]` in degrees, with an optional
 * altitude the engine does not read.
 *
 * A source never asks for location access and never starts a foreground service: both are the app's
 * decisions, with the manifest declarations and the Play policy consequences only the app can weigh,
 * so a source is handed a permission the app already holds. It is driven from the thread its engine
 * is, and delivers every position there.
 */
public interface LocationSource {
    /**
     * Begins stating positions, each handed to [deliver] until [stop]. [deliver] throws
     * `IllegalArgumentException` for a position that names no place, which the engine refuses and reads
     * nothing of. Starting a source that is started does nothing.
     */
    public fun start(deliver: (position: List<Double>) -> Unit)

    /** Ends the positions: nothing more is delivered until the next [start]. Stopping a source that is stopped does nothing. */
    public fun stop()

    /**
     * Is told of each route the navigation follows, and of none, null, where it stops following one: a
     * replay walks the route it is told of, and a source that reads the platform has nothing to do with
     * it, which is what this default does, since the platform states where the traveller is whatever
     * route they follow.
     */
    public fun follow(route: FollowedRoute?) {}
}
