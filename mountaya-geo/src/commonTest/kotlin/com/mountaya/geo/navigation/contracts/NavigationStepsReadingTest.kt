package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NavigationStepsReadingTest {
    private val steps = listOf(NavigationStep(0, 1, RestSchema.ManeuverType.Depart))

    @Test
    fun twoReadingsAreEqualWhereBothMembersAre() {
        assertEquals(NavigationStepsReading(false, steps), NavigationStepsReading(false, steps.toList()))
        assertEquals(
            NavigationStepsReading(false, steps).hashCode(),
            NavigationStepsReading(false, steps.toList()).hashCode(),
        )
        assertNotEquals(NavigationStepsReading(true, emptyList()), NavigationStepsReading(false, emptyList()))
        assertEquals(
            "NavigationStepsReading(refused=true, steps=[])",
            NavigationStepsReading(true, emptyList()).toString(),
        )
    }
}
