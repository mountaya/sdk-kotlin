package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResolveCharacteristicsTest {
    private val derived = ProfileTotals(24_310.0, 1_520.4, 1_401.0, 1_030.0, 1_852.0, 17.9)
    private val t1 = RestSchema.TrailDifficultyType("T1")
    private val t3 = RestSchema.TrailDifficultyType("T3")

    @Test
    fun withNothingStatedEveryFigureIsTheGeometrysTheStatedOnesNone() {
        val resolved = resolveCharacteristics(derived, null)

        assertEquals(
            ResolvedCharacteristics(
                distance = 24_310.0,
                ascent = 1_520.4,
                descent = 1_401.0,
                altitudeMin = 1_030.0,
                altitudeMax = 1_852.0,
                slopeMax = 17.9,
            ),
            resolved,
        )
        assertNull(resolved.duration)
        assertNull(resolved.slopeMin)
        assertNull(resolved.trailDifficultyMax)
    }

    @Test
    fun aStatedFigureStandsOverTheDerivedOneFigureByFigure() {
        val stated =
            ProfileCharacteristics(
                distance = 24_300.0,
                duration = 32_400.0,
                durationPartial = false,
                ascent = 1_531,
                descent = null,
                altitudeMin = 1_035,
                altitudeMax = null,
                slopeMin = -15.2,
                slopeMax = null,
                stopDuration = null,
                trailDifficultyMin = t1,
                trailDifficultyMax = t3,
            )
        val resolved = resolveCharacteristics(derived, stated)

        assertEquals(24_300.0, resolved.distance)
        assertEquals(32_400.0, resolved.duration)
        assertEquals(1_531.0, resolved.ascent)
        assertEquals(1_401.0, resolved.descent)
        assertEquals(1_035.0, resolved.altitudeMin)
        assertEquals(1_852.0, resolved.altitudeMax)
        assertEquals(-15.2, resolved.slopeMin)
        assertEquals(17.9, resolved.slopeMax)
        assertEquals(t1, resolved.trailDifficultyMin)
        assertEquals(t3, resolved.trailDifficultyMax)
    }
}
