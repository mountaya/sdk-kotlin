package com.mountaya.geo

/**
 * highlightParts answers [title] as the parts a search result draws it in once [query] is matched
 * against it ([highlightRanges]): each run the query matched, and each run between them, as the title
 * writes it, in order, so the parts joined are the title. A title with no match, and an empty query,
 * are one unmatched part holding the whole title.
 */
public fun highlightParts(
    title: String,
    query: String,
): List<HighlightPart> = partsOf(title, highlightRanges(title, query))

/** [title] split at [ranges], sorted and disjoint, into matched and unmatched parts. */
internal fun partsOf(
    title: String,
    ranges: List<IntRange>,
): List<HighlightPart> {
    if (ranges.isEmpty()) return listOf(HighlightPart(title, match = false))
    val parts = mutableListOf<HighlightPart>()
    var cursor = 0
    for (range in ranges) {
        if (range.first > cursor) parts += HighlightPart(title.substring(cursor, range.first), match = false)
        parts += HighlightPart(title.substring(range), match = true)
        cursor = range.last + 1
    }
    if (cursor < title.length) parts += HighlightPart(title.substring(cursor), match = false)
    return parts
}
