package com.mountaya.geo

/**
 * routeLengthMeters is the length of [line] in meters, the sum of the great-circle distances
 * between its vertices: the last of its [cumulativeMeters], and 0 for a line with no vertex. A
 * vertex that names no place is bridged, as [cumulativeMeters] reads it.
 */
public fun routeLengthMeters(line: List<List<Double>>): Double = cumulativeMeters(line).lastOrNull() ?: 0.0
