package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItineraryWaypointRowsTest {
    private val line =
        GeoJsonLineString(listOf(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.005, 1_200.0), listOf(6.0, 45.01, 1_500.0)))
    private val series = buildElevationSeries(line)

    @Test
    fun thePlacesInItineraryOrderEachReachedFromTheOneBefore() {
        val rows =
            itineraryWaypointRows(
                series,
                line,
                listOf(
                    ProfilePlace(ProfileWaypoint.AtDistance("col", 500.0), name = "Col"),
                    ProfilePlace(ProfileWaypoint.AtDistance("hut", 200.0), name = "Hut"),
                ),
            )

        assertEquals(listOf("hut#0", "col#0"), rows.map { it.key })
        assertEquals(listOf(0, 1), rows.map { it.index })
        assertEquals(listOf(0.0, 200.0), rows.map { it.from })
        assertEquals(listOf(200.0, 500.0), rows.map { it.to })
        assertEquals(1_071.9457284734617, assertNotNull(rows[0].metrics.altitude, "the row states its altitude"), 1e-9)
        assertEquals(
            107.91859271019243,
            assertNotNull(rows[1].metrics.sectionAscent, "the row states its section ascent"),
            1e-9,
        )
        assertEquals(500.0, rows[1].metrics.distance)
        assertEquals(300.0, rows[1].metrics.sectionDistance)
    }

    @Test
    fun aStatedFigureCarriesIntoTheRunningTotalsAfterItAnUnnamedPlaceStatingNone() {
        val rows =
            itineraryWaypointRows(
                series,
                line,
                listOf(
                    ProfilePlace(
                        ProfileWaypoint.AtDistance("hut", 400.0),
                        name = "Hut",
                        metrics = ItinerarySegmentRowMetrics(sectionDistance = 450.0, ascent = 250.0),
                    ),
                    ProfilePlace(ProfileWaypoint.AtDistance("col", 900.0), name = "Col"),
                ),
            )

        assertEquals(450.0, rows[0].metrics.distance)
        assertEquals(250.0, rows[0].metrics.ascent)
        assertEquals(950.0, rows[1].metrics.distance)
        assertEquals(491.74221024920655, assertNotNull(rows[1].metrics.ascent, "the stretch states its ascent"), 1e-9)
        val unnamed =
            itineraryWaypointRows(
                series,
                line,
                listOf(
                    ProfilePlace(
                        ProfileWaypoint.AtDistance("water", 300.0),
                        metrics = ItinerarySegmentRowMetrics(sectionDistance = 999.0),
                    ),
                ),
            )
        assertEquals(300.0, unnamed[0].metrics.sectionDistance)
    }

    @Test
    fun aPlacePassedTwiceStatesItsFiguresOnItsPrimaryPass() {
        val loop =
            GeoJsonLineString(
                listOf(
                    listOf(6.0, 45.0, 1_000.0),
                    listOf(6.0, 45.005, 1_200.0),
                    listOf(6.0, 45.0075, 1_350.0),
                    listOf(6.0, 45.01, 1_500.0),
                    listOf(6.0, 45.0075, 1_350.0),
                    listOf(6.0, 45.005, 1_200.0),
                    listOf(6.0, 45.0, 1_000.0),
                ),
            )
        val rows =
            itineraryWaypointRows(
                buildElevationSeries(loop),
                loop,
                listOf(
                    ProfilePlace(ProfileWaypoint.AtPosition("start", listOf(6.0, 45.0)), name = "Start"),
                    ProfilePlace(
                        ProfileWaypoint.AtPosition("spring", listOf(6.0, 45.005)),
                        name = "Spring",
                        metrics = ItinerarySegmentRowMetrics(sectionDistance = 600.0),
                    ),
                    ProfilePlace(ProfileWaypoint.AtPosition("top", listOf(6.0, 45.01)), name = "Top"),
                    ProfilePlace(ProfileWaypoint.AtPosition("finish", listOf(6.0, 45.0)), name = "Finish"),
                ),
            )

        assertEquals(listOf("start#0", "spring#0", "top#0", "spring#1", "finish#0"), rows.map { it.key })
        assertEquals(600.0, rows[1].metrics.sectionDistance)
        assertEquals(600.0, rows[1].metrics.distance)
        assertEquals(
            555.974633222288,
            assertNotNull(rows[3].metrics.sectionDistance, "the row states its section distance"),
            1e-6,
        )
    }

    @Test
    fun anItineraryWithNoElevationStatesItsDistancesAlone() {
        val flatLine = GeoJsonLineString(listOf(listOf(6.0, 45.0), listOf(6.0, 45.005), listOf(6.0, 45.01)))
        val rows =
            itineraryWaypointRows(
                buildElevationSeries(flatLine),
                flatLine,
                listOf(ProfilePlace(ProfileWaypoint.AtDistance("hut", 300.0), name = "Hut")),
            )

        assertNull(rows[0].metrics.altitude)
        assertNull(rows[0].metrics.ascent)
        assertEquals(300.0, rows[0].metrics.distance)
        assertEquals(300.0, rows[0].metrics.sectionDistance)
        assertTrue(itineraryWaypointRows(series, line, emptyList()).isEmpty())
    }
}
