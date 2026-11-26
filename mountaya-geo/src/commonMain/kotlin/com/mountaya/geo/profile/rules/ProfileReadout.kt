package com.mountaya.geo

/**
 * defaultReadoutStats is what a cursor readout states where its caller names nothing, the `defaultStats`
 * `geo/profile-cursor-readout.json` declares: where the cursor stands, then what it took to get there.
 */
public val defaultReadoutStats: List<ReadoutStat> =
    listOf(ReadoutStat.Distance, ReadoutStat.Elevation, ReadoutStat.Ascent, ReadoutStat.Descent)

/**
 * profileReadout is what a profile states about [point], the sample of [series] its cursor stands on, in the
 * order [stats] asks (`geo.profile.cursor-readout`): the sample's own distance and altitude, never a point read
 * between two samples; the ascent and the descent from the itinerary's start, read off the one cumulative scan
 * of the whole series ([rangeTotals]), so the figure and the last row of a table beside it are one number; the
 * grade across the window centred on it ([gradeAt]); and the stretch of [stretches] its distance reads
 * ([stretchAt]), the later of two at a meter they share. A figure with no value is left out.
 */
public fun profileReadout(
    series: ElevationSeries,
    point: ElevationPoint,
    stats: List<ReadoutStat> = defaultReadoutStats,
    stretches: List<ProfileClass> = emptyList(),
): ProfileReadout {
    val climbs = ReadoutStat.Ascent in stats || ReadoutStat.Descent in stats
    val reached = if (climbs) rangeTotals(series, 0.0, point.distance) else null
    val cells =
        stats.mapNotNull { stat ->
            val value =
                when (stat) {
                    ReadoutStat.Distance -> point.distance
                    ReadoutStat.Elevation -> point.elevation
                    ReadoutStat.Ascent -> reached?.ascent
                    ReadoutStat.Descent -> reached?.descent
                }
            value?.let { ReadoutCell(stat, it) }
        }
    return ProfileReadout(cells, gradeAt(series.points, point.distance), stretchAt(stretches, point.distance))
}
