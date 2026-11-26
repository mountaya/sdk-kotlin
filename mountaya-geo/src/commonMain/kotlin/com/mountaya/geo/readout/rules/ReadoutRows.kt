package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.math.abs

/**
 * READOUT_DETAIL_ZOOM is the zoom from which a readout states the point of interest, the terrain and the zone:
 * the detail zoom.
 */
public const val READOUT_DETAIL_ZOOM: Double = 13.0

/** The places each coordinate of a copied pair is written to. */
internal const val READOUT_COPY_DECIMALS: Int = 5

/** The level whose colour a zone of a level the table does not name is drawn in. */
internal val zoneLevelFallback: RestSchema.ZoneLevel = RestSchema.ZoneLevel.Info

/**
 * OverlayReadout is how a readout writes the band of one vector overlay: the [measure] its values are written in, the
 * [scale] they are multiplied by first, and the travel-rate form a speed is written at whatever the reader's.
 */
internal class OverlayReadout(
    val measure: ReadoutMeasure,
    val scale: Double = 1.0,
    val pace: RestSchema.UnitPace? = null,
)

/**
 * The vector overlays whose band a readout states, by slug: a temperature as a temperature, a humidity as a humidity,
 * a snowfall and a snow depth, given in meters, as a snowfall, a visibility as a distance, and a wind as a speed per
 * time whatever the reader's pace, since a wind is no travel rate.
 */
internal val overlayReadouts: Map<String, OverlayReadout> =
    mapOf(
        "humidity" to OverlayReadout(ReadoutMeasure.Humidity),
        "snowdepth" to OverlayReadout(ReadoutMeasure.Snowfall, scale = 100.0),
        "snowfall-daily" to OverlayReadout(ReadoutMeasure.Snowfall),
        "snowfall-hourly" to OverlayReadout(ReadoutMeasure.Snowfall),
        "temperature-actual" to OverlayReadout(ReadoutMeasure.Temperature),
        "temperature-perceived" to OverlayReadout(ReadoutMeasure.Temperature),
        "temperature-soil" to OverlayReadout(ReadoutMeasure.Temperature),
        "visibility" to OverlayReadout(ReadoutMeasure.Distance),
        "wind" to OverlayReadout(ReadoutMeasure.Speed, pace = RestSchema.UnitPace.PerTime),
    )

/** The colour each zone level is drawn in. */
internal val zoneLevelColors: Map<RestSchema.ZoneLevel, String> =
    mapOf(
        RestSchema.ZoneLevel.Danger to "#f03e3e",
        RestSchema.ZoneLevel.Info to "#1c7ed6",
        RestSchema.ZoneLevel.Resolved to "#37b24d",
        RestSchema.ZoneLevel.Warning to "#f59f00",
    )

/**
 * readoutRows answers what the readout a right-click or a long press opens at [point] states (`geo.readout.rows`), in
 * order and each only where the map holds it: the point of interest at the detail zoom, 13 and above; the band the
 * classic overlay draws at any zoom, for a vector overlay the readout's table names with a name and a bound that is a
 * finite number; the terrain's altitude, slope and aspect at the detail zoom; the moment alone, for a vector overlay
 * whose band no row states; the coordinates, always; a note to zoom in, below the detail zoom; and the zone at the
 * detail zoom. A map's readout holds no place link, no waypoint action and no street-level view.
 *
 * A point of interest's category is read through `poiCategoryOf` and worded by [wording] as `describePoi` words one,
 * its English title where the host states none and the tile's own category titled where the table names none. A
 * band's two bounds are stated where they differ under `readout.overlayRange`, and its one bound otherwise, scaled
 * where the table states a scale. The slope reads its colour on the profile's ascent ramp and the aspect its compass
 * direction. A copy writes the latitude then the longitude, each its exact binary value rounded half away from zero to
 * five places, with no sign before one that rounds to zero. A zone of a level the table does not name is drawn in the
 * colour of `info` under no word, and its window reads both bounds under `readout.zoneWindowRange`, its start alone
 * under `readout.zoneWindowFrom` and its end alone under `readout.zoneWindowUntil`.
 */
public fun readoutRows(
    point: ReadoutPoint,
    wording: SearchWording = SearchWording(),
): List<ReadoutRow> {
    val detail = point.zoom >= READOUT_DETAIL_ZOOM
    val rows = mutableListOf<ReadoutRow>()
    if (detail) point.poi?.let { rows += poiRow(it, wording) }
    val band = bandRow(point)
    band?.let { rows += it }
    if (detail) point.terrain?.let(::terrainRow)?.let { rows += it }
    val overlay = point.overlay
    val instant = point.instant
    if (band == null && overlay != null && overlay.isVector && instant != null) {
        rows += ReadoutRow.Moment(instant, "readout.overlayTime")
    }
    coordinatesRow(point.position)?.let { rows += it }
    if (!detail) rows += ReadoutRow.ZoomIn("readout.zoomInForDetails")
    if (detail) point.zone?.let { rows += zoneRow(it) }
    return rows
}

