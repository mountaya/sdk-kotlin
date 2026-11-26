package com.mountaya.geo

/**
 * searchQueryChangeOf answers what [typed], the query in a search box once the reader's typing settles,
 * asks of a search whose last settled query is [settled], the empty string before any:
 *
 * - [SearchQueryChange.Unchanged] where [typed], trimmed of the white space at its ends, is
 *   [settled]: white space typed around a settled query asks nothing, nor does an empty box before
 *   any query;
 * - [SearchQueryChange.Clear] where it trims to nothing: the search settles empty, so the same query
 *   typed again after it asks again;
 * - [SearchQueryChange.Ask] otherwise, for the trimmed query, which is the round's identity and the
 *   query settled next.
 *
 * Letter case is part of the query: `Mont Blanc` asks again after `mont blanc`. The white space is
 * the one set ECMAScript's trim removes, in every SDK.
 */
public fun searchQueryChangeOf(
    settled: String,
    typed: String,
): SearchQueryChange {
    val query = trimWhiteSpace(typed)
    return when {
        query == settled -> SearchQueryChange.Unchanged
        query.isEmpty() -> SearchQueryChange.Clear
        else -> SearchQueryChange.Ask(query)
    }
}
