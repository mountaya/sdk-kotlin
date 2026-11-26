package com.mountaya.geo

/**
 * WaypointSummaryWords is the copy a place's one-line summary is assembled from, in the reader's
 * language: the word for a place that is equipped, and the clause each timing fact reads in. The
 * words are the caller's; only the assembly is the rule.
 *
 * Two are equal where they are the same words, the clauses compared as the functions they are.
 */
public class WaypointSummaryWords(
    /** The word that says a place is equipped, the one a card heads its services with: `Services`. */
    public val amenities: String,
    /** The clause of the opening hours, handed them as the place writes them: `open 8:00 to 18:00`. */
    public val openingHours: (hours: String) -> String,
    /** The clause of the cut-off, handed it as the caller's formatter spells it: `cut-off 09:00`. */
    public val cutoffAt: (moment: String) -> String,
    /** The clause of the planned stop, handed it as the duration formatter states it: `stop 15m`. */
    public val stop: (elapsed: String) -> String,
) {
    override fun equals(other: Any?): Boolean =
        other is WaypointSummaryWords &&
            other.amenities == amenities &&
            other.openingHours == openingHours &&
            other.cutoffAt == cutoffAt &&
            other.stop == stop

    override fun hashCode(): Int =
        ((amenities.hashCode() * 31 + openingHours.hashCode()) * 31 + cutoffAt.hashCode()) * 31 + stop.hashCode()

    override fun toString(): String = "WaypointSummaryWords(amenities=$amenities)"
}
