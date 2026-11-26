package com.mountaya.geo

/**
 * SearchQueryChange is what a query a reader typed asks of a search, after the query it last
 * settled on: a round for the query as it is asked, a search cleared, or nothing.
 */
public sealed interface SearchQueryChange {
    /** Ask is a round to ask, for [query], the typed query trimmed of the white space at its ends. */
    public class Ask(
        /** The query the round sends, which is the round's identity. */
        public val query: String,
    ) : SearchQueryChange {
        override fun equals(other: Any?): Boolean = other is Ask && other.query == query

        override fun hashCode(): Int = query.hashCode()

        override fun toString(): String = "SearchQueryChange.Ask($query)"
    }

    /** Clear settles an empty search and asks nothing: the query trims to nothing. */
    public object Clear : SearchQueryChange {
        override fun toString(): String = "SearchQueryChange.Clear"
    }

    /** Unchanged asks nothing: the query trims to the one already settled. */
    public object Unchanged : SearchQueryChange {
        override fun toString(): String = "SearchQueryChange.Unchanged"
    }
}
