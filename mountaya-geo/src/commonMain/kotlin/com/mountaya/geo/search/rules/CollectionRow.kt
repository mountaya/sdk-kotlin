package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * collectionRow answers the row a [collection] renders as for [query]: identified `collection:` then
 * its id, titled by its name, its second line its count of itineraries for the surface to pluralize.
 * Picking it opens the collection with its own fields, so an editor shows them at once; one with no
 * description carries null, as the API sends it.
 *
 * Null where it carries no id.
 */
public fun collectionRow(
    collection: DataSchema.CollectionSearchResult,
    query: String,
): SearchRow? = collectionRow(collection, searchNeedleOf(query))

/** The row [collection] renders as for the query [needle] folds, which a round folds once for every row. */
internal fun collectionRow(
    collection: DataSchema.CollectionSearchResult,
    needle: SearchNeedle,
): SearchRow? {
    val id = collection.id ?: return null
    val title = collection.name.orEmpty()
    return SearchRow(
        id = "collection:$id",
        title = title,
        matched = highlightRanges(title, needle),
        secondary = SearchRowSecondary.Count(collection.count?.itineraries),
        activities = emptyList(),
        payload = SearchRowPayload.Collection(id, collection.name, collection.description, collection.isPrivate),
    )
}
