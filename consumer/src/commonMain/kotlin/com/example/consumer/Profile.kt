package com.example.consumer

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.CURSOR_MARKER_GLYPH
import com.mountaya.elements.CursorMarkerPin
import com.mountaya.elements.CursorMarkerPoint
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.PROFILE_CURSOR_MARKER_ID
import com.mountaya.elements.SEGMENT_TABLE_CURSOR_MARKER_ID
import com.mountaya.elements.WAYPOINT_TABLE_CURSOR_MARKER_ID
import com.mountaya.elements.hideCursorMarker
import com.mountaya.elements.showCursorMarker
import com.mountaya.elements.writeLegEmphasis
import com.mountaya.geo.AXIS_LABEL_GUTTER
import com.mountaya.geo.AxisLabelBox
import com.mountaya.geo.CurveColor
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.MIN_DOMAIN_SPAN_METERS
import com.mountaya.geo.MIN_RANGE_SPAN_METERS
import com.mountaya.geo.PROFILE_BASELINE
import com.mountaya.geo.PROFILE_BOTTOM_INSET
import com.mountaya.geo.PROFILE_SCRUB_COLUMNS
import com.mountaya.geo.PROFILE_TOP_INSET
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.PlotCommand
import com.mountaya.geo.ProfileAnchor
import com.mountaya.geo.ProfilePlace
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.ResolvedItinerary
import com.mountaya.geo.SegmentRowCell
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.WaypointRowCell
import com.mountaya.geo.WaypointTableColumn
import com.mountaya.geo.amenityFilter
import com.mountaya.geo.amenityName
import com.mountaya.geo.amenityStacks
import com.mountaya.geo.anchoredDomain
import com.mountaya.geo.buildElevationSeries
import com.mountaya.geo.chainGeometry
import com.mountaya.geo.clampRange
import com.mountaya.geo.cullCrowdedLabels
import com.mountaya.geo.curveStops
import com.mountaya.geo.defaultSegmentTableColumns
import com.mountaya.geo.distanceTicks
import com.mountaya.geo.domainTicks
import com.mountaya.geo.extentDomain
import com.mountaya.geo.formatAltitudeRange
import com.mountaya.geo.formatDistanceTick
import com.mountaya.geo.formatElevation
import com.mountaya.geo.gradeAt
import com.mountaya.geo.itinerarySegmentRows
import com.mountaya.geo.itineraryWaypointRows
import com.mountaya.geo.lineBetween
import com.mountaya.geo.placeOnCurve
import com.mountaya.geo.placeSpans
import com.mountaya.geo.profileAreaPath
import com.mountaya.geo.profileDomain
import com.mountaya.geo.profilePolyline
import com.mountaya.geo.profileReadout
import com.mountaya.geo.profileTotals
import com.mountaya.geo.profileX
import com.mountaya.geo.profileY
import com.mountaya.geo.resolveCharacteristics
import com.mountaya.geo.rowAt
import com.mountaya.geo.rowStandsFor
import com.mountaya.geo.scrubDistance
import com.mountaya.geo.scrubGrid
import com.mountaya.geo.sectionChipCells
import com.mountaya.geo.sectionGrade
import com.mountaya.geo.segmentRowCell
import com.mountaya.geo.slopeStretches
import com.mountaya.geo.statStripCells
import com.mountaya.geo.surfaceColor
import com.mountaya.geo.toggleAmenity
import com.mountaya.geo.wayTypeColor
import com.mountaya.geo.waypointCard
import com.mountaya.geo.waypointPropertyLink
import com.mountaya.geo.waypointPropertyValue
import com.mountaya.geo.waypointRowCell

/**
 * Profile reads an itinerary's profile and tables as an app's shared code does, on every target: the rules a chart,
 * a strip, a card at the cursor and two tables stand on, a place's card, the figures written in the reader's units,
 * and the links a data element writes to a map. None of it names a composable.
 */
