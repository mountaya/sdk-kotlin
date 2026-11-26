package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * poiRow answers the row a point of interest [poi] renders as for [query]: identified `poi:` then
 * its OpenStreetMap type and id, titled by its name else its category, its second line its category,
 * then its elevation where it is meaningful and otherwise its distance from the search's origin, then
 * its city or region and country. Its category rides to the payload, where it picks the pin.
 *
 * Null where its point names no place, or where it carries no OpenStreetMap type and id.
 */
public fun poiRow(
    poi: DataSchema.POI,
    query: String,
    wording: SearchWording = SearchWording(),
): SearchRow? = poiRow(poi, searchNeedleOf(query), wording)

/** The row [poi] renders as for the query [needle] folds, which a round folds once for every row. */
internal fun poiRow(
    poi: DataSchema.POI,
    needle: SearchNeedle,
    wording: SearchWording,
): SearchRow? {
    val point = placeOrNull(poi.point) ?: return null
    val type = poi.osm?.type ?: return null
    val id = poi.osm?.id ?: return null
    val title = poiTitle(poi, wording)
    return SearchRow(
        id = "poi:${type.value}:$id",
        title = title,
        matched = highlightRanges(title, needle),
        secondary = textSecondary(describePoi(poi, wording)),
        activities = emptyList(),
        payload = SearchRowPayload.Poi(point, poi.category),
    )
}
