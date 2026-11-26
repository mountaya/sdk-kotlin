package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * zoneRow answers the row a [zone] renders as for [query]: identified `zone:` then its id, titled by
 * its name, its second line its own description where it has one. Picking it opens the zone, at its
 * anchor and framed on its extent where the search answered them.
 *
 * Null where it carries no id.
 */
public fun zoneRow(
    zone: DataSchema.ZoneSearchResult,
    query: String,
): SearchRow? = zoneRow(zone, searchNeedleOf(query))

/** The row [zone] renders as for the query [needle] folds, which a round folds once for every row. */
internal fun zoneRow(
    zone: DataSchema.ZoneSearchResult,
    needle: SearchNeedle,
): SearchRow? {
    val id = zone.id ?: return null
    val title = zone.name.orEmpty()
    return SearchRow(
        id = "zone:$id",
        title = title,
        matched = highlightRanges(title, needle),
        secondary = textSecondary(zone.description),
        activities = emptyList(),
        payload = SearchRowPayload.Zone(id, placeOrNull(zone.point), boxOrNull(zone.bounds)),
    )
}
