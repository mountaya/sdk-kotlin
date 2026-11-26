package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ResolvedCharacteristicsTest {
    private val t3 = RestSchema.TrailDifficultyType("T3")

    @Test
    fun everyFigureIsNullUnlessGivenAndTwoAreEqualWhereEveryFigureIs() {
        val empty = ResolvedCharacteristics()
        assertNull(empty.distance)
        assertNull(empty.trailDifficultyMax)
        assertNull(empty.slopeMin)
        val figures = ResolvedCharacteristics(distance = 24_300.0, ascent = 1_531.0, trailDifficultyMax = t3)

        assertEquals(ResolvedCharacteristics(distance = 24_300.0, ascent = 1_531.0, trailDifficultyMax = t3), figures)
        assertEquals(
            ResolvedCharacteristics(distance = 24_300.0, ascent = 1_531.0, trailDifficultyMax = t3).hashCode(),
            figures.hashCode(),
        )
        assertNotEquals(
            ResolvedCharacteristics(
                distance = 24_300.0,
                ascent = 1_531.0,
                trailDifficultyMax = RestSchema.TrailDifficultyType("T4"),
            ),
            figures,
        )
        assertNotEquals(ResolvedCharacteristics(ascent = -0.0), ResolvedCharacteristics(ascent = 0.0))
    }
}
