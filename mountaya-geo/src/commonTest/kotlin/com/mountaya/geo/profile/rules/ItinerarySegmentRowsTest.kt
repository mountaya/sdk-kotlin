package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItinerarySegmentRowsTest {
    private val valley =
        GeoJsonLineString(listOf(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.005, 1_100.0), listOf(6.0, 45.01, 1_250.0)))
    private val col =
        GeoJsonLineString(
            listOf(listOf(6.0, 45.01, 1_250.0), listOf(6.0, 45.015, 1_400.0), listOf(6.0, 45.02, 1_320.0)),
        )
    private val leg = 1_111.9492664453662
    private val legs =
        listOf(
            ItineraryLeg(id = "s1", name = "Valley", geometry = valley),
            ItineraryLeg(id = "s2", name = "Col", color = "#e8590c", geometry = col),
        )
    private val series = buildElevationSeries(chainGeometry(legs))

    @Test
    fun eachLegSpansItsOwnGeometryAndStatesWhatTheScanReads() {
        val rows = itinerarySegmentRows(series, legs)

        assertEquals(listOf("s1", "s2"), rows.map { it.key })
        assertEquals(listOf(0, 1), rows.map { it.index })
        assertEquals(leg, rows[0].to, 1e-6)
        assertEquals(rows[0].to, rows[1].from)
        assertEquals(250.0, rows[0].metrics.sectionAscent)
        assertEquals(1_000.0, rows[0].metrics.altitudeMin)
        assertEquals(400.0, rows[1].metrics.ascent)
        assertEquals(80.0, rows[1].metrics.descent)
        assertEquals("#e8590c", rows[1].color)
        assertTrue(rows[0].memberIds.isEmpty())
        assertNull(rows[1].metrics.duration)
        assertNull(rows[1].metrics.difficulty)
    }

    @Test
    fun theLegsOwnFiguresThenAStatedFigureStandOverTheScanTheSpanDerived() {
        val own =
            listOf(
                ItineraryLeg(
                    id = "s1",
                    name = "Valley",
                    geometry = valley,
                    sectionDistance = 1_000.0,
                    duration = 1_800.0,
                    altitudeMax = 1_300,
                ),
                legs[1],
            )
        val stated =
            ItinerarySegmentRowMetrics(sectionDistance = 2_000.0, difficulty = RestSchema.TrailDifficultyType("T3"))
        val rows = itinerarySegmentRows(series, own, mapOf("s2" to stated))

        assertEquals(1_000.0, rows[0].metrics.sectionDistance)
        assertEquals(1_000.0, rows[0].metrics.distance)
        assertEquals(1_800.0, rows[0].metrics.duration)
        assertEquals(1_300.0, rows[0].metrics.altitudeMax)
        assertEquals(3_000.0, rows[1].metrics.distance)
        assertEquals(RestSchema.TrailDifficultyType("T3"), rows[1].metrics.difficulty)
        assertEquals(leg, rows[0].to, 1e-6)
    }

    @Test
    fun aStatedRunningFigureResetsTheTotalsForTheRowsAfterIt() {
        val rows =
            itinerarySegmentRows(
                series,
                legs + ItineraryLeg(id = "t", name = "Transfer", sectionDistance = 500.0),
                mapOf("s1" to ItinerarySegmentRowMetrics(distance = 5_000.0, ascent = 300.0)),
            )

        assertEquals(5_000.0, rows[0].metrics.distance)
        assertEquals(300.0, rows[0].metrics.ascent)
        assertEquals(5_000 + leg, assertNotNull(rows[1].metrics.distance, "the row states its distance"), 1e-6)
        assertEquals(450.0, rows[1].metrics.ascent)
        assertEquals(5_500 + leg, assertNotNull(rows[2].metrics.distance, "the row states its distance"), 1e-6)
    }

    @Test
    fun aLegWithNoGeometrySpansNothingAndDerivesNothing() {
        val rows = itinerarySegmentRows(series, listOf(legs[0], ItineraryLeg(id = "t", name = "Transfer")))

        assertEquals(rows[1].from, rows[1].to)
        assertNull(rows[1].metrics.sectionDistance)
        assertNull(rows[1].metrics.sectionAscent)
        assertEquals(250.0, rows[1].metrics.ascent)
    }

    @Test
    fun aCollapsedGroupIsOneRowNamingItsMembersAndNoElevationStatesLengthsAlone() {
        val group = ItineraryLeg(id = "group:g1", memberIds = listOf("s2", "s3"), name = "Over", geometry = col)
        assertEquals(listOf("s2", "s3"), itinerarySegmentRows(series, listOf(legs[0], group))[1].memberIds)
        val flat = ElevationSeries(emptyList(), 2 * leg, null, null)
        val rows = itinerarySegmentRows(flat, legs)

        assertNull(rows[0].metrics.ascent)
        assertNull(rows[0].metrics.altitudeMin)
        assertEquals(2 * leg, assertNotNull(rows[1].metrics.distance, "the row states its distance"), 1e-6)
    }

    @Test
    fun aSpanIsHeldToTheSeriesLength() {
        val short = ElevationSeries(emptyList(), 100.0, null, null)

        assertEquals(listOf(100.0, 100.0), itinerarySegmentRows(short, legs).map { it.to })
    }
}
