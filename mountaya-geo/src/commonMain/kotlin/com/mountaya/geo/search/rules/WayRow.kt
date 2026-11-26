package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * wayRow answers the row a named [way] renders as for [query]: identified `way:` then its
 * OpenStreetMap type and id, titled by its name else its `highway` tag, its second line its tag, its
 * grade or surface, its city or region and country, and advertising the activities it suits. Picking
 * it moves the camera to its anchor, frames its bounds and traces its line where the search verified
 * one.
 *
 * Null where its point names no place, or where it carries no OpenStreetMap type and id.
 */
public fun wayRow(
    way: DataSchema.Way,
    query: String,
    wording: SearchWording = SearchWording(),
): SearchRow? = wayRow(way, searchNeedleOf(query), wording)

/** The row [way] renders as for the query [needle] folds, which a round folds once for every row. */
internal fun wayRow(
    way: DataSchema.Way,
    needle: SearchNeedle,
    wording: SearchWording,
): SearchRow? {
    val anchor = placeOrNull(way.point) ?: return null
    val type = way.osm?.type ?: return null
    val id = way.osm?.id ?: return null
    val title = wayTitle(way, wording)
    return SearchRow(
        id = "way:${type.value}:$id",
        title = title,
        matched = highlightRanges(title, needle),
        secondary = textSecondary(describeWay(way, wording)),
        activities = way.activityCompatible.orEmpty(),
        payload = SearchRowPayload.Way(anchor, boxOrNull(way.bounds), way.coordinates),
    )
}
