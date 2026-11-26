package com.mountaya.geo

/**
 * curveSpans is [stretches] in distance order with no overlap, the one reading the gradient a curve is
 * painted with and every lookup of a distance share: each read in order, one of no length left out, and
 * where two would share ground the earlier keeps it, the later starting where the earlier ends. A stretch
 * keeps its own members through it, so a classified stretch keeps its word and a table's row its own.
 *
 * Stretches that start at one distance keep the order they are given in.
 */
internal fun <Span : ProfileSpan<Span>> curveSpans(stretches: List<Span>): List<Span> {
    val ordered =
        stretches
            .mapNotNull { stretch ->
                val from = minOf(stretch.from, stretch.to)
                val to = maxOf(stretch.from, stretch.to)
                if (to > from) stretch.spanning(from, to) else null
            }
            // A stable sort: two that start at one distance keep the order they are given in.
            .sortedWith { a, b ->
                if (a.from == b.from) {
                    0
                } else if (a.from < b.from) {
                    -1
                } else {
                    1
                }
            }
    val resolved = ArrayList<Span>(ordered.size)
    var cursor = Double.NEGATIVE_INFINITY
    for (span in ordered) {
        val from = maxOf(span.from, cursor)
        if (span.to <= from) continue
        resolved += span.spanning(from, span.to)
        cursor = span.to
    }
    return resolved
}
