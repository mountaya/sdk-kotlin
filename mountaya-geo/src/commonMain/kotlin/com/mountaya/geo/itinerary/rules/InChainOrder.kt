package com.mountaya.geo

/**
 * inChainOrder answers the [entries] of a chain in the order the chain runs, by the ordinal [position]
 * reads off each, two at one position in the order they are listed, since Kotlin's sort is stable.
 *
 * An itinerary's segments and its waypoints each carry such a position, and a chain held after a local
 * edit may arrive in another order: a reader that walked it as it came would draw it out of order.
 */
internal fun <Entry> inChainOrder(
    entries: List<Entry>,
    position: (Entry) -> Int,
): List<Entry> = entries.sortedBy(position)
