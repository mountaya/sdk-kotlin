package com.mountaya.geo

/**
 * SearchListState is what a result list shows of a search under a filter, before any of it is worded or drawn
 * (`geo.search.list-state`): its rows, its failed branches, whether it says nothing was found, whether it notes the
 * round fell back from the visible area, and what its status line announces.
 *
 * Two are equal where every member is.
 */
public class SearchListState(
    /** The rows of the sections the filter shows, in the order they render. */
    public val rows: List<SearchRow>,
    /**
     * The sections under the filter whose branch failed, in the order they render, each a line of its own beside a
     * retry.
     */
    public val failed: List<SearchSection>,
    /** Whether the list says nothing was found. */
    public val isNothingFound: Boolean,
    /** Whether the list notes that the round fell back from the visible area to everywhere. */
    public val hasFallbackNote: Boolean,
    /** What the status line announces, or null where it announces nothing. */
    public val status: SearchListStatus?,
) {
    override fun equals(other: Any?): Boolean =
        other is SearchListState &&
            other.rows == rows &&
            other.failed == failed &&
            other.isNothingFound == isNothingFound &&
            other.hasFallbackNote == hasFallbackNote &&
            other.status == status

    override fun hashCode(): Int {
        var hash = rows.hashCode()
        hash = 31 * hash + failed.hashCode()
        hash = 31 * hash + isNothingFound.hashCode()
        hash = 31 * hash + hasFallbackNote.hashCode()
        hash = 31 * hash + status.hashCode()
        return hash
    }

    override fun toString(): String =
        "SearchListState(rows=${rows.map { it.title }}, failed=${failed.map { it.id }}, " +
            "isNothingFound=$isNothingFound, hasFallbackNote=$hasFallbackNote, status=$status)"
}
