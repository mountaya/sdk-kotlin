package com.mountaya.geo.profile.contracts

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ProfileCharacteristics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProfileCharacteristicsTest {
    private fun summary(
        distance: Double? = 12_317.0,
        partial: Boolean = false,
    ) = ProfileCharacteristics(
        distance,
        16_200.0,
        partial,
        1_345,
        280,
        1_035,
        2_100,
        -9.0,
        22.0,
        null,
        RestSchema.TrailDifficultyType.Hiking,
        RestSchema.TrailDifficultyType.AlpineHiking,
    )

    @Test
    fun twoSummariesAreEqualWhenEveryFigureIs() {
        assertEquals(summary(), summary())
        assertEquals(summary().hashCode(), summary().hashCode())
        assertNotEquals(summary(), summary(partial = true))
        assertNotEquals(summary(distance = 0.0), summary(distance = -0.0))
        assertEquals(
            "ProfileCharacteristics(distance=12317.0, duration=16200.0, durationPartial=false, " +
                "ascent=1345, descent=280, " +
                "altitudeMin=1035, altitudeMax=2100, slopeMin=-9.0, slopeMax=22.0, stopDuration=null, " +
                "trailDifficultyMin=hiking, trailDifficultyMax=alpine-hiking)",
            summary().toString(),
        )
    }
}
