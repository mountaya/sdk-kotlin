package com.mountaya.geo

/**
 * sectionGrade is a leg's average grade, in degrees (`geo.profile.section-chips`): its climb less its loss, over
 * its length, as [metrics] state them. The average rather than the steepest meter, since a chip a few characters
 * wide is read as how hard a leg is; the itinerary's steepest is the stat strip's. Null where a figure is missing
 * or the leg has no length.
 */
public fun sectionGrade(metrics: ItinerarySegmentRowMetrics): Double? {
    val distance = metrics.sectionDistance ?: return null
    val ascent = metrics.sectionAscent ?: return null
    val descent = metrics.sectionDescent ?: return null
    if (!(distance > 0.0)) return null
    return convertSlope((ascent - descent) / distance, SlopeUnit.Ratio, SlopeUnit.Degrees)
}
