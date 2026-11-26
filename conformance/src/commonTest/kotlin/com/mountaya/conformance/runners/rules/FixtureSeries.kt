package com.mountaya.conformance

import com.mountaya.geo.ElevationPoint
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.ProfileTotals

/**
 * The elevation series [value] writes, as `geo/elevation-series.json` answers one. Its
 * `hasElevation` is no field of its own here, since it is whether the series has a sample, and it is
 * held to that so a fixture that disagrees with itself fails rather than being read past.
 */
internal fun fixtureSeries(value: FixtureValue?): ElevationSeries {
    val series = value as? FixtureValue.Object ?: throw AssertionError("`series` is not an object")
    val points =
        series.items("points").map { item ->
            val point = item as? FixtureValue.Object ?: throw AssertionError("a point is not an object")
            ElevationPoint(
                point.number("distance"),
                point.number("elevation"),
                fixturePosition(point["lngLat"], "lngLat"),
                point.number("index").toInt(),
            )
        }
    val read =
        ElevationSeries(
            points,
            series.number("totalDistance"),
            series.numberOrNull("minElevation"),
            series.numberOrNull("maxElevation"),
        )
    val stated = series.bool("hasElevation")
    if (read.hasElevation != stated) throw AssertionError("`hasElevation` disagrees with the points the series holds")
    return read
}

/** [totals] in the shape the profile fixtures write totals in. */
internal fun totalsValue(totals: ProfileTotals): FixtureValue =
    FixtureValue.Object(
        listOf(
            "ascent" to numberOrNull(totals.ascent),
            "descent" to numberOrNull(totals.descent),
            "distance" to FixtureValue.Number(totals.distance),
            "elevationMax" to numberOrNull(totals.elevationMax),
            "elevationMin" to numberOrNull(totals.elevationMin),
            "gradeMax" to numberOrNull(totals.gradeMax),
        ),
    )
