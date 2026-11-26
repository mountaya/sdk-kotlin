package com.mountaya.navigation

import com.mountaya.geo.METERS_PER_DEGREE_MEAN_SPHERE
import com.mountaya.geo.positionAtMeters
import com.mountaya.geo.routeLengthMeters
import kotlin.math.PI
import kotlin.math.cos

/** Journeys holds what the engine's tests walk a route with: positions as a device moving along it states them. */
object Journeys {
    /** A position this many meters north of [position]. */
    fun north(
        position: List<Double>,
        meters: Double,
    ): List<Double> = listOf(position[0], position[1] + meters / METERS_PER_DEGREE_MEAN_SPHERE)

    /** A position this many meters west of [position], along its parallel. */
    fun west(
        position: List<Double>,
        meters: Double,
    ): List<Double> =
        listOf(position[0] - meters / (METERS_PER_DEGREE_MEAN_SPHERE * cos(position[1] * PI / 180.0)), position[1])

    /**
     * Every position a traveller walking [line] states, one each [meters] from its first vertex, and its
     * last vertex at the end, as `positionAtMeters` places them along it.
     */
    fun walked(
        line: List<List<Double>>,
        meters: Double = 10.0,
    ): List<List<Double>> {
        val length = routeLengthMeters(line)
        val positions = ArrayList<List<Double>>()
        var along = 0.0
        while (along < length) {
            positions += positionAtMeters(line, along)
            along += meters
        }
        positions += positionAtMeters(line, length)
        return positions
    }
}
