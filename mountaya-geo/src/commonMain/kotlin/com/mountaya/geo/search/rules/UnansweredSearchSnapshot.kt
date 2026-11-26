package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * unansweredSearchSnapshot answers what the query [query] in a search box reads as while the search has not answered
 * it (`geo.search.typing-pause`): the skeleton of the round it is owed, every section of [targets] loading and a
 * round in flight, where the search is live, [isLive], holding a transport, and the query trims to something; and
 * every section settled with nothing otherwise, a blank query, or one with no transport to ask it through, owing no
 * round. Either way no round is settled and no fall back is noted, and it names the query trimmed as a round asks it.
 */
internal fun unansweredSearchSnapshot(
    query: String,
    isLive: Boolean,
    targets: List<DataSchema.SearchTarget>,
): SearchSnapshot {
    val asked = trimWhiteSpace(query)
    val asks = isLive && asked.isNotEmpty()
    val state = if (asks) SearchSectionState.Loading else SearchSectionState.Settled
    return SearchSnapshot(asked, isSearching = asks, sections = targets.map { SearchSection(it, state) })
}
