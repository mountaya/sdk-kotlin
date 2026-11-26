package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigationRouteTest {
    private val line = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0))

    @Test
    fun aRouteStatesItsLineAloneWhereItHasNoStepsAndNoDuration() {
        val route = NavigationRoute(line)

        assertTrue(route.steps.isEmpty())
        assertNull(route.duration)
    }

    @Test
    fun twoRoutesAreEqualByWhatTheyHold() {
        val steps = listOf(NavigationStep(0, 1, RestSchema.ManeuverType.Depart))

        assertEquals(NavigationRoute(line, steps, 600.0), NavigationRoute(line.map { it.toList() }, steps, 600.0))
        assertEquals(
            NavigationRoute(line, steps, 600.0).hashCode(),
            NavigationRoute(
                line.map {
                    it.toList()
                },
                steps,
                600.0,
            ).hashCode(),
        )
        assertEquals(NavigationRoute(listOf(listOf(Double.NaN, 0.0))), NavigationRoute(listOf(listOf(Double.NaN, 0.0))))
        assertNotEquals(NavigationRoute(listOf(listOf(0.0, 0.0))), NavigationRoute(listOf(listOf(-0.0, 0.0))))
        assertNotEquals(NavigationRoute(line, duration = 600.0), NavigationRoute(line))
        assertNotEquals(NavigationRoute(line, steps), NavigationRoute(line))
    }
}
