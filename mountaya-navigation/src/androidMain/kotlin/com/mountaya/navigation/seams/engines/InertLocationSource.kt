package com.mountaya.navigation

/**
 * InertLocationSource is a location that never moves: it states no position at all, so every position
 * a navigation reads is one the app hands to `NavigationEngine.setPosition` itself.
 *
 * It is the engine's default, for an app whose positions come from elsewhere: its own location stack,
 * a recorded track, a test.
 */
public class InertLocationSource : LocationSource {
    /** Does nothing: no position is ever delivered. */
    override fun start(deliver: (position: List<Double>) -> Unit) {}

    /** Does nothing. */
    override fun stop() {}

    override fun toString(): String = "InertLocationSource"
}
