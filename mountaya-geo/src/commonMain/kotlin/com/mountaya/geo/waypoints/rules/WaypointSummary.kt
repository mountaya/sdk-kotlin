package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/** What parts the summary's two clauses: a middle dot between two spaces. */
private const val SUMMARY_SEPARATOR = " · "

/**
 * waypointSummary is what [waypoint] says about itself in one line when its author wrote no
 * description: two clauses at most, whether it is equipped and when it can be used, joined by a
 * middle dot between two spaces, `Services · stop 15m`. The services themselves are repeated as
 * badges directly beneath, so the line names none of them.
 *
 * The first clause is [WaypointSummaryWords.amenities] where the place lists any service. The second
 * is one timing fact: its opening hours where its contact states some, else its cut-off where
 * [formatCutoffAt] can spell one, else its planned stop as [formatDuration] states it for [units],
 * where it is one minute or more. A cut-off is an instant only the caller knows the zone to print in,
 * so the caller supplies its formatter as it supplies the words; with none, the clause falls through
 * to the stop. Null where neither clause has anything to say.
 */
public fun waypointSummary(
    waypoint: RestSchema.ItineraryWaypoint,
    words: WaypointSummaryWords,
    units: UnitPreferences,
    formatCutoffAt: ((Instant) -> String)? = null,
): String? {
    val clauses = ArrayList<String>(2)
    if (waypoint.amenities.isNotEmpty()) clauses += words.amenities
    timingClause(waypoint, words, units, formatCutoffAt)?.let { clauses += it }
    return if (clauses.isEmpty()) null else clauses.joinToString(SUMMARY_SEPARATOR)
}

/** The one timing fact the summary states: the opening hours, else the cut-off, else the stop. */
private fun timingClause(
    waypoint: RestSchema.ItineraryWaypoint,
    words: WaypointSummaryWords,
    units: UnitPreferences,
    formatCutoffAt: ((Instant) -> String)?,
): String? {
    val openingHours = waypoint.contact?.opening_hours
    if (!openingHours.isNullOrEmpty()) return words.openingHours(openingHours)
    val cutoffAt = waypoint.timing?.cutoff_at
    if (cutoffAt != null && formatCutoffAt != null) return words.cutoffAt(formatCutoffAt(cutoffAt))
    // A stop that rounds to no minute has no spelling, and a clause around none is a bare word.
    val stop = formatDuration(waypoint.timing?.stop_duration?.toDouble(), units)
    return if (stop.isEmpty()) null else words.stop(stop)
}
