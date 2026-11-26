package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry

/**
 * itineraryWaypointRows is an itinerary's [places] as its waypoint rows, one per pass of a place in itinerary
 * order (`geo.profile.waypoint-rows`), each placed on [geometry] as [resolveWaypoints] places it, so a loop
 * passing a place twice lists it twice and a loop's start and finish stand at zero and at the total with no
 * radius to tune.
 *
 * Each row is reached from the row before, the first from the itinerary's start: its `from` is where the row
 * before stands and its `to` where its place stands. Its altitude is the curve's at its distance, read on the
 * line between the two samples either side; its section figures and its running climb and loss are read off the
 * one cumulative scan of the whole [series] ([rangeTotals]), so the last row's running climb is the itinerary's.
 *
 * A place that divides the itinerary states its `metrics` over the derived figures, field by field, on its
 * primary pass alone, the one the order its places are listed in gives it, so an official running figure resets
 * the totals once. From the first row that states one, each running total is the row before's plus this row's
 * section, a stated running figure replacing it; the rows before read the scan digit for digit. Every placed pass
 * is a row, dividing or not.
 */
public fun itineraryWaypointRows(
    series: ElevationSeries,
    geometry: GeoJsonGeometry?,
    places: List<ProfilePlace>,
): List<ItineraryWaypointRow> {
    var previous = 0.0
    var runningAscent: Double? = null
    var runningDescent: Double? = null
    var runningDistance = 0.0
    var follows = false
    return waypointPasses(geometry, places.map { it.placement }).mapIndexed { index, pass ->
        val place = places[pass.listed]
        val to = pass.distance
        val from = previous
        previous = to
        val reaching = rangeTotals(series, from, to)
        val fromStart = rangeTotals(series, 0.0, to)
        val stated = if (pass.isPrimary && place.dividesRoute) place.metrics else null
        follows = follows || statesRowFigure(stated)

        val sectionDistance = stated?.sectionDistance ?: reaching.distance
        val sectionAscent = stated?.sectionAscent ?: reaching.ascent
        val sectionDescent = stated?.sectionDescent ?: reaching.descent
        if (follows) {
            runningAscent = stated?.ascent ?: carried(runningAscent, sectionAscent)
            runningDescent = stated?.descent ?: carried(runningDescent, sectionDescent)
            runningDistance = stated?.distance ?: (runningDistance + sectionDistance)
        } else {
            runningAscent = fromStart.ascent
            runningDescent = fromStart.descent
            runningDistance = to
        }
        ItineraryWaypointRow(
            key = pass.key,
            place = place,
            index = index,
            from = from,
            to = to,
            metrics =
                ItineraryWaypointRowMetrics(
                    distance = runningDistance,
                    sectionDistance = sectionDistance,
                    ascent = runningAscent,
                    descent = runningDescent,
                    sectionAscent = sectionAscent,
                    sectionDescent = sectionDescent,
                    altitude = elevationAt(series.points, to),
                ),
        )
    }
}

/** Whether [stated] holds a figure a waypoint row has a cell for: a section's distance, climb or loss, or a running one. */
private fun statesRowFigure(stated: ItinerarySegmentRowMetrics?): Boolean {
    if (stated == null) return false
    return listOf(
        stated.ascent,
        stated.descent,
        stated.distance,
        stated.sectionAscent,
        stated.sectionDescent,
        stated.sectionDistance,
    ).any { it != null }
}

/** A running [total] with a [section]'s figure added, an absent figure leaving it as it is. */
private fun carried(
    total: Double?,
    section: Double?,
): Double? = if (section == null) total else (total ?: 0.0) + section
