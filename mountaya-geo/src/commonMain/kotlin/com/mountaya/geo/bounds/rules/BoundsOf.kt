package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonMultiPolygon
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon

/**
 * boundsOf is the box `[west, south, east, north]`, in degrees, around every position of
 * [geometries], a collection of any depth flattened first, so a directions segment bounds its vias
 * as well as its line; or null where no position names a place.
 *
 * A longitude is read in two frames, as written and with the western hemisphere turned a turn
 * east, and the box is the narrower of the two: a set as written more than half a turn wide that
 * the turned frame reads narrower, one straddling the antimeridian or a shape cut in two there, is
 * framed across it, its west edge within [-180, 180] and its east edge past 180, as
 * [normalizeBoundingBox] keeps a box. Positions alone cannot tell such a set from one drawn wider
 * than half the world across the prime meridian, which reads the narrow way too. A set whose every
 * position lies on the antimeridian, written as both 180 and -180, keeps its written width. Every
 * other set keeps its longitudes as written.
 *
 * A position that names no place, short of a longitude or a latitude or with either not finite,
 * bounds nothing, and an altitude is ignored. An edge is the first extreme value met, so of 0 and
 * -0 it is the one written first.
 */
public fun boundsOf(geometries: List<GeoJsonGeometry>): List<Double>? {
    val extent = Extent()
    for (geometry in geometries) extent.cover(geometry)
    return extent.box()
}

/** The extremes of the positions met so far, the longitudes in both frames. */
private class Extent {
    var west = Double.POSITIVE_INFINITY
    var east = Double.NEGATIVE_INFINITY
    var turnedWest = Double.POSITIVE_INFINITY
    var turnedEast = Double.NEGATIVE_INFINITY
    var south = Double.POSITIVE_INFINITY
    var north = Double.NEGATIVE_INFINITY

    fun cover(geometry: GeoJsonGeometry) {
        when (geometry) {
            is GeoJsonPoint -> {
                visit(geometry.coordinates)
            }

            is GeoJsonMultiPoint -> {
                geometry.coordinates.forEach(::visit)
            }

            is GeoJsonLineString -> {
                geometry.coordinates.forEach(::visit)
            }

            is GeoJsonMultiLineString -> {
                geometry.coordinates.forEach { it.forEach(::visit) }
            }

            is GeoJsonPolygon -> {
                geometry.coordinates.forEach { it.forEach(::visit) }
            }

            is GeoJsonMultiPolygon -> {
                geometry.coordinates.forEach { polygon ->
                    polygon.forEach { it.forEach(::visit) }
                }
            }

            is GeoJsonGeometryCollection -> {
                geometry.geometries.forEach(::cover)
            }
        }
    }

    private fun visit(position: List<Double>) {
        if (!namesPlace(position)) return
        val longitude = position[0]
        val latitude = position[1]
        // The western hemisphere a turn east, where a set straddling the antimeridian is one short run.
        val turned = if (longitude < 0.0) longitude + DEGREES_PER_TURN else longitude
        if (longitude < west) west = longitude
        if (longitude > east) east = longitude
        if (turned < turnedWest) turnedWest = turned
        if (turned > turnedEast) turnedEast = turned
        if (latitude < south) south = latitude
        if (latitude > north) north = latitude
    }

    fun box(): List<Double>? {
        if (south == Double.POSITIVE_INFINITY) return null
        val writtenSpan = east - west
        val turnedSpan = turnedEast - turnedWest
        // The turned frame is asked only of a set more than half a turn wide as written, so a
        // turn's rounding (a tiny negative longitude plus 360 is 360) never reads a small set the
        // long way round. A turned frame of no width on the antimeridian is a set lying wholly on
        // it, written from both sides, and it keeps its written width.
        val onAntimeridian = turnedSpan == 0.0 && turnedWest == LONGITUDE_LIMIT
        if (writtenSpan > LONGITUDE_LIMIT && !onAntimeridian && turnedSpan < writtenSpan) {
            val shift = if (turnedWest > LONGITUDE_LIMIT) DEGREES_PER_TURN else 0.0
            return listOf(turnedWest - shift, south, turnedEast - shift, north)
        }
        return listOf(west, south, east, north)
    }
}
