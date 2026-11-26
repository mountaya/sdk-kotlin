package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchFilter is which part of a search's results a reader asks to see, in the order a surface
 * offers them: everything, then one filter per kind of thing a reader looks for. Each narrowing
 * filter shows the sections its reader means by it, and every section is under exactly one of them.
 */
public enum class SearchFilter(
    /** The filter's id, as a surface and the contract name it. */
    public val id: String,
    /** The search targets whose sections the filter shows. */
    public val targets: Set<DataSchema.SearchTarget>,
) {
    /** Every section. */
    All(
        "all",
        setOf(
            DataSchema.SearchTarget.Places,
            DataSchema.SearchTarget.Pois,
            DataSchema.SearchTarget.Ways,
            DataSchema.SearchTarget.Itineraries,
            DataSchema.SearchTarget.Collections,
            DataSchema.SearchTarget.Zones,
        ),
    ),

    /** The itineraries and the collections, a collection being a set of itineraries. */
    Itineraries("itineraries", setOf(DataSchema.SearchTarget.Itineraries, DataSchema.SearchTarget.Collections)),

    /** The places, the points of interest and the named ways, one idea of a place. */
    Places("places", setOf(DataSchema.SearchTarget.Places, DataSchema.SearchTarget.Pois, DataSchema.SearchTarget.Ways)),

    /** The zones. */
    Zones("zones", setOf(DataSchema.SearchTarget.Zones)),
}
