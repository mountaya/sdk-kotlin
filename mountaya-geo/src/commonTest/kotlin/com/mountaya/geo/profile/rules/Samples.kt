package com.mountaya.geo

/** A series' samples at the distances and altitudes a test writes, each at the origin and numbered by its place. */
internal fun samples(vararg pairs: Pair<Double, Double>): List<ElevationPoint> =
    pairs.mapIndexed { index, (distance, elevation) -> ElevationPoint(distance, elevation, listOf(0.0, 0.0), index) }

/** A series' samples a hundred meters apart, at the altitudes a test writes. */
internal fun samplesEvery100Meters(vararg elevations: Double): List<ElevationPoint> =
    elevations.mapIndexed { index, elevation -> ElevationPoint(index * 100.0, elevation, listOf(0.0, 0.0), index) }
