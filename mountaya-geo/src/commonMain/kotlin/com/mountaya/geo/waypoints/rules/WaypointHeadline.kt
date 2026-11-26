package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/**
 * waypointHeadline is the line a card states under [waypoint]'s name: what its author wrote about
 * it, its description past the white space at its ends, or, where it has none or only white space,
 * what [waypointSummary] assembles from its record. A description always wins, even a terse one,
 * since an author who wrote one chose it over anything a summary could say.
 */
public fun waypointHeadline(
    waypoint: RestSchema.ItineraryWaypoint,
    words: WaypointSummaryWords,
    units: UnitPreferences,
    formatCutoffAt: ((Instant) -> String)? = null,
): String? {
    val description = waypoint.description?.let(::trimWhiteSpace).orEmpty()
    return description.ifEmpty { waypointSummary(waypoint, words, units, formatCutoffAt) }
}
