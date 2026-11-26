package com.mountaya.geo

import kotlin.math.floor

/** The width of the sector each direction names, in degrees: an eighth of a turn. */
internal const val ASPECT_SECTOR_DEGREES: Double = 45.0

/**
 * aspectCardinal names the compass direction an aspect of [degrees] faces (`geo.terrain.aspect-cardinal`), a
 * bearing 0 north and 90 east as `slopeAspectFromNeighbours` answers one: the bearing is folded into [0, 360) by
 * whole turns, and each of the eight directions names the 45 degrees centred on its own heading, north from 337.5
 * to 22.5. A bearing on the edge between two sectors names the one clockwise of it, so 22.5 is north-east and
 * 337.5 north.
 *
 * Null for a bearing that is no finite number, which faces no direction.
 */
public fun aspectCardinal(degrees: Double): AspectCardinal? {
    if (!degrees.isFinite()) return null
    val folded = degrees - floor(degrees / DEGREES_PER_TURN) * DEGREES_PER_TURN
    // Half a sector on, so each sector starts where its direction's edge stands, the edge reading clockwise.
    val sector = floor((folded + ASPECT_SECTOR_DEGREES / 2) / ASPECT_SECTOR_DEGREES).toInt()
    val directions = AspectCardinal.entries
    return directions[sector % directions.size]
}
