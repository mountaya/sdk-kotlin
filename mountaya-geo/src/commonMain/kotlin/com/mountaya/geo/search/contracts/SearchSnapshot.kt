package com.mountaya.geo

/**
 * SearchSnapshot is a search as a list shows it at one moment: the query its rows answer, whether a round is in
 * flight, whether a round has settled, whether the round fell back from the visible area to everywhere, and its
 * sections, in the order they render.
 *
 * [query] is the query trimmed as a round asks it. [isSettled] is whether a round has settled with none in flight: a
 * round confined to the visible area that found nothing asks once more everywhere, and between its two passes every
 * section stands settled and empty, which is no answer yet, so a list says nothing was found only once [isSettled]
 * holds (`geo.search.broaden`, `geo.search.list-state`).
 *
 * Two are equal where every member is, a failed section's failure being the very same failure, so a state flow of
 * snapshots reports only one that differs.
 */
public class SearchSnapshot(
    /** The query the rows answer, trimmed of the white space at its ends. */
    public val query: String,
    /** Whether a round is in flight. */
    public val isSearching: Boolean = false,
    /** Whether a round has settled and none is in flight. */
    public val isSettled: Boolean = false,
    /** Whether the round fell back from the visible area to everywhere, having found nothing within it. */
    public val hasFallenBack: Boolean = false,
    /** The search's sections, one per branch, in the order they render. */
    public val sections: List<SearchSection> = emptyList(),
) {
    override fun equals(other: Any?): Boolean =
        other is SearchSnapshot &&
            other.query == query &&
            other.isSearching == isSearching &&
            other.isSettled == isSettled &&
            other.hasFallenBack == hasFallenBack &&
            other.sections == sections

    override fun hashCode(): Int {
        var hash = query.hashCode()
        hash = 31 * hash + isSearching.hashCode()
        hash = 31 * hash + isSettled.hashCode()
        hash = 31 * hash + hasFallenBack.hashCode()
        hash = 31 * hash + sections.hashCode()
        return hash
    }

    override fun toString(): String =
        "SearchSnapshot($query, isSearching=$isSearching, isSettled=$isSettled, hasFallenBack=$hasFallenBack, " +
            "sections=$sections)"
}
