package com.mountaya.geo

/**
 * searchListState answers what a result list shows of [snapshot] under [filter] (`geo.search.list-state`).
 *
 * Its rows are those of the sections the filter shows, in the order they render (`sectionsForFilter`), and each of
 * those sections whose branch failed has a line of its own, beside which a list offers a retry. It says nothing was
 * found only once a round has settled with none in flight, no row under the filter and no failed branch under it:
 * a failure is no empty answer, and a round falling back from the visible area is still in flight. It notes the
 * fall back whenever the round fell back. Its status line announces the failure, else the emptiness, else, once no
 * round is in flight, how many rows it shows, and nothing while a round runs with no failure to announce.
 */
public fun searchListState(
    snapshot: SearchSnapshot,
    filter: SearchFilter,
): SearchListState {
    val sections = snapshot.sections.filter { it.id in filter.targets }
    val rows = sections.flatMap { it.rows }
    val failed = sections.filter { it.state.failure != null }
    val isNothingFound = snapshot.isSettled && !snapshot.isSearching && rows.isEmpty() && failed.isEmpty()
    val status =
        when {
            failed.isNotEmpty() -> SearchListStatus.Error
            isNothingFound -> SearchListStatus.EmptyTitle
            rows.isNotEmpty() && !snapshot.isSearching -> SearchListStatus.ResultCount(rows.size)
            else -> null
        }
    return SearchListState(rows, failed, isNothingFound, snapshot.hasFallenBack, status)
}
