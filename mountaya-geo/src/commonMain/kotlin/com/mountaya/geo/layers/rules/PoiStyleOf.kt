package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * poiCategoryStyles is how a point of interest of each category the Data API declares is drawn, the
 * table the points of interest's tiles are painted with, so a search result's pin lies on the tile's
 * own in the same glyph and colour (`geo.layers.poi-style`).
 *
 * It names every category the contract declares. `DataSchema.POICategory` is open, so a category the
 * vocabulary gains later decodes as itself, and has no entry until this table names it.
 */
public val poiCategoryStyles: Map<DataSchema.POICategory, PoiStyle> =
    mapOf(
        DataSchema.POICategory.Accommodation to PoiStyle("apartment-building", HexColor("#7b1fa2")),
        DataSchema.POICategory.Airport to PoiStyle("airport", HexColor("#455a64")),
        DataSchema.POICategory.BusStation to PoiStyle("bus", HexColor("#455a64")),
        DataSchema.POICategory.CableCar to PoiStyle("tram", HexColor("#455a64")),
        DataSchema.POICategory.Cafe to PoiStyle("cutlery", HexColor("#e53935")),
        DataSchema.POICategory.Campsite to PoiStyle("camping", HexColor("#2e7d32")),
        DataSchema.POICategory.CaravanSite to PoiStyle("camper-van", HexColor("#6d4c41")),
        DataSchema.POICategory.Hospital to PoiStyle("medical-cross", HexColor("#d81b60")),
        DataSchema.POICategory.Hut to PoiStyle("hut", HexColor("#ff9800")),
        DataSchema.POICategory.Parking to PoiStyle("parking-sign", HexColor("#1565c0")),
        DataSchema.POICategory.Pass to PoiStyle("pass", HexColor("#795548")),
        DataSchema.POICategory.Peak to PoiStyle("mountain", HexColor("#5d4037")),
        DataSchema.POICategory.Pharmacy to PoiStyle("medical-cross", HexColor("#4caf50")),
        DataSchema.POICategory.Restaurant to PoiStyle("cutlery", HexColor("#e53935")),
        DataSchema.POICategory.Shelter to PoiStyle("campground", HexColor("#ff9800")),
        DataSchema.POICategory.Supermarket to PoiStyle("cart", HexColor("#8e24aa")),
        DataSchema.POICategory.Toilets to PoiStyle("toilet", HexColor("#0288d1")),
        DataSchema.POICategory.TrainStation to PoiStyle("train", HexColor("#455a64")),
        DataSchema.POICategory.Viewpoint to PoiStyle("eye", HexColor("#00897b")),
        DataSchema.POICategory.WaterSource to PoiStyle("droplet", HexColor("#2196f3")),
    )

/**
 * poiStyleOf answers how a point of interest of [category] is drawn: its entry in [poiCategoryStyles],
 * or null for a category the table does not name, whose pin is its caller's to draw.
 *
 * The category is matched exactly, letter case included, as the Data API spells it: `HUT` is a hut,
 * and `hut` names no category.
 */
public fun poiStyleOf(category: DataSchema.POICategory): PoiStyle? = poiCategoryStyles[category]
