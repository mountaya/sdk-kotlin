package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GuidancePlanTest {
    private val depart = stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.Depart))
    private val arrive = stepInstruction(NavigationStep(1, 1, RestSchema.ManeuverType.Goal))

    private fun plan(meters: Double = 100.0) =
        GuidancePlan(
            listOf(
                GuidanceStep(
                    meters,
                    depart,
                    listOf(GuidancePrompt(0, depart, meters), GuidancePrompt(1, arrive, 50.0)),
                ),
                GuidanceStep(0.0, arrive, emptyList()),
            ),
        )

    @Test
    fun twoPlansAreEqualWhereEveryStepIs() {
        assertEquals(plan(), plan())
        assertEquals(plan(), GuidancePlan(plan().steps.toList()))
        assertEquals(plan().hashCode(), GuidancePlan(plan().steps.toList()).hashCode())
        assertNotEquals(plan(), plan(meters = 101.0))
        assertNotEquals(plan(), GuidancePlan(plan().steps.take(1)))
        assertEquals(listOf(0, 1), plan().steps.flatMap { step -> step.prompts.map { it.id } })
    }
}
