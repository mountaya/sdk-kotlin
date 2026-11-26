package com.mountaya.geo

/**
 * highlightRanges answers every run of [title] that [query] matches without regard to case or
 * accents, left to right and never overlapping, each a range of indexes into [title] as written: the
 * UTF-16 indexes a Kotlin string and a Compose `AnnotatedString` count in.
 *
 * Both are folded alike, one segment at a time: a code point with its combining marks and what
 * composes with it, lowercased, stripped of the accents a canonical decomposition leaves (U+0300 to
 * U+036F), recomposed, a final sigma read as a sigma, a sharp s as `ss` and a Latin letter with a
 * stroke (đ, ħ, ł, ø, ŧ, small or capital) as the letter under the stroke, on either side: `evian`
 * matches `Évian`, `strasse` matches `Straße` and `lodz` matches `Łódź`. A match is found in the
 * folded title and mapped back onto whole segments of the title as written, so a fold that changes a
 * string's length never moves the emphasis (`stan` in `İstanbul` is `stan`), and a run takes a sharp
 * s whole (`stras` in `Straße` is `Straß`). Two matches inside one segment's fold are one run.
 *
 * A run is made of code points with their marks and never of extended grapheme clusters, so it may
 * end inside an emoji sequence, between the two regional indicators of a flag or inside an Indic
 * conjunct. A mark that is no accent (an Arabic vowel, a Hebrew point) is kept, so an unvowelled word
 * matches no vowelled spelling of it. None for an empty title, an empty query, or a query that folds
 * to nothing, such as a lone accent.
 */
public fun highlightRanges(
    title: String,
    query: String,
): List<IntRange> {
    if (title.isEmpty() || query.isEmpty()) return emptyList()
    return highlightRanges(title, searchNeedleOf(query))
}

/**
 * Every run of [title] the query [folded] holds the fold of matches, as [highlightRanges] over the query
 * answers them: the form a round reads every row's title by, its query folded once.
 */
internal fun highlightRanges(
    title: String,
    folded: SearchNeedle,
): List<IntRange> {
    val needle = folded.text
    if (title.isEmpty() || needle.isEmpty()) return emptyList()
    val haystack = foldForSearch(title)
    val ranges = mutableListOf<IntArray>()
    var from = 0
    while (from <= haystack.text.length - needle.length) {
        val at = haystack.text.indexOf(needle, from)
        if (at < 0) break
        val start = haystack.starts[at]
        val end = haystack.ends[at + needle.length - 1]
        val previous = ranges.lastOrNull()
        if (previous != null && start < previous[1]) {
            previous[1] = maxOf(previous[1], end)
        } else {
            ranges += intArrayOf(start, end)
        }
        from = at + needle.length
    }
    return ranges.map { it[0] until it[1] }
}
