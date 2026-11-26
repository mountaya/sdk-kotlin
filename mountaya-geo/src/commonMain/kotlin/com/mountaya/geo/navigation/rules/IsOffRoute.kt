package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString

/**
 * OFF_ROUTE_METERS is the distance from the route past which a traveller is off it, in meters
 * (`geo.navigation.off-route`).
 *
 * One number for every activity and every SDK, never a setting, so two apps following one route
 * agree on where a traveller left it. Fifty meters is about twice the error a phone's position
 * routinely carries under trees or between valley walls: far enough that the noise of an ordinary
 * position never reroutes a traveller, and near enough that a walker who takes a wrong fork at a
 * right angle is told within a minute.
 */
public const val OFF_ROUTE_METERS: Double = 50.0

/**
 * isOffRoute says whether a traveller [meters] from the route, as [metersToRoute] measures it, is off it:
 * past [OFF_ROUTE_METERS], and on it at the threshold and within it. A distance that is no number is
 * past no threshold.
 */
public fun isOffRoute(meters: Double): Boolean = meters > OFF_ROUTE_METERS

/**
 * isOffRouteAt says whether a traveller at [position] is off the route [lines] draw, measured as
 * [metersToRoute] measures a point to a line: in the local plane on the mean sphere, each segment
 * kept in one piece the short way round the antimeridian. A route of one vertex has no segment to be
 * near, and is infinitely far, so a traveller is off it everywhere. A position's reported accuracy,
 * speed and heading are not read, so one position is judged the same wherever it is judged.
 *
 * It is the rule every engine reads a position by, so an engine over a navigation library hands it
 * the library's own question, and the library acts on the contract's answer.
 */
public fun isOffRouteAt(
    position: List<Double>,
    lines: List<GeoJsonLineString>,
): Boolean = isOffRoute(metersToRoute(position, lines))
