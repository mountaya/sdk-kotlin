package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * placeRow answers the row a geocoded [place] renders as for [query], the query the round asked:
 * identified `place:` then its OpenStreetMap type and id, titled by its name (else its address
 * label, else its category), emphasizing what [query] matched, worded by [wording], its second line
 * its category, region and country. Picking it moves the camera to its point and frames its bounds.
 *
 * Null, no row, where its point names no place, since picking it would go nowhere, and where it
 * carries no OpenStreetMap type and id, since a row with no identity cannot be keyed.
 */
public fun placeRow(
    place: DataSchema.Place,
    query: String,
    wording: SearchWording = SearchWording(),
): SearchRow? = placeRow(place, searchNeedleOf(query), wording)

/** The row [place] renders as for the query [needle] folds, which a round folds once for every row. */
internal fun placeRow(
    place: DataSchema.Place,
    needle: SearchNeedle,
    wording: SearchWording,
): SearchRow? {
    val point = placeOrNull(place.point) ?: return null
    val type = place.osm?.type ?: return null
    val id = place.osm?.id ?: return null
    val title = placeTitle(place, wording)
    return SearchRow(
        id = "place:${type.value}:$id",
        title = title,
        matched = highlightRanges(title, needle),
        secondary = textSecondary(describePlace(place, wording)),
        activities = emptyList(),
        payload = SearchRowPayload.Place(point, boxOrNull(place.bounds)),
    )
}
