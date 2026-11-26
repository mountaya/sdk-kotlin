package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/** SearchEntity builds the Data API's search results a row rule reads, with an OpenStreetMap identity and a point. */
internal object SearchEntity {
    fun osm(
        type: String = "NODE",
        id: String = "9",
    ): DataSchema.OSMRef =
        DataSchema.OSMRef {
            this.type = DataSchema.OSMType(type)
            this.id = id
        }

    fun poi(
        name: String? = "Refuge du Goûter",
        category: String? = "HUT",
        elevation: Double? = null,
        distance: Double? = null,
        city: String? = null,
        region: String? = null,
        country: String? = null,
        countryName: String? = null,
        point: List<Double>? = listOf(6.5, 45.8),
    ): DataSchema.POI =
        DataSchema.POI {
            this.osm = osm()
            this.name = name
            this.category = category?.let { DataSchema.POICategory(it) }
            this.elevation = elevation
            this.distanceMeters = distance
            this.city = city
            this.region = region
            this.country = country
            this.countryName = countryName
            this.point = point
        }

    fun place(
        name: String? = "Annecy",
        category: String? = "POPULATED_PLACE",
        label: String? = "",
        region: String? = null,
        country: String? = null,
        countryName: String? = null,
        point: List<Double>? = listOf(6.13, 45.9),
        bounds: List<Double>? = null,
    ): DataSchema.Place =
        DataSchema.Place {
            this.osm = osm("WAY", "7")
            this.name = name
            this.category = category?.let { DataSchema.PlaceCategory(it) }
            this.label = label
            this.region = region
            this.country = country
            this.countryName = countryName
            this.point = point
            this.bounds = bounds
        }

    fun way(
        name: String? = "GR 5",
        highway: String? = "path",
        sacScale: String? = null,
        surface: String? = null,
        point: List<Double>? = listOf(6.5, 45.5),
    ): DataSchema.Way =
        DataSchema.Way {
            this.osm = osm("WAY", "3")
            this.name = name
            this.highway = highway
            this.sacScale = sacScale
            this.surface = surface
            this.point = point
        }
}
