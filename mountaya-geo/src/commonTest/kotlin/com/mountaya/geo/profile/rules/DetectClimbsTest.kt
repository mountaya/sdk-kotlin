package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class DetectClimbsTest {
    /** A sample every 100 m, written in kilometers the way a profile carries them: 0.1 × index. */
    private fun profile(vararg elevations: Double) =
        elevations.mapIndexed { index, it -> RouteProfilePoint(index / 10.0, it) }

    private fun flat(
        count: Int,
        elevation: Double,
    ) = DoubleArray(count) { elevation }

    private fun ramp(
        count: Int,
        from: Double,
        step: Double,
    ) = DoubleArray(count) { from + step * (it + 1) }

    @Test
    fun aClimbOpensOnItsFootClosesAtItsSummitAndIsCategorized() {
        val climbs = detectClimbs(profile(*flat(11, 1_000.0), *ramp(40, 1_000.0, 6.0), *flat(10, 1_240.0)))

        assertEquals(1, climbs.size)
        // 1.2 − 0.9 is a hair under 0.3 km, so the grade at 1.2 km is read from 0.8 km.
        assertEquals(0.8, climbs[0].startKm)
        assertEquals(5.0, climbs[0].endKm)
        assertEquals(1_240.0, climbs[0].peakElevation)
        assertEquals(5.714285714285714, climbs[0].avgGradePercent, 1e-12)
        assertEquals(ClimbCategory.Category3, climbs[0].category)
    }

    @Test
    fun aFlatAsLongAsTheCloseWindowEndsTheClimbAndAShorterEasingDoesNot() {
        val twoClimbs =
            profile(
                *flat(11, 1_000.0),
                *ramp(30, 1_000.0, 7.0),
                *flat(10, 1_210.0),
                *ramp(30, 1_210.0, 7.0),
                *flat(10, 1_420.0),
            )

        assertEquals(listOf(0.8 to 4.0, 4.8 to 8.0), detectClimbs(twoClimbs).map { it.startKm to it.endKm })
        val oneClimb =
            profile(
                *flat(11, 1_000.0),
                *ramp(20, 1_000.0, 6.0),
                *flat(3, 1_120.0),
                *ramp(20, 1_120.0, 6.0),
                *flat(10, 1_240.0),
            )
        assertEquals(1, detectClimbs(oneClimb).size)
    }

    @Test
    fun aClimbToTheEndClosesAtTheEnd() {
        val climbs = detectClimbs(profile(*flat(11, 1_000.0), *ramp(50, 1_000.0, 8.0)))

        assertEquals(6.0, climbs.single().endKm)
        assertEquals(ClimbCategory.Category2, climbs.single().category)
    }

    @Test
    fun aFalseFlatADescentAShortRampAndASingleSampleAreNoClimb() {
        assertEquals(emptyList(), detectClimbs(profile(*ramp(100, 1_000.0, 2.0))))
        assertEquals(emptyList(), detectClimbs(profile(*ramp(60, 2_000.0, -6.0))))
        assertEquals(
            emptyList(),
            detectClimbs(profile(*flat(11, 1_000.0), *ramp(4, 1_000.0, 12.0), *flat(10, 1_048.0))),
        )
        assertEquals(emptyList(), detectClimbs(listOf(RouteProfilePoint(0.0, 1_000.0))))
        assertEquals(emptyList(), detectClimbs(emptyList()))
    }

    @Test
    fun theWindowsAreTheContracts() {
        assertEquals(
            listOf(0.3, 3.0, 1.0, 0.5, 0.5),
            listOf(
                CLIMB_OPEN_WINDOW_KM,
                CLIMB_OPEN_GRADE_PERCENT,
                CLIMB_CLOSE_GRADE_PERCENT,
                CLIMB_CLOSE_WINDOW_KM,
                MIN_CLIMB_LENGTH_KM,
            ),
        )
    }
}
