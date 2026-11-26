package com.mountaya.elements

/**
 * StandingRefusals is the refusals of access that stand on a map, each by the source it came from, and
 * whether one naming no source stands (`map.events.access-standing`). Two are equal where both are.
 */
internal class StandingRefusals(
    /** The id of every source whose refusal stands, each once, in the order refused. */
    val sources: List<String> = emptyList(),
    /** Whether a refusal naming no source stands: of a style, a sprite, a glyph or the layers manifest. */
    val unsourced: Boolean = false,
) {
    /** Whether no refusal stands. */
    val isEmpty: Boolean get() = sources.isEmpty() && !unsourced

    override fun equals(other: Any?): Boolean =
        other is StandingRefusals && other.sources == sources && other.unsourced == unsourced

    override fun hashCode(): Int = sources.hashCode() * 31 + unsourced.hashCode()

    override fun toString(): String = "StandingRefusals(sources=$sources, unsourced=$unsourced)"
}
