package com.mountaya.geo

/**
 * Returns whether [position] names a place: it states a longitude and a latitude, its first two
 * values, and both are finite numbers. An altitude is no part of the place, so a position with
 * none still names one.
 *
 * The one statement of the rule every geo rule reads a position by. A position written short, or
 * with a longitude or a latitude that is `NaN` or infinite, names none.
 */
internal fun namesPlace(position: List<Double>): Boolean =
    position.size >= 2 && position[0].isFinite() && position[1].isFinite()
