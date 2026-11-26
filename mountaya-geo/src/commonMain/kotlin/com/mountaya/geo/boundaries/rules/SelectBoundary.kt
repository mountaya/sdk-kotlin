package com.mountaya.geo

/**
 * selectBoundary is the boundary a map locks its camera to from its [center], `[longitude,
 * latitude]`: of [boundaries], in the order the organization lists them, the first whose box holds
 * the center, else the first, and null where there is none, which a map reads as no lock.
 *
 * A box holds the center when the center's latitude lies between its south and north edges and
 * its meridian lies east of the west edge by no more than the box is wide, the longitudes read as
 * angles however many turns apart they are written, so a box across the antimeridian holds a
 * center written on either side. A center on an edge is inside.
 *
 * A center that names no place, short of a longitude or a latitude or with either not finite, is
 * held by no box. A box that is not four numbers holds nothing, and neither does one whose east
 * edge lies west of its west edge or whose north edge lies south of its south edge.
 */
public fun selectBoundary(
    boundaries: List<Boundaries>,
    center: List<Double>,
): Boundaries? {
    val first = boundaries.firstOrNull() ?: return null
    if (!namesPlace(center)) return first
    return boundaries.firstOrNull { holds(it.bbox, center[0], center[1]) } ?: first
}

private fun holds(
    box: List<Double>,
    longitude: Double,
    latitude: Double,
): Boolean {
    if (box.size != 4) return false
    val (west, south, east, north) = box
    if (!(latitude >= south && latitude <= north)) return false
    if (longitude >= west && longitude <= east) return true
    // How far east of the west edge the center's meridian lies, within [0, 360): inside the box
    // when that is no farther than the box is wide.
    val eastOfWestEdge = wrapLongitude(longitude - west - LONGITUDE_LIMIT) + LONGITUDE_LIMIT
    return eastOfWestEdge <= east - west
}
