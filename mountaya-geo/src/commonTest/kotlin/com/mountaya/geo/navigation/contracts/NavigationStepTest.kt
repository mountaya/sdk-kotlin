package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class NavigationStepTest {
    private val turn = RestSchema.ManeuverType.TurnLeft

    @Test
    fun twoStepsAreEqualWhereEveryMemberIsTheSameNumber() {
        val step = NavigationStep(0, 2, turn, "Chemin du Lac", 900.0, 600.0, null)

        assertEquals(step, NavigationStep(0, 2, turn, "Chemin du Lac", 900.0, 600.0, null))
        assertEquals(step.hashCode(), NavigationStep(0, 2, turn, "Chemin du Lac", 900.0, 600.0, null).hashCode())
        assertEquals(
            NavigationStep(0, 1, turn, duration = Double.NaN),
            NavigationStep(0, 1, turn, duration = Double.NaN),
        )
        assertNotEquals(NavigationStep(0, 1, turn, distance = 0.0), NavigationStep(0, 1, turn, distance = -0.0))
        assertNotEquals(step, NavigationStep(0, 2, turn, null, 900.0, 600.0, null))
        assertNotEquals(step, NavigationStep(0, 2, turn, "Chemin du Lac", 900.0, 600.0, 2))
    }

    /** A figure nobody stated is none, never a zero. */
    @Test
    fun aStepStatesOnlyItsRangeAndItsManeuver() {
        val step = NavigationStep(fromIndex = 3, toIndex = 3, maneuver = RestSchema.ManeuverType.Goal)

        assertNull(step.name)
        assertNull(step.distance)
        assertNull(step.duration)
        assertNull(step.exitNumber)
        assertEquals(
            "NavigationStep(3..3, goal, name=null, distance=null, duration=null, exitNumber=null)",
            step.toString(),
        )
    }
}
