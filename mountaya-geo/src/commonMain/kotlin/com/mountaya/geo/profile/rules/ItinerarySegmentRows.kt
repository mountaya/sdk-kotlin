package com.mountaya.geo

/**
 * itinerarySegmentRows is an itinerary's [legs] as its table rows, in chain order (`geo.profile.segment-rows`),
 * the rows a segment table lists and a profile's section chips state.
 *
 * Each row spans the length of its leg's own lines on the distance axis ([profileLength]), from where the row
 * before it ended, held to the length of [series], and a leg with no geometry spans nothing. Its figures are the
 * ones [stated] for it by its id, field by field, over the leg's own, the server's, over those read off the one
 * cumulative scan of the whole series between its two ends ([rangeTotals]), so the last row's running climb is
 * the itinerary's; a leg spanning nothing derives none, since a figure of zero is one a reader believes. The
 * altitude band is the stated one over the derived; the duration and the difficulty are the stated ones, which
 * no geometry implies.
 *
 * The running `distance`, `ascent` and `descent` add each row's section figure to the row before's, null until a
 * figure adds to one, so an itinerary with no elevation states no climb; a stated running figure replaces the
 * running total and carries into the rows after it, so the rows after an official figure follow it rather than
 * drifting back.
 */
public fun itinerarySegmentRows(
    series: ElevationSeries,
    legs: List<ItineraryLeg>,
    stated: Map<String, ItinerarySegmentRowMetrics> = emptyMap(),
): List<ItinerarySegmentRow> {
    val rows = ArrayList<ItinerarySegmentRow>(legs.size)
    var from = 0.0
    var travelled = 0.0
    var climbed: Double? = null
    var dropped: Double? = null
    for (leg in legs) {
        val length = profileLength(leg.geometry)
        val to = minOf(series.totalDistance, from + length)
        val derived = if (length > 0.0) rangeTotals(series, from, to) else null
        val own = stated[leg.id]

        val sectionDistance = own?.sectionDistance ?: leg.sectionDistance ?: derived?.let { to - from }
        val sectionAscent = own?.sectionAscent ?: leg.sectionAscent?.toDouble() ?: derived?.ascent
        val sectionDescent = own?.sectionDescent ?: leg.sectionDescent?.toDouble() ?: derived?.descent

        travelled += sectionDistance ?: 0.0
        climbed = sectionAscent?.let { (climbed ?: 0.0) + it } ?: climbed
        dropped = sectionDescent?.let { (dropped ?: 0.0) + it } ?: dropped

        val metrics =
            ItinerarySegmentRowMetrics(
                distance = own?.distance ?: travelled,
                sectionDistance = sectionDistance,
                ascent = own?.ascent ?: climbed,
                descent = own?.descent ?: dropped,
                sectionAscent = sectionAscent,
                sectionDescent = sectionDescent,
                altitudeMin = own?.altitudeMin ?: leg.altitudeMin?.toDouble() ?: derived?.elevationMin,
                altitudeMax = own?.altitudeMax ?: leg.altitudeMax?.toDouble() ?: derived?.elevationMax,
                duration = own?.duration ?: leg.duration,
                difficulty = own?.difficulty,
            )
        travelled = metrics.distance ?: travelled
        climbed = metrics.ascent ?: climbed
        dropped = metrics.descent ?: dropped

        rows +=
            ItinerarySegmentRow(
                key = leg.id,
                id = leg.id,
                memberIds = leg.memberIds,
                name = leg.name,
                color = leg.color,
                index = rows.size,
                from = from,
                to = to,
                metrics = metrics,
            )
        from = to
    }
    return rows
}