private fun poiRow(
    poi: ReadoutPoint.Poi,
    wording: SearchWording,
): ReadoutRow.Poi {
    val category = poiCategoryOf(poi.category)
    val label =
        if (category == null) {
            titleCase(poi.category)
        } else {
            wordFor(SearchLabel.PoiCategory(category), titleCase(category.value.lowercase()), wording)
        }
    return ReadoutRow.Poi(poi.name.takeIf { it.isNotEmpty() }, category, label)
}

private fun bandRow(point: ReadoutPoint): ReadoutRow.Band? {
    val overlay = point.overlay?.takeIf { it.isVector && it.name.isNotEmpty() } ?: return null
    val readout = overlayReadouts[overlay.slug] ?: return null
    val band = point.band ?: return null
    val bounds = listOfNotNull(band.min, band.max).filter { it.isFinite() }.map { it * readout.scale }
    if (bounds.isEmpty()) return null
    val values = if (bounds.size == 2 && bounds[0] != bounds[1]) bounds else bounds.take(1)
    return ReadoutRow.Band(
        overlay = overlay.name,
        measure = readout.measure,
        pace = readout.pace,
        values = values,
        word = if (values.size == 2) "readout.overlayRange" else null,
        time = point.instant?.let { ReadoutRow.Moment(it, "readout.overlayTime") },
    )
}

private fun terrainRow(terrain: ReadoutPoint.Terrain): ReadoutRow.Terrain? {
    val readings = mutableListOf<ReadoutRow.Terrain.Reading>()
    terrain.elevation?.takeIf { it.isFinite() }?.let {
        readings += ReadoutRow.Terrain.Reading(ReadoutRow.Terrain.Reading.Kind.Altitude, it, "readout.altitude")
    }
    terrain.slope?.takeIf { it.isFinite() }?.let {
        readings +=
            ReadoutRow.Terrain.Reading(
                ReadoutRow.Terrain.Reading.Kind.Slope,
                it,
                "readout.slope",
                color = slopeRampColor(abs(it)),
            )
    }
    terrain.aspect?.let { aspect ->
        val cardinal = aspectCardinal(aspect) ?: return@let
        readings +=
            ReadoutRow.Terrain.Reading(
                ReadoutRow.Terrain.Reading.Kind.Aspect,
                aspect,
                "readout.aspect",
                cardinal = cardinal,
                cardinalWord = "readout.aspects.${cardinal.code}",
            )
    }
    return if (readings.isEmpty()) null else ReadoutRow.Terrain(readings)
}

private fun coordinatesRow(position: List<Double>): ReadoutRow.Coordinates? {
    val text = formatLngLat(position) ?: return null
    val longitude = wrapLongitudePastRange(position[0])
    val copy = "${copiedCoordinate(position[1])}, ${copiedCoordinate(longitude)}"
    return ReadoutRow.Coordinates(text, copy, "readout.coordinates")
}

/**
 * A coordinate as a copy writes it: its exact binary value rounded half away from zero to five places, a point, no
 * grouping, and a minus sign before a negative value that does not round to zero.
 */
internal fun copiedCoordinate(value: Double): String {
    val digits = decimalPlaces(abs(value), READOUT_COPY_DECIMALS)
    val isZero = digits.all { it == '0' || it == '.' }
    return if (value < 0 && !isZero) "-$digits" else digits
}

private fun zoneRow(zone: ReadoutPoint.Zone): ReadoutRow.Zone {
    val color = zoneLevelColors[zone.level]
    val start = zone.start
    val end = zone.end
    val window =
        when {
            start != null && end != null -> ReadoutRow.Zone.Window(start, end, "readout.zoneWindowRange")
            start != null -> ReadoutRow.Zone.Window(start, null, "readout.zoneWindowFrom")
            end != null -> ReadoutRow.Zone.Window(null, end, "readout.zoneWindowUntil")
            else -> null
        }
    return ReadoutRow.Zone(
        name = zone.name.takeIf { it.isNotEmpty() },
        description = zone.description.takeIf { it.isNotEmpty() },
        level = zone.level,
        levelWord = if (color != null) "readout.zoneLevels.${zone.level.value}" else null,
        color = color ?: zoneLevelColors.getValue(zoneLevelFallback),
        window = window,
        word = "readout.zone",
    )
}
