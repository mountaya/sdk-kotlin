package com.mountaya.geo

import kotlin.math.abs

/**
 * wrapLongitudePastRange keeps [longitude] as it is written where it already lies within
 * [-180, 180], 180 included, and wraps any other as [wrapLongitude] does. A longitude a reader or
 * the wire wrote keeps its digits; one a map counted past the antimeridian is brought back by
 * whole turns. A longitude that is not finite wraps to `NaN`.
 */
public fun wrapLongitudePastRange(longitude: Double): Double =
    if (abs(longitude) <= LONGITUDE_LIMIT) longitude else wrapLongitude(longitude)
