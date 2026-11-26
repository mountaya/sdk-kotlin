package com.mountaya.geo

/**
 * assignDistinctColors answers [count] colours of [palette] for a batch shown side by side, such as
 * a set of suggested itineraries, each another than every colour [reserved] names, and spread as
 * widely around the wheel as the colours left allow.
 *
 * The colours left are the palette's, in its order, but those [reserved] names, compared without
 * regard to the case of their letters; where none is left, the whole palette. The answer takes every
 * `stride`-th of them, the stride the colours left over [count] rounded down and at least one, and
 * wraps past the last: over the twelve of [livelyPalette], three are the first, the fifth and the
 * ninth. It is deterministic, so a batch regenerated with some of its colours kept assigns the
 * others the same way every time.
 *
 * Exactly [count] colours, repeating once the colours left are spent; none for a count under one.
 * A palette that lists no colour reads as [livelyPalette].
 */
public fun assignDistinctColors(
    count: Int,
    reserved: List<String>,
    palette: List<String> = livelyPalette,
): List<String> {
    if (count <= 0) return emptyList()
    val listed = listedOr(palette)
    val taken = reserved.mapTo(HashSet()) { asciiLowercase(it) }
    val pool = listed.filterNot { asciiLowercase(it) in taken }.ifEmpty { listed }
    val stride = maxOf(1, pool.size / count)
    return List(count) { slot -> colorAt(pool, (slot.toLong() * stride % pool.size).toInt()) }
}
