package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * searchSectionOf answers the section the branch [branch] of a round fills with [result], the Data API's answer to
 * its request, its rows built for the query the round asked, folded once into [query] for every row of every
 * branch, and worded by [wording] (`geo.search.live-round`, `geo.search.rows`).
 *
 * An error the answer reports for the branch wins over whatever came with it: the section reads it as a
 * [SearchBranchException], and the branches beside it are left as they are. Otherwise the section is settled with a
 * row for each result of the branch's own member of the answer that renders as one, in the order the answer lists
 * them, and none where the member is absent.
 */
internal fun searchSectionOf(
    result: DataSchema.SearchResult,
    branch: DataSchema.SearchTarget,
    query: SearchNeedle,
    wording: SearchWording,
): SearchSection {
    val reported = result.errors?.firstOrNull { it.target == branch }
    if (reported != null) {
        return SearchSection(
            branch,
            SearchSectionState.Failed(SearchBranchException(branch, reported.code, reported.message)),
        )
    }
    val rows: List<SearchRow> =
        when (branch) {
            DataSchema.SearchTarget.Places -> {
                result.places
                    ?.places
                    .orEmpty()
                    .mapNotNull { place -> placeRow(place, query, wording) }
            }

            DataSchema.SearchTarget.Pois -> {
                result.pois
                    ?.pois
                    .orEmpty()
                    .mapNotNull { poi -> poiRow(poi, query, wording) }
            }

            DataSchema.SearchTarget.Ways -> {
                result.ways
                    ?.ways
                    .orEmpty()
                    .mapNotNull { way -> wayRow(way, query, wording) }
            }

            DataSchema.SearchTarget.Itineraries -> {
                result.itineraries
                    ?.itineraries
                    .orEmpty()
                    .mapNotNull { itinerary -> itineraryRow(itinerary, query) }
            }

            DataSchema.SearchTarget.Collections -> {
                result.collections
                    ?.collections
                    .orEmpty()
                    .mapNotNull { collection -> collectionRow(collection, query) }
            }

            DataSchema.SearchTarget.Zones -> {
                result.zones
                    ?.zones
                    .orEmpty()
                    .mapNotNull { zone -> zoneRow(zone, query) }
            }

            else -> {
                emptyList()
            }
        }
    return SearchSection(branch, SearchSectionState.Settled, rows)
}
