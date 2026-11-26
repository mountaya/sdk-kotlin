package com.mountaya.geo

/**
 * searchNeedleOf folds [query] for matching once, as [highlightRanges] folds it, so a round that builds
 * many rows matches each title against one fold. The empty query, and one that folds to nothing such as
 * a lone accent, are the empty needle, which matches no run of any title.
 */
internal fun searchNeedleOf(query: String): SearchNeedle =
    SearchNeedle(if (query.isEmpty()) "" else foldForSearch(query).text)
