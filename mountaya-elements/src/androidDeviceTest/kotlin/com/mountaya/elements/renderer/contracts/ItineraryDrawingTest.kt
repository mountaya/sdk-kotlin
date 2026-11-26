package com.mountaya.elements

import com.mountaya.geo.ProfileRange
import com.mountaya.geo.lineBetween
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItineraryDrawingTest {
    private val drawing = ItineraryDrawing(Itineraries.resolved, null)

    @Test
    fun theItinerarysLegsAreItsCurveAndItsRowsItsPlacesItsPinsAndItsRows() {
        val points = drawing.series.points
        val total = drawing.series.totalDistance

        assertEquals(listOf(1_000.0, 1_200.0, 1_400.0, 1_400.0, 1_250.0, 1_100.0), points.map { it.elevation })
        assertEquals(points[2].distance, points[3].distance, "the seam between two legs is free")
        assertEquals(listOf("climb", "descent"), drawing.rows.map { it.id })
        assertEquals(listOf("Climb", "Descent"), drawing.rows.map { it.name })
        assertEquals(0.0, drawing.rows[0].from)
        assertEquals(total, drawing.rows[1].to)
        assertEquals(drawing.rows[0].to, drawing.rows[1].from)
        assertTrue(abs(drawing.rows[0].to - total / 2) < 1e-6, "${drawing.rows[0].to} of $total")
        assertEquals(listOf("hut", "col", "spring"), drawing.places.map { it.id })
        assertEquals(listOf("hut", "col", "spring"), drawing.waypointRows.map { it.place.id })
        assertEquals(ProfileRange(0.0, total), drawing.whole)
        assertEquals(400.0, drawing.characteristics.ascent, "read off the curve, the server stating none")
        assertEquals(300.0, drawing.characteristics.descent)
    }

    @Test
    fun aChainTheAppStatesIsTheCurveItsLegsTheRowsThePlacesStillTheItinerarys() {
        val climb = Itineraries.resolved.legs.first()
        val chained = ItineraryDrawing(Itineraries.resolved, listOf(climb))

        assertEquals(listOf(1_000.0, 1_200.0, 1_400.0), chained.series.points.map { it.elevation })
        assertEquals(listOf("climb"), chained.rows.map { it.id })
        assertEquals(listOf("hut", "col", "spring"), chained.places.map { it.id })
    }

    @Test
    fun anItineraryNotAnsweredYetDrawsNothingAndPointsNowhere() {
        val nothing = ItineraryDrawing(null, null)

        assertTrue(nothing.series.points.isEmpty())
        assertTrue(nothing.rows.isEmpty() && nothing.places.isEmpty() && nothing.waypointRows.isEmpty())
        assertEquals(MapTarget(), nothing.cursorTarget(0))
    }

    @Test
    fun aCursorPinsTheMarkerAtItsSampleAndLightsItsLegTheLaterOneAtASharedMeter() {
        val points = drawing.series.points
        val climb = lineBetween(points, drawing.rows[0].from, drawing.rows[0].to)
        val descent = lineBetween(points, drawing.rows[1].from, drawing.rows[1].to)

        assertEquals(MapTarget(points[1].lngLat, climb), drawing.cursorTarget(1))
        assertEquals(MapTarget(points[2].lngLat, descent), drawing.cursorTarget(2))
        assertEquals(MapTarget(points[5].lngLat, descent), drawing.cursorTarget(5))
        assertEquals(MapTarget(), drawing.cursorTarget(null))
        assertEquals(MapTarget(), drawing.cursorTarget(6), "no sample stands there")
    }

    @Test
    fun aLegsRowPinsTheMarkerAtItsEndAndLightsItselfAPlacesRowPinsAtThePlace() {
        val points = drawing.series.points
        val climb = drawing.rows[0]
        val hut = drawing.waypointRows.first()

        assertEquals(
            MapTarget(listOf(6.0, 45.01), lineBetween(points, climb.from, climb.to)),
            drawing.segmentTarget(climb),
        )
        assertEquals(MapTarget(), drawing.segmentTarget(null))
        assertEquals(MapTarget(listOf(6.0, 45.005)), drawing.waypointTarget(hut))
        assertEquals(MapTarget(), drawing.waypointTarget(null))
    }
}
