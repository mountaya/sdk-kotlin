package com.mountaya.geo

import kotlin.math.cos
import kotlin.math.hypot

/** One leg of the walked lines: its two ends, where it starts on the distance axis, and its length. */
internal class WalkedLeg(
    val previous: List<Double>,
    val current: List<Double>,
    val start: Double,
    val meters: Double,
)

/** One close pass of a line by a place: where its nearest point stands on the axis, and how near. */
internal class MarkerPass(
    val distance: Double,
    val meters: Double,
)

/**
 * Lists every leg of [lines], laid end to end on the distance axis an elevation series is drawn
 * against: distance accumulates within a line, and the jump to the next line costs none and is no
 * leg, since nothing travels it. A vertex that names no place ends no leg: the leg runs from the
 * vertex before it that names one to the next that does.
 */
internal fun walkLegs(lines: List<List<List<Double>>>): List<WalkedLeg> {
    val legs = ArrayList<WalkedLeg>()
    var distance = 0.0
    for (line in lines) {
        var previous: List<Double>? = null
        for (current in line) {
            if (!namesPlace(current)) continue
            val from = previous
            if (from != null) {
                val meters = haversineMeters(from, current)
                legs += WalkedLeg(from, current, distance, meters)
                distance += meters
            }
            previous = current
        }
    }
    return legs
}

/**
 * Returns each close pass of [legs] by the place at [position]: each run of consecutive legs that
 * come within [proximityMeters] of it is one pass, at the nearest point of the run, in itinerary
 * order. A place the line never comes that near has none, and so does one that names no place.
 */
internal fun markerPasses(
    legs: List<WalkedLeg>,
    position: List<Double>,
    proximityMeters: Double,
): List<MarkerPass> {
    val passes = ArrayList<MarkerPass>()
    var best: MarkerPass? = null
    for (leg in legs) {
        val approach = approachOf(leg, position)
        if (approach.meters <= proximityMeters) {
            val held = best
            if (held == null || approach.meters < held.meters) best = approach
            continue
        }
        best?.let { passes += it }
        best = null
    }
    best?.let { passes += it }
    return passes
}

/**
 * The closest approach of [leg] to the place at [position], measured in a local east and north
 * frame at the leg's start: a degree north is the mean sphere's, and a degree east that times the
 * cosine of the start's latitude. Each longitude is read as the short way from the leg's start, so
 * a place written a whole turn away, or across the antimeridian, measures the meters it stands off.
 * A leg of no length is measured from its start.
 */
private fun approachOf(
    leg: WalkedLeg,
    position: List<Double>,
): MarkerPass {
    val startLng = leg.previous[0]
    val startLat = leg.previous[1]
    val metersPerDegreeEast = cos(startLat * RADIANS_PER_DEGREE) * METERS_PER_DEGREE_MEAN_SPHERE
    val legEast = longitudeDelta(startLng, leg.current[0]) * metersPerDegreeEast
    val legNorth = (leg.current[1] - startLat) * METERS_PER_DEGREE_MEAN_SPHERE
    val placeEast = longitudeDelta(startLng, position.getOrElse(0) { Double.NaN }) * metersPerDegreeEast
    val placeNorth = (position.getOrElse(1) { Double.NaN } - startLat) * METERS_PER_DEGREE_MEAN_SPHERE
    val lengthSquared = legEast * legEast + legNorth * legNorth
    val along =
        if (lengthSquared == 0.0) {
            0.0
        } else {
            minOf(1.0, maxOf(0.0, (placeEast * legEast + placeNorth * legNorth) / lengthSquared))
        }
    return MarkerPass(leg.start + leg.meters * along, hypot(placeEast - legEast * along, placeNorth - legNorth * along))
}
