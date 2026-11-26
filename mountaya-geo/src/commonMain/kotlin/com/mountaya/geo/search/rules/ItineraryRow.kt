package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * itineraryRow answers the row an [itinerary] renders as for [query]: identified `itinerary:` then
 * its id, titled by its name, its second line its distance and ascent for the surface to state in
 * its reader's units, advertising its activity. Picking it opens the itinerary, framed on its extent
 * where the search answered one. An itinerary is addressed by its id, so a missing extent costs the
 * framing and nothing else.
 *
 * Null where it carries no id.
 */
public fun itineraryRow(
    itinerary: DataSchema.ItinerarySearchResult,
    query: String,
): SearchRow? = itineraryRow(itinerary, searchNeedleOf(query))

/** The row [itinerary] renders as for the query [needle] folds, which a round folds once for every row. */
internal fun itineraryRow(
    itinerary: DataSchema.ItinerarySearchResult,
    needle: SearchNeedle,
): SearchRow? {
    val id = itinerary.id ?: return null
    val title = itinerary.name.orEmpty()
    return SearchRow(
        id = "itinerary:$id",
        title = title,
        matched = highlightRanges(title, needle),
        secondary = SearchRowSecondary.Metric(itinerary.distance, itinerary.ascent),
        activities = listOfNotNull(itinerary.activitySlug?.takeIf { it.value.isNotEmpty() }),
        payload = SearchRowPayload.Itinerary(id, boxOrNull(itinerary.bounds), placeOrNull(itinerary.start)),
    )
}
