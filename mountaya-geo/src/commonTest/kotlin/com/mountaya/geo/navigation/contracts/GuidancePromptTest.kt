package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GuidancePromptTest {
    private val left = stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft))

    @Test
    fun twoPromptsAreEqualWhereTheyHoldOnePlaceOneInstructionAndOneDistance() {
        assertEquals(GuidancePrompt(1, left, 50.0), GuidancePrompt(1, left, 50.0))
        assertNotEquals(GuidancePrompt(1, left, 50.0), GuidancePrompt(2, left, 50.0))
        assertNotEquals(GuidancePrompt(1, left, 50.0), GuidancePrompt(1, left, 40.0))
    }

    /** A trigger compares as every number Geo derives does: by its bits, `NaN` the same as `NaN`. */
    @Test
    fun aTriggerComparesAsEveryGeoNumberDoes() {
        assertEquals(GuidancePrompt(1, left, Double.NaN), GuidancePrompt(1, left, Double.NaN))
        assertNotEquals(GuidancePrompt(1, left, 0.0), GuidancePrompt(1, left, -0.0))
    }
}
