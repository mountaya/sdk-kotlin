package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * poiTileCategories is the Data API category each category of the points of interest's tiles reads
 * as, keyed by the tile's category as OpenMapTiles spells it, in small letters
 * (`geo.layers.poi-category`). Several tile categories read as one: a spring, a drinking water tap and
 * a well are each a water source, since the API's category says what a place is for.
 */
public val poiTileCategories: Map<String, DataSchema.POICategory> =
    mapOf(
        "alpine_hut" to DataSchema.POICategory.Hut,
        "cafe" to DataSchema.POICategory.Cafe,
        "camp_site" to DataSchema.POICategory.Campsite,
        "caravan_site" to DataSchema.POICategory.CaravanSite,
        "chalet" to DataSchema.POICategory.Accommodation,
        "drinking_water" to DataSchema.POICategory.WaterSource,
        "fast_food" to DataSchema.POICategory.Restaurant,
        "food_court" to DataSchema.POICategory.Restaurant,
        "guest_house" to DataSchema.POICategory.Accommodation,
        "hospital" to DataSchema.POICategory.Hospital,
        "hostel" to DataSchema.POICategory.Accommodation,
        "hotel" to DataSchema.POICategory.Accommodation,
        "parking" to DataSchema.POICategory.Parking,
        "pharmacy" to DataSchema.POICategory.Pharmacy,
        "restaurant" to DataSchema.POICategory.Restaurant,
        "shelter" to DataSchema.POICategory.Shelter,
        "spring" to DataSchema.POICategory.WaterSource,
        "toilets" to DataSchema.POICategory.Toilets,
        "viewpoint" to DataSchema.POICategory.Viewpoint,
        "water_well" to DataSchema.POICategory.WaterSource,
        "wilderness_hut" to DataSchema.POICategory.Hut,
    )

/**
 * poiCategoryOf reads the Data API category a point of interest of the tiles is, from the [category]
 * its feature carries: its entry in [poiTileCategories], or null where the table names none, so a click
 * on the map never asks the API about a category the reader did not point at.
 *
 * The category is read with its ASCII capitals folded and nothing else, never by a locale's case
 * rules, and matched exactly, nothing trimmed: `Restaurant` and `DRINKING_WATER` read as the table's,
 * while ` cafe`, a full-width `ＣＡＦＥ`, the empty category and `constructor`, which another
 * language's objects all inherit, read as none.
 */
public fun poiCategoryOf(category: String): DataSchema.POICategory? = poiTileCategories[asciiLowercase(category)]
