package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi

/**
 * cumulativeMeters is the distance in meters along [line] to each of its vertices, `[longitude,
 * latitude]` in degrees: 0 at the first, and each later one the one before plus the great-circle
 * distance between them, so the last is the line's length. The answer has one distance per vertex,
 * ascending, and the line is measured identically by every rule that reads a distance along it.
 *
 * A vertex that names no place, short of a longitude or a latitude or with either not finite,
 * keeps its index and stands at the distance of the last vertex that did, 0 before any, and the
 * vertex after it is measured from that one, as every walk of a line reads it. An empty line
 * answers an empty list.
 *
 * Each measurement is recorded by the `MeasureProbe` a test holds, and by nothing otherwise.
 */
@OptIn(InternalMountayaApi::class)
public fun cumulativeMeters(line: List<List<Double>>): List<Double> {
    MeasureProbe.record(line)
    val distances = ArrayList<Double>(line.size)
    var running = 0.0
    var previous: List<Double>? = null
    for (vertex in line) {
        if (namesPlace(vertex)) {
            val from = previous
            if (from != null) running += haversineMeters(from, vertex)
            previous = vertex
        }
        distances += running
    }
    return distances
}
