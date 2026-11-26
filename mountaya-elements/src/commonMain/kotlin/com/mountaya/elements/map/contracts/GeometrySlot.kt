package com.mountaya.elements

import com.mountaya.geo.LayerRank

/**
 * GeometrySlot is one of the three geometries a map draws from data over what it holds: the [Preview] of an
 * itinerary being planned, the [Highlight] of what a reader picked, and the [Emphasis] of the leg a reader
 * points at (`map.geometry.slots`), each named by [id].
 *
 * Each holds one geometry in a colour, apart from the others, so writing one never clears another. Each
 * draws from a source of its own as the itinerary's cased line, the highlight alone adding a circle for a
 * point (`map.geometry.layers`).
 */
public enum class GeometrySlot(
    /** The slot's name, as the contract spells it. */
    public val id: String,
) {
    /** An itinerary being planned, drawn just above the itinerary lines. */
    Preview("preview"),

    /** What a reader picked, a result from a list, drawn above every content band. */
    Highlight("highlight"),

    /** The leg a reader points at, drawn just above the itinerary lines. */
    Emphasis("emphasis"),
    ;

    /** The colour the slot draws in where a call names none. */
    internal val color: String
        get() =
            when (this) {
                Preview -> "#2b8a3e"
                Highlight -> "#f59f00"
                Emphasis -> "#e9501e"
            }

    /**
     * The band the slot draws at: the highlight at the chrome band, as what the reader is doing, and the
     * preview and the emphasis at the band just above the itinerary lines they accompany, so the order the
     * calls arrive in never stacks one under the route it lights.
     */
    internal val rank: Int
        get() =
            when (this) {
                Highlight -> LayerRank.Chrome.rank
                Preview, Emphasis -> LayerRank.ItineraryFocus.rank
            }

    /** The id of the source the slot draws from, `mountaya-` then its name. */
    internal val sourceId: String get() = "mountaya-$id"
}
