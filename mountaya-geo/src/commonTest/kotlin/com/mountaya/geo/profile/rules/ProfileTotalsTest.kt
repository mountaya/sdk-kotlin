package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProfileTotalsTest {
    private val climb =
        buildElevationSeries(
            GeoJsonLineString(
                listOf(
                    listOf(6.8652, 45.9237, 1_035.0),
                    listOf(6.8752, 45.9287, 1_120.0),
                    listOf(6.8852, 45.9337, 1_118.0),
                    listOf(6.8952, 45.9387, 1_290.0),
                    listOf(6.9052, 45.9437, 1_240.0),
                    listOf(6.9152, 45.9487, 1_480.0),
                    listOf(6.9271, 45.9682, 1_252.0),
                ),
            ),
        )

    @Test
    fun theTotalsAreTheOneScanReadAtItsLastSample() {
        val totals = profileTotals(climb)

        assertEquals(495.0, totals.ascent)
        assertEquals(278.0, totals.descent)
        assertEquals(1_035.0, totals.elevationMin)
        assertEquals(1_480.0, totals.elevationMax)
        assertEquals(climb.totalDistance, totals.distance)
        assertEquals(14.144950577528101, assertNotNull(totals.gradeMax, "the stretch states its steepest grade"), 1e-9)
    }

    @Test
    fun aSeriesWithNoSampleStatesItsLengthAlone() {
        assertEquals(
            ProfileTotals(7_117.5, null, null, null, null, null),
            profileTotals(ElevationSeries(emptyList(), 7_117.5, null, null)),
        )
    }

    @Test
    fun theThresholdAndTheWindowAreTheContracts() {
        assertEquals(5.0, ELEVATION_NOISE_THRESHOLD_METERS)
        assertEquals(100.0, GRADE_WINDOW_METERS)
    }
}
