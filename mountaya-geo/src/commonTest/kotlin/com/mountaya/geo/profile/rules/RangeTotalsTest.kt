package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RangeTotalsTest {
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
    fun theWholeItineraryIsItsTotals() {
        val whole = rangeTotals(climb, 0.0, climb.totalDistance)
        val totals = profileTotals(climb)

        assertEquals(totals.ascent, whole.ascent)
        assertEquals(totals.descent, whole.descent)
        assertEquals(
            assertNotNull(totals.gradeMax, "the stretch states its steepest grade"),
            assertNotNull(whole.gradeMax, "the stretch states its steepest grade"),
            1e-12,
        )
    }

    /** The noise threshold never restarts at a boundary. */
    @Test
    fun twoStretchesSumToTheWhole() {
        val first = rangeTotals(climb, 0.0, 3_000.0)
        val rest = rangeTotals(climb, 3_000.0, climb.totalDistance)

        assertEquals(
            495.0,
            assertNotNull(first.ascent, "the stretch states its ascent") +
                assertNotNull(rest.ascent, "the stretch states its ascent"),
            1e-9,
        )
        assertEquals(
            278.0,
            assertNotNull(first.descent, "the stretch states its descent") +
                assertNotNull(rest.descent, "the stretch states its descent"),
            1e-9,
        )
        assertEquals(3_000.0, first.distance)
    }

    @Test
    fun aRangeGivenBackwardsIsReadInOrder() {
        assertEquals(rangeTotals(climb, 1_000.0, 5_000.0), rangeTotals(climb, 5_000.0, 1_000.0))
    }

    @Test
    fun aStretchBetweenTwoSamplesHasABandAndAGradeOfItsOwn() {
        val stretch = rangeTotals(climb, 2_000.0, 2_500.0)

        assertEquals(
            1_135.1505171931315,
            assertNotNull(stretch.elevationMin, "the stretch states its lowest altitude"),
            1e-9,
        )
        assertEquals(
            1_225.4458140834251,
            assertNotNull(stretch.elevationMax, "the stretch states its highest altitude"),
            1e-9,
        )
        assertEquals(0.0, stretch.descent)
    }

    @Test
    fun aStretchPastTheSamplesStatesNoGradeAndASeriesWithNoneItsLengthAlone() {
        val past = rangeTotals(climb, climb.totalDistance, 8_000.0)

        assertEquals(0.0, past.ascent)
        assertEquals(1_252.0, past.elevationMin)
        assertNull(past.gradeMax)
        assertEquals(
            ProfileTotals(500.0, null, null, null, null, null),
            rangeTotals(ElevationSeries(emptyList(), 0.0, null, null), 500.0, 0.0),
        )
    }
}
