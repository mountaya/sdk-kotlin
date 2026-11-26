package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PlaceOnCurveTest {
    private val series =
        ElevationSeries(
            samplesEvery100Meters(1_000.0, 1_100.0, 1_200.0, 1_300.0, 1_400.0, 1_500.0),
            500.0,
            1_000.0,
            1_500.0,
        )
    private val domain = ElevationDomain(1_000.0, 1_500.0)
    private val whole = ProfileRange(0.0, 500.0)
    private val gateCutoff = Instant.fromEpochSeconds(1_788_000_000)

    @Test
    fun eachPassIsAPinADividerOrACutOffByWhatItStates() {
        val places =
            listOf(
                ProfilePlace(ProfileWaypoint.AtDistance("start", 100.0), name = "Start", isPinned = false),
                ProfilePlace(ProfileWaypoint.AtDistance("water", 200.0)),
                ProfilePlace(
                    ProfileWaypoint.AtDistance("gate", 300.0),
                    name = "Gate",
                    details = WaypointDetails(timing = WaypointDetails.Timing(cutoffAt = gateCutoff)),
                ),
            )
        val placement = placeOnCurve(series, null, domain, whole, places)

        assertEquals(listOf("water#0", "gate#0"), placement.pins.map { it.key })
        assertEquals(listOf(40.0, 60.0), placement.pins.map { it.x })
        assertEquals(listOf(60.0, 42.0), placement.pins.map { it.y })
        assertEquals(listOf(1_200.0, 1_300.0), placement.pins.map { it.altitude })
        assertEquals(listOf("start#0", "gate#0"), placement.dividers.map { it.key })
        assertEquals(listOf(78.0, 42.0), placement.dividers.map { it.y })
        assertEquals(listOf("gate#0"), placement.cutoffs.map { it.key })
        assertEquals(60.0, placement.cutoffs.firstOrNull()?.x)
        assertEquals(gateCutoff, placement.cutoffs.firstOrNull()?.cutoffAt)
    }

    @Test
    fun aPinBetweenTwoSamplesStandsOnTheLineBetweenThem() {
        val col = ProfilePlace(ProfileWaypoint.AtDistance("col", 250.0), name = "Col")
        val placement = placeOnCurve(series, null, domain, whole, listOf(col))

        assertEquals(1_250.0, placement.pins.firstOrNull()?.altitude)
        assertEquals(51.0, placement.pins.firstOrNull()?.y)
    }

    @Test
    fun aWindowLeavesOutWhatStandsOutsideItItsEndsKept() {
        val places =
            listOf("before" to 100.0, "edge" to 200.0, "inside" to 300.0, "after" to 450.0)
                .map { (id, distance) -> ProfilePlace(ProfileWaypoint.AtDistance(id, distance), name = id) }
        val placement = placeOnCurve(series, null, domain, ProfileRange(200.0, 400.0), places)

        assertEquals(listOf("edge#0", "inside#0"), placement.pins.map { it.key })
        assertEquals(listOf(0.0, 50.0), placement.pins.map { it.x })
    }

    @Test
    fun withNoElevationThereIsNoPinTheDividerHasNoHeightAndTheCutOffStands() {
        val flat = ElevationSeries(emptyList(), 500.0, null, null)
        val gate =
            ProfilePlace(
                ProfileWaypoint.AtDistance("gate", 250.0),
                name = "Gate",
                details = WaypointDetails(timing = WaypointDetails.Timing(cutoffAt = gateCutoff)),
            )
        val placement = placeOnCurve(flat, null, emptyElevationDomain, whole, listOf(gate))

        assertEquals(emptyList(), placement.pins)
        assertEquals(1, placement.dividers.size)
        assertNull(placement.dividers.firstOrNull()?.y)
        assertEquals(50.0, placement.cutoffs.firstOrNull()?.x)
    }

    @Test
    fun aPlacePassedTwiceIsPlacedAtEachPassKeyedByItsPasses() {
        val line =
            GeoJsonLineString(
                listOf(
                    listOf(6.0, 45.0, 1_000.0),
                    listOf(6.0, 45.002, 1_200.0),
                    listOf(6.0, 45.004, 1_400.0),
                    listOf(6.0, 45.002, 1_200.0),
                    listOf(6.0, 45.0, 1_000.0),
                ),
            )
        val looped = buildElevationSeries(line)
        val hut = ProfilePlace(ProfileWaypoint.AtPosition("hut", listOf(6.0, 45.001)), name = "Hut")
        val placement = placeOnCurve(looped, line, domain, ProfileRange(0.0, looped.totalDistance), listOf(hut))
        val xs = placement.dividers.map { it.x }

        assertEquals(listOf("hut#0", "hut#1"), placement.dividers.map { it.key })
        assertEquals(2, xs.size)
        assertEquals(12.5, xs[0], 0.001)
        assertEquals(87.5, xs[1], 0.001)
        assertEquals(2, placement.pins.size)
    }
}
