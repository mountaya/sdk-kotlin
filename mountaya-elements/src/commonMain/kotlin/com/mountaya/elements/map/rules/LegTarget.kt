package com.mountaya.elements

import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.lineBetween
import com.mountaya.geo.nearestSampleIndex

/**
 * legTarget is what pointing a segment table's row writes to its map (`map.profile.table-rows`): the cursor marker at
 * the sample of [series] nearest the end of its leg, [to], the earlier of two as near, the place a roadbook's row is
 * about arriving at, and the leg lit on the emphasis slot as the series cut between its two ends, [from] and [to]
 * (`lineBetween`), so a row standing for several legs lights them all.
 */
internal fun legTarget(
    series: ElevationSeries,
    from: Double,
    to: Double,
): MapTarget =
    MapTarget(
        nearestSampleIndex(series.points, to)?.let { series.points[it].lngLat },
        lineBetween(series.points, from, to),
    )

/**
 * passTarget is what pointing a waypoint table's row writes to its map (`map.profile.table-rows`): the cursor marker at
 * the sample of [series] nearest where the itinerary passes the place, [at], the row being the place itself, and no
 * stretch lit.
 */
internal fun passTarget(
    series: ElevationSeries,
    at: Double,
): MapTarget = MapTarget(nearestSampleIndex(series.points, at)?.let { series.points[it].lngLat })
