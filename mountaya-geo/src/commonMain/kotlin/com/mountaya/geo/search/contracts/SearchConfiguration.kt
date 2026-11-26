package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * searchSectionOrder is the order a search asks its branches in and renders their sections in, whatever order they
 * answer in (`geo.search.section-order`): the itineraries, the reader's own answers to where to go, then the
 * places, which a query most often names, and the ways after them, the road to a summit being the second reading
 * of its name, then the points of interest, the zones, and the collections last, a collection being a set of
 * itineraries rather than a destination.
 */
public val searchSectionOrder: List<DataSchema.SearchTarget> =
    listOf(
        DataSchema.SearchTarget.Itineraries,
        DataSchema.SearchTarget.Places,
        DataSchema.SearchTarget.Ways,
        DataSchema.SearchTarget.Pois,
        DataSchema.SearchTarget.Zones,
        DataSchema.SearchTarget.Collections,
    )

/**
 * SearchConfiguration is what a search asks its rounds with: how a request reaches the Data API, the language and
 * the cap a round asks with, the branches it asks, how a row is worded, and who hears a failed branch.
 *
 * A round asks one request per branch of [targets], in their order, each enabling its branch alone, and renders the
 * sections in that order (`geo.search.section-order`); a branch listed twice is asked once, and one this build
 * cannot enable is asked none. A change of the language, the cap, the branches, whether there is a transport, or
 * the language or the length system of the wording's units asks a fresh round (`geo.search.live-round`); a replaced
 * transport, word resolver or handler asks nothing, its words read as a branch lands.
 *
 * It is built whole, each member a named argument. Two are equal where they hold the same transport and handler,
 * each by identity, and the same everything else.
 */
public class SearchConfiguration(
    /**
     * How a request reaches the Data API, or null for a search that asks nothing and shows every section settled with
     * nothing.
     */
    public val fetch: SearchFetch? = null,
    /** The language a round asks the Data API to answer in, or null for the API's own default. */
    public val language: DataSchema.Language? = null,
    /** The cap a round asks each branch for, or null for the API's own default. */
    public val limit: SearchLimit? = null,
    /** The branches a round asks, in the order their sections render. */
    public val targets: List<DataSchema.SearchTarget> = searchSectionOrder,
    /** How a row's title and second line are worded for the reader. */
    public val wording: SearchWording = SearchWording(),
    /**
     * The handler told each failure the transport throws for a branch of the live round, after its section reads it,
     * or null for none. A branch the Data API answers as failed is no thrown failure, and its section alone reads it.
     */
    public val onError: ((Throwable) -> Unit)? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is SearchConfiguration &&
            other.fetch === fetch &&
            other.language == language &&
            other.limit == limit &&
            other.targets == targets &&
            other.wording == wording &&
            other.onError === onError

    override fun hashCode(): Int {
        var hash = fetch.hashCode()
        hash = 31 * hash + language.hashCode()
        hash = 31 * hash + limit.hashCode()
        hash = 31 * hash + targets.hashCode()
        hash = 31 * hash + wording.hashCode()
        hash = 31 * hash + onError.hashCode()
        return hash
    }

    override fun toString(): String =
        "SearchConfiguration(fetch=${fetch != null}, language=$language, limit=$limit, targets=$targets, " +
            "wording=$wording, onError=${onError != null})"
}