object Profile {
    /** Returns what a chart draws of [itinerary] and what its cursor states. */
    fun chart(itinerary: RestSchema.ItineraryWithDetails): List<String> {
        val resolved = ResolvedItinerary(itinerary)
        val series = buildElevationSeries(chainGeometry(resolved.legs) ?: resolved.geometry)
        val units = UnitPreferences("fr")
        val window = clampRange(ProfileRange(0.0, series.totalDistance / 2), series.totalDistance)
        val domain =
            if (anchoredDomain(series, ProfileAnchor.SeaLevel, window) == profileDomain(series)) {
                extentDomain(0.0, 4_810.0)
            } else {
                profileDomain(series, window)
            }
        val read = domainTicks(domain).map { formatElevation(it, units) }.toMutableList()
        seen(PROFILE_VIEW_BOX, PROFILE_TOP_INSET, PROFILE_BOTTOM_INSET, PROFILE_BASELINE)
        seen(MIN_DOMAIN_SPAN_METERS, MIN_RANGE_SPAN_METERS, PROFILE_SCRUB_COLUMNS, AXIS_LABEL_GUTTER)
        seen(profilePolyline(series, domain, window).map { it.x to it.y })
        for (command in profileAreaPath(series, domain, window)) {
            when (command) {
                is PlotCommand.Move -> seen(command.point)
                is PlotCommand.Line -> seen(command.point)
                PlotCommand.Close -> seen(command)
            }
        }
        val stretches = slopeStretches(series.points)
        seen(
            curveStops(stretches.map { CurveColor(it.from, it.to, it.color) }, series.totalDistance, window)
                .map { it.offset to it.color },
        )
        val placement =
            placeOnCurve(series, resolved.geometry, domain, window, resolved.waypoints.map { ProfilePlace(it) })
        for (stack in amenityStacks(placement.pins)) {
            read += stack.shown.map { amenityName(it, ElementsScope.standard) }
            read += "+${stack.counted}"
        }
        read += cullCrowdedLabels(placement.dividers.map { AxisLabelBox(it.key, it.x - 4, it.x + 4) })
        seen(placement.cutoffs.map { it.cutoffAt })
        val index = scrubGrid(series.points, window).firstOrNull()
        if (index != null) {
            val point = series.points[index]
            val readout = profileReadout(series, point, stretches = stretches)
            read += readout.cells.map { "${it.stat.id} ${it.value}" }
            seen(readout.grade, readout.stretch?.label, gradeAt(series.points, point.distance))
            seen(profileX(point.distance, series.totalDistance, window), profileY(point.elevation, domain))
        }
        seen(scrubDistance(0, window))
        for (tick in distanceTicks(window.from, window.to, units.length)) read += formatDistanceTick(tick, units)
        val characteristics = resolveCharacteristics(profileTotals(series), resolved.characteristics)
        for (cell in statStripCells(characteristics, null)) read += "${cell.stat.id} ${cell.number ?: 0.0}"
        read += formatAltitudeRange(characteristics.altitudeMin, characteristics.altitudeMax, units)
        seen(
            surfaceColor(RestSchema.SurfaceType.Paved).hex,
            wayTypeColor(RestSchema.WayType.Road).hex,
        )
        return read
    }

    /** Returns the rows and cells of the two tables over [itinerary], the filter over its places and a place's card. */
    fun tables(itinerary: RestSchema.ItineraryWithDetails): List<String> {
        val resolved = ResolvedItinerary(itinerary)
        val series = buildElevationSeries(resolved.geometry)
        val units = UnitPreferences("en", RestSchema.UnitLength.Imperial)
        val rows = itinerarySegmentRows(series, resolved.legs)
        val read = mutableListOf<String>()
        for (row in rows) {
            for (column in defaultSegmentTableColumns) {
                val cell = segmentRowCell(row, column)
                if (cell is SegmentRowCell.Segment) read += cell.name
            }
            seen(sectionChipCells(row.metrics).map { it.stat to it.value })
            seen(sectionGrade(row.metrics), rowStandsFor(row, "seg_1"))
        }
        seen(rowAt(rows, series.totalDistance / 2)?.id)
        seen(placeSpans(rows, series.totalDistance).map { Triple(it.start, it.end, it.index) })
        rows.firstOrNull()?.let { seen(lineBetween(series.points, it.from, it.to)) }
        val places = resolved.waypoints.map { ProfilePlace(it) }
        val waypointRows = itineraryWaypointRows(series, resolved.geometry, places)
        for (row in waypointRows) {
            val cell =
                waypointRowCell(row, WaypointTableColumn.Cutoff, ElementsScope.standard.markerGlyphs, units) {
                    it.toString()
                }
            if (cell is WaypointRowCell.Cutoff) read += cell.moment.orEmpty()
        }
        val picked = toggleAmenity(emptyList(), RestSchema.WaypointAmenity.Water)
        val filter = amenityFilter(waypointRows, picked, ElementsScope.standard)
        read += filter.available.map { it.value } + filter.shown.map { it.key }
        val place = resolved.waypoints.firstOrNull()
        if (place != null) {
            val facts = waypointCard(place.details, ElementsScope.standard, units) { it.toString() }
            read += listOf(facts.headline.orEmpty()) + facts.services.map { it.name }
            read += (facts.contact + facts.timing).map { "${it.label}: ${it.value} ${it.href.orEmpty()}" }
            read += facts.fields.map { it.value } + facts.links.map { it.label } + facts.photos.map { it.url }
            for (property in place.details?.properties.orEmpty()) {
                read += waypointPropertyValue(property, units)
                seen(waypointPropertyLink(property))
            }
        }
        return read
    }

    /** Pins a profile's cursor marker on [map], lights the leg under it, [stretch], and takes both down. */
    fun links(
        map: MapEngine,
        stretch: GeoJsonGeometry?,
    ) {
        var pinned: CursorMarkerPin? = null
        try {
            pinned = showCursorMarker(map, pinned, CursorMarkerPoint(PROFILE_CURSOR_MARKER_ID, listOf(6.86, 45.83)))
        } catch (refused: MapException) {
            seen(refused.kind)
        }
        var isLit = writeLegEmphasis(map, false, stretch, "#e9501e")
        hideCursorMarker(map, pinned, PROFILE_CURSOR_MARKER_ID)
        isLit = writeLegEmphasis(map, isLit, null)
        seen(isLit, CURSOR_MARKER_GLYPH, SEGMENT_TABLE_CURSOR_MARKER_ID, WAYPOINT_TABLE_CURSOR_MARKER_ID)
    }
}
