package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SteepestGradeTest {
    private fun degreesOf(ratio: Double) = convertSlope(ratio, SlopeUnit.Ratio, SlopeUnit.Degrees)

    private fun samples(vararg pairs: Pair<Double, Double>) =
        pairs.mapIndexed { index, (at, elevation) -> ElevationPoint(at, elevation, listOf(0.0, 0.0), index) }

    /** Two samples 10 m apart rising 5 m are not a 26-degree cliff: the run is the window's full 100 m. */
    @Test
    fun theGradeIsMeasuredOverTheWholeWindowNotBetweenTwoCloseSamples() {
        val points = samples(0.0 to 0.0, 500.0 to 0.0, 510.0 to 5.0, 1_000.0 to 5.0)

        assertEquals(
            degreesOf(0.05),
            assertNotNull(
                steepestGrade(points, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
                "the stretch has a steepest grade",
            ),
            1e-12,
        )
    }

    @Test
    fun aStretchShorterThanTheWindowIsMeasuredEndToEnd() {
        val points = samples(0.0 to 0.0, 1_000.0 to 100.0)

        assertEquals(
            degreesOf(0.1),
            assertNotNull(steepestGrade(points, 200.0, 250.0), "the stretch has a steepest grade"),
            1e-12,
        )
    }

    @Test
    fun aStretchThatOnlyDescendsIsZeroAndOneWithNoLengthHasNone() {
        assertEquals(0.0, steepestGrade(samples(0.0 to 100.0, 1_000.0 to 0.0), 0.0, 1_000.0))
        assertNull(steepestGrade(samples(0.0 to 0.0, 1_000.0 to 100.0), 400.0, 400.0))
        assertNull(steepestGrade(samples(0.0 to 0.0), 0.0, 100.0))
        assertNull(steepestGrade(emptyList(), 0.0, 100.0))
    }
}
