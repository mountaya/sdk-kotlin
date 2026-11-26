package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/** A geocoded place's category as the reader reads it. */
internal fun placeCategoryWord(
    place: DataSchema.Place,
    wording: SearchWording,
): String {
    val category = place.category ?: return ""
    return wordFor(SearchLabel.PlaceCategory(category), titleCase(category.value.lowercase()), wording)
}

/** A point of interest's category as the reader reads it. */
internal fun poiCategoryWord(
    poi: DataSchema.POI,
    wording: SearchWording,
): String {
    val category = poi.category ?: return ""
    return wordFor(SearchLabel.PoiCategory(category), titleCase(category.value.lowercase()), wording)
}

/** A way's `highway` tag as the reader reads it. */
internal fun highwayWord(
    way: DataSchema.Way,
    wording: SearchWording,
): String {
    val highway = way.highway ?: return ""
    return wordFor(SearchLabel.Highway(highway), titleCase(highway), wording)
}

/**
 * A geocoded place's title: its name, else the geocoder's address label for an address with no name,
 * else its category, so no row is titled with nothing.
 */
internal fun placeTitle(
    place: DataSchema.Place,
    wording: SearchWording,
): String {
    val name = place.name.orEmpty()
    if (name.isNotEmpty()) return name
    val label = place.label.orEmpty()
    if (place.category == DataSchema.PlaceCategory.Address && label.isNotEmpty()) return label
    return placeCategoryWord(place, wording)
}

/** A point of interest's title: its name, else its category, since many carry no name. */
internal fun poiTitle(
    poi: DataSchema.POI,
    wording: SearchWording,
): String = poi.name.orEmpty().ifEmpty { poiCategoryWord(poi, wording) }

/** A way's title: its name, else its `highway` tag, since trails are often unnamed. */
internal fun wayTitle(
    way: DataSchema.Way,
    wording: SearchWording,
): String = way.name.orEmpty().ifEmpty { highwayWord(way, wording) }
