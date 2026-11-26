package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileCharacteristicsTest {
    private fun difficulty(vararg grades: String) =
        grades.map { RestSchema.TrailDifficultyInfo(0, 1, RestSchema.TrailDifficultyType(it)) }

    @Test
    fun theFiguresAreSummedAndTheRangesTakeTheExtremes() {
        val summary =
            profileCharacteristics(
                listOf(
                    restSegment(
                        distance = 7_117.0,
                        duration = 9_000.0,
                        ascent = 495,
                        descent = 278,
                        altitude =
                            1_035 to 1_480,
                        gradient = -9.0 to 14.0,
                    ),
                    restSegment(
                        distance = 5_200.0,
                        duration = 7_200.0,
                        ascent = 850,
                        descent = 2,
                        altitude =
                            1_252 to 2_100,
                        gradient = -4.0 to 22.0,
                    ),
                ),
            )

        assertEquals(12_317.0, summary.distance)
        assertEquals(16_200.0, summary.duration)
        assertEquals(1_345 to 280, summary.ascent to summary.descent)
        assertEquals(1_035 to 2_100, summary.altitudeMin to summary.altitudeMax)
        assertEquals(-9.0 to 22.0, summary.slopeMin to summary.slopeMax)
        assertFalse(summary.durationPartial)
    }

    @Test
    fun aFigureNoSegmentStatesIsNullAndADurationSomeLackIsPartial() {
        val partial =
            profileCharacteristics(
                listOf(restSegment(distance = 1_000.0, duration = 600.0), restSegment(distance = 2_000.0)),
            )

        assertEquals(600.0, partial.duration)
        assertTrue(partial.durationPartial)
        assertNull(partial.ascent)
        val nothing = profileCharacteristics(listOf(restSegment(), restSegment()))
        assertNull(nothing.distance)
        assertNull(nothing.duration)
        assertFalse(nothing.durationPartial)
        assertFalse(profileCharacteristics(emptyList()).durationPartial)
    }

    @Test
    fun theDifficultyRangeIsTheEasiestAndHardestRankedGrade() {
        val summary =
            profileCharacteristics(
                listOf(
                    restSegment(trailDifficulty = difficulty("hiking", "mountain-hiking")),
                    restSegment(trailDifficulty = difficulty("alpine-hiking")),
                ),
            )

        assertEquals(
            "hiking" to "alpine-hiking",
            summary.trailDifficultyMin?.value to summary.trailDifficultyMax?.value,
        )
        val unknown =
            profileCharacteristics(listOf(restSegment(trailDifficulty = difficulty("unknown", "mtb-2", "unknown"))))
        assertEquals("mtb-2" to "mtb-2", unknown.trailDifficultyMin?.value to unknown.trailDifficultyMax?.value)
        assertNull(
            profileCharacteristics(listOf(restSegment(trailDifficulty = difficulty("unknown")))).trailDifficultyMax,
        )
    }

    /** Two grades of one rank on two scales: the first listed holds the bound. */
    @Test
    fun ofTwoGradesOfOneRankTheFirstListedHolds() {
        val summary = profileCharacteristics(listOf(restSegment(trailDifficulty = difficulty("mtb-1", "hiking"))))

        assertEquals("mtb-1" to "mtb-1", summary.trailDifficultyMin?.value to summary.trailDifficultyMax?.value)
    }

    @Test
    fun theStopsAreSummedFromTheWaypoints() {
        val stops =
            listOf(
                restWaypoint(timing = RestSchema.WaypointTiming { stop_duration = 900 }),
                restWaypoint(),
                restWaypoint(timing = RestSchema.WaypointTiming { stop_duration = 300 }),
            )

        assertEquals(1_200, profileCharacteristics(listOf(restSegment()), stops).stopDuration)
        assertNull(profileCharacteristics(listOf(restSegment()), listOf(restWaypoint())).stopDuration)
    }
}
