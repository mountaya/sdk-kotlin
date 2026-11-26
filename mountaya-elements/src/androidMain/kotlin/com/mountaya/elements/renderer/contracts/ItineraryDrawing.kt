package com.mountaya.elements

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.ItineraryLeg
import com.mountaya.geo.ItinerarySegmentRow
import com.mountaya.geo.ItineraryWaypointRow
import com.mountaya.geo.ProfilePlace
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.ResolvedCharacteristics
import com.mountaya.geo.ResolvedItinerary
import com.mountaya.geo.buildElevationSeries
import com.mountaya.geo.chainGeometry
import com.mountaya.geo.itinerarySegmentRows
import com.mountaya.geo.itineraryWaypointRows
import com.mountaya.geo.lineBetween
import com.mountaya.geo.profileTotals
import com.mountaya.geo.resolveCharacteristics
import com.mountaya.geo.rowAt

/**
 * ItineraryDrawing is what the data elements draw of one itinerary, read once from its answer, [resolved]: the
 * geometry its curve walks, the series, the figures stated over derived, the legs as rows, and the places as profile
 * places and as rows.
 *
 * A chain the app states, [legs], is also the curve (`sdk/SPEC.md` section 7): its legs' geometries are walked in
 * place of the itinerary's, which a chain carrying none falls back to, so a chip never describes a line it was not
 * cut from.
 */
internal class ItineraryDrawing(
    resolved: ResolvedItinerary?,
    legs: List<ItineraryLeg>?,
) {
    /** The geometry the curve walks. */
    val geometry: GeoJsonGeometry? = legs?.let(::chainGeometry) ?: resolved?.geometry

    /** The itinerary read for its profile. */
    val series: ElevationSeries = buildElevationSeries(geometry)

    /** The itinerary's figures, the server's over the ones the geometry reads. */
    val characteristics: ResolvedCharacteristics =
        resolveCharacteristics(profileTotals(series), resolved?.characteristics)

    /** The itinerary's legs as table rows. */
    val rows: List<ItinerarySegmentRow> = itinerarySegmentRows(series, legs ?: resolved?.legs.orEmpty())

    /** The itinerary's places, as a profile marks them. */
    val places: List<ProfilePlace> = resolved?.waypoints.orEmpty().map(::ProfilePlace)

    /** The itinerary's places as its waypoint table's rows. */
    val waypointRows: List<ItineraryWaypointRow> get() = itineraryWaypointRows(series, geometry, places)

    /** The window of the whole itinerary. */
    val whole: ProfileRange get() = ProfileRange(0.0, series.totalDistance)

    /**
     * What a profile's cursor on sample [index] writes to a map: the marker at the sample, and the leg under it lit,
     * the later of two at the meter they share (`rowAt`); nothing where no sample stands at [index].
     */
    fun cursorTarget(index: Int?): MapTarget {
        val point = index?.let { series.points.getOrNull(it) } ?: return MapTarget()
        val row = rowAt(rows, point.distance)
        return MapTarget(point.lngLat, row?.let { lineBetween(series.points, it.from, it.to) })
    }

    /**
     * What pointing a segment table's [row] writes to a map, as `legTarget` has it: the marker at the sample nearest
     * the leg's end, the place a roadbook's row is about arriving at, and the leg lit as its stretch of the series, so
     * a row standing for several legs lights them all.
     */
    fun segmentTarget(row: ItinerarySegmentRow?): MapTarget =
        row?.let { legTarget(series, it.from, it.to) } ?: MapTarget()

    /**
     * What pointing a waypoint table's [row] writes to a map, as `passTarget` has it: the marker at the sample nearest
     * the place's pass, the row being the place itself, and no stretch lit.
     */
    fun waypointTarget(row: ItineraryWaypointRow?): MapTarget = row?.let { passTarget(series, it.to) } ?: MapTarget()
}
