package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GuidanceStepTest {
    private val depart = stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.Depart))
    private val left = stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft))

    @Test
    fun twoStepsAreEqualWhereEveryMemberIs() {
        val step = GuidanceStep(1_112.0, depart, emptyList())
        assertEquals(GuidanceStep(1_112.0, depart, emptyList()), step)
        assertNotEquals(GuidanceStep(1_000.0, depart, emptyList()), step)
        assertNotEquals(GuidanceStep(1_112.0, left, emptyList()), step)
        assertNotEquals(GuidanceStep(1_112.0, depart, listOf(GuidancePrompt(0, depart, 1_112.0))), step)
    }
}
