package com.mountaya.geo

/**
 * RouteReading is one position read against a followed route in one pass over its line: where the
 * traveller stands on it, and how far they are from it, which decides whether they are off it.
 *
 * Internal: an engine reads both from one walk of the line, and a host reads each through its own rule,
 * `navigationProgressAt` and `metersToRoute`, which answer the same numbers.
 */
internal class RouteReading(
    /** Where the traveller stands, as `navigationProgressAt` reads it. */
    val progress: NavigationProgress,
    /** The traveller's distance in meters to the route's line, as `metersToRoute` measures it. */
    val meters: Double,
)
