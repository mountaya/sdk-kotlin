package com.mountaya.geo

/** The longitude of the antimeridian, half a turn from the prime meridian. */
internal const val LONGITUDE_LIMIT: Double = 180.0

/**
 * wrapLongitude brings [longitude], in degrees, into [-180, 180) by whole turns, so the
 * antimeridian is written -180 and 190 is -170. A longitude that is not finite names no meridian
 * and wraps to `NaN`.
 *
 * It is the remainder of the longitude past -180 by a turn, taken twice so a negative one lands in
 * the turn too: `((longitude + 180) % 360 + 360) % 360 - 180`, each `%` the truncated remainder.
 * The arithmetic costs a longitude already in the range its last digits; [wrapLongitudePastRange]
 * keeps those.
 */
public fun wrapLongitude(longitude: Double): Double =
    ((longitude + LONGITUDE_LIMIT) % DEGREES_PER_TURN + DEGREES_PER_TURN) % DEGREES_PER_TURN - LONGITUDE_LIMIT
