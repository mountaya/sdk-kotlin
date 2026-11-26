package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlin.math.roundToLong

/**
 * The least elevation a point of interest's second line states. An OpenStreetMap `ele` tag missing
 * or malformed reads as 0, and one near sea level tells nothing apart, so below it the distance from
 * the search's origin takes the place.
 */
internal const val ELEVATION_MIN_METERS: Double = 100.0

/** The first figure a distance is not stated at in its small unit: 1,000 m reads `1.0 km`. */
internal const val DISTANCE_FIGURE_CEILING: Double = 1_000.0

/** The meters in a kilometer. */
private const val METERS_PER_KILOMETER = 1_000.0

/** The grade of the scale every third path carries, which tells no path apart. */
private const val UNREMARKABLE_GRADE = "hiking"

/**
 * describePlace answers a geocoded place's second line under its title (`geo.search.describe`): what it is, its
 * category worded for the reader, then its region, then its country with its flag, each left out where it repeats
 * the title without regard to case, joined by a middle dot between spaces. An address whose geocoder's label is
 * another than its title reads as that label and its country, and a country as what it is alone. [wording] words a
 * category for the reader, and with none a category reads as its English title.
 */
public fun describePlace(
    place: DataSchema.Place,
    wording: SearchWording = SearchWording(),
): String {
    val name = placeTitle(place, wording)
    val primary = placeCategoryWord(place, wording)
    val country = countryToken(place.country, place.countryName)
    val label = place.label.orEmpty()
    return when (place.category) {
        DataSchema.PlaceCategory.Address -> {
            if (label.isNotEmpty() && label.lowercase() != name.lowercase()) {
                if (country.isEmpty()) label else joinTokens(label, country)
            } else {
                joinTokens(suppressIfMatchesName(primary, name), country)
            }
        }

        DataSchema.PlaceCategory.Country -> {
            primary
        }

        else -> {
            joinTokens(suppressIfMatchesName(primary, name), suppressIfMatchesName(place.region, name), country)
        }
    }
}

/**
 * describePoi answers a point of interest's second line under its title (`geo.search.describe`): its category worded
 * for the reader, then its elevation where it is at least 100 m and otherwise its distance from the search's
 * origin, then its town, else its region, and its country with its flag, each left out where it repeats the title.
 * Elevation leads, because three huts a kilometre apart are told apart by their altitude. A figure is stated in
 * [wording]'s units, rounded half away from zero, and in metric with Latin digits where it states none: 1150 m away
 * reads `1.2 km`.
 */
public fun describePoi(
    poi: DataSchema.POI,
    wording: SearchWording = SearchWording(),
): String {
    val name = poiTitle(poi, wording)
    val elevation = poi.elevation
    val distance = poi.distanceMeters
    val figure =
        when {
            elevation != null && elevation >= ELEVATION_MIN_METERS -> elevationToken(elevation, wording.units)
            distance != null && distance > 0.0 -> distanceToken(distance, wording.units)
            else -> ""
        }
    return joinTokens(
        suppressIfMatchesName(poiCategoryWord(poi, wording), name),
        figure,
        suppressIfMatchesName(poi.city ?: poi.region ?: "", name),
        countryToken(poi.country, poi.countryName),
    )
}

/**
 * describeWay answers a way's second line under its title (`geo.search.describe`): its `highway` tag worded for the
 * reader, then its grade on the SAC scale where it is not the unremarkable `hiking`, else its surface, worded as the
 * whole token, `gravel surface`, then its town, else its region, and its country with its flag, each left out where
 * it repeats the title.
 */
public fun describeWay(
    way: DataSchema.Way,
    wording: SearchWording = SearchWording(),
): String {
    val name = wayTitle(way, wording)
    val grade = way.sacScale
    val surface = way.surface
    val second =
        when {
            !grade.isNullOrEmpty() && grade != UNREMARKABLE_GRADE -> {
                wordFor(SearchLabel.SacScale(grade), titleCase(grade), wording)
            }

            !surface.isNullOrEmpty() -> {
                wordFor(
                    SearchLabel.Surface(surface),
                    titleCase(surface).lowercase() + " surface",
                    wording,
                )
            }

            else -> {
                ""
            }
        }
    return joinTokens(
        suppressIfMatchesName(highwayWord(way, wording), name),
        second,
        suppressIfMatchesName(way.city ?: way.region ?: "", name),
        countryToken(way.country, way.countryName),
    )
}

/** An elevation as a second line states it: whole meters, `3835 m`, or the reader's units. */
internal fun elevationToken(
    meters: Double,
    units: UnitPreferences?,
): String = if (units == null) "${roundHalfAwayFromZero(meters, 0).roundToLong()} m" else formatElevation(meters, units)

/**
 * A distance as a second line states it: rounded to the nearest ten of its small unit, since a
 * search hit is not placed to the meter, and in the large unit to one decimal once that figure
 * reaches [DISTANCE_FIGURE_CEILING], so 995 m reads `1.0 km` and never `1000 m`. The unit is chosen
 * on the rounded figure. A distance that rounds to zero is nothing: `0 m` claims a hit the reader
 * stands on. With no units, meters and kilometers in Latin digits; with them, the reader's system,
 * language and digits. Every figure rounds half away from zero on its decimal value.
 */
internal fun distanceToken(
    meters: Double,
    units: UnitPreferences?,
): String {
    val imperial = units != null && units.length == RestSchema.UnitLength.Imperial
    val small = if (imperial) LengthUnit.Foot else LengthUnit.Meter
    val tens = roundHalfAwayFromZero(convertLength(meters, LengthUnit.Meter, small) / 10.0, 0) * 10.0
    if (tens == 0.0) return ""
    if (units == null) {
        if (tens < DISTANCE_FIGURE_CEILING) return "${tens.roundToLong()} m"
        val tenths = (roundHalfAwayFromZero(meters / METERS_PER_KILOMETER, 1) * 10.0).roundToLong()
        return "${tenths / 10}.${tenths % 10} km"
    }
    val patterns = unitPatterns(units.language)
    if (tens < DISTANCE_FIGURE_CEILING) {
        return formatMeasurement(tens, 0, if (imperial) patterns.foot else patterns.meter, units.language, patterns)
    }
    val large = if (imperial) LengthUnit.Mile else LengthUnit.Kilometer
    return formatMeasurement(
        convertLength(meters, LengthUnit.Meter, large),
        1,
        if (imperial) patterns.mile else patterns.kilometer,
        units.language,
        patterns,
        minimumFractionDigits = 1,
    )
}
