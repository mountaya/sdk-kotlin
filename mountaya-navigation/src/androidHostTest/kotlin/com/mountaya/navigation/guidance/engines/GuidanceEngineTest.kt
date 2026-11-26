package com.mountaya.navigation

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.FollowedRoute
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.NavigationState
import com.mountaya.geo.NavigationStatus
import com.mountaya.geo.NavigationStep
import com.mountaya.geo.followedRouteOf
import com.mountaya.geo.guidancePlanOf
import com.mountaya.geo.navigationProgressAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The guidance of one navigation, handed the states a machine commits: it keeps to the route followed,
 * lays each one out afresh, and says each prompt the state's own progress finds due, once per route and
 * only while the navigation is `navigating`.
 */
class GuidanceEngineTest {
    /** Departs east for about 1,112 m, turns north onto a path for as long, and arrives at its end. */
    private val turning: FollowedRoute =
        assertNotNull(
            followedRouteOf(
                NavigationRoute(
                    listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.01, 0.01)),
                    steps =
                        listOf(
                            NavigationStep(0, 1, RestSchema.ManeuverType.Depart, name = "Chemin"),
                            NavigationStep(1, 2, RestSchema.ManeuverType.TurnLeft, name = "Sentier"),
                            NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                        ),
                ),
            ),
            "the route is one a navigation follows",
        )

    /** Two stretches east along the equator, followed as a departure and an arrival. */
    private val eastward: FollowedRoute =
        assertNotNull(
            followedRouteOf(NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.02, 0.0)))),
            "the route is one a navigation follows",
        )

    /** A route as it lands, read at no position yet. */
    private fun landed(route: FollowedRoute) = NavigationState(NavigationStatus.Navigating, route)

    /** [route] read at [position], in [status]. */
    private fun at(
        route: FollowedRoute,
        position: List<Double>,
        status: NavigationStatus = NavigationStatus.Navigating,
    ) = NavigationState(status, route, navigationProgressAt(route, position, 0.0))

    @Test
    fun eachRouteFollowedIsLaidOutAfreshAndAnotherHushesTheVoice() {
        val voice = RecordingVoice()
        val guidance = GuidanceEngine(voice)

        assertTrue(guidance.follow(turning))
        assertEquals(guidancePlanOf(turning), guidance.plan)
        assertEquals(1, voice.hushed)
        assertFalse(guidance.follow(turning), "the route guided is no other")
        assertFalse(
            guidance.follow(followedRouteOf(NavigationRoute(turning.line, turning.steps))),
            "nor one equal to it",
        )
        assertEquals(1, voice.hushed)

        assertTrue(guidance.follow(eastward))
        assertEquals(2, voice.hushed)
        assertTrue(guidance.follow(null))
        assertNull(guidance.route)
        assertNull(guidance.plan)
        assertEquals(3, voice.hushed)
    }

    /** The departure as the route lands, the turn 55 m before it, the arrival 55 m before the end, each once. */
    @Test
    fun eachPromptDueIsSaidOncePerRouteWhereTheStatesProgressFindsIt() {
        val voice = RecordingVoice()
        val guidance = GuidanceEngine(voice)
        guidance.follow(turning)

        guidance.guide(landed(turning))
        guidance.guide(at(turning, listOf(0.002, 0.0)))
        assertEquals(listOf(NavigationInstructionKey.Depart), voice.said.map { it.key })

        guidance.guide(at(turning, listOf(0.0097, 0.0)))
        guidance.guide(at(turning, listOf(0.0098, 0.0)))
        guidance.guide(at(turning, listOf(0.01, 0.0097)))
        assertEquals(
            listOf(
                NavigationInstructionKey.Depart,
                NavigationInstructionKey.TurnLeft,
                NavigationInstructionKey.GoalPlain,
            ),
            voice.said.map { it.key },
        )
        assertEquals(listOf("Chemin", "Sentier", null), voice.said.map { it.name })

        // Followed again, the route is laid out afresh, and each prompt is due once more.
        guidance.follow(eastward)
        guidance.follow(turning)
        guidance.guide(landed(turning))
        assertEquals(4, voice.said.size)
        assertEquals(NavigationInstructionKey.Depart, voice.said.last().key)
    }

    @Test
    fun nothingIsSaidInAnyStatusButNavigating() {
        val voice = RecordingVoice()
        val guidance = GuidanceEngine(voice)
        guidance.follow(turning)
        val nearTheTurn = listOf(0.0097, 0.0)

        for (status in NavigationStatus.entries - NavigationStatus.Navigating) {
            guidance.guide(at(turning, nearTheTurn, status))
        }
        assertEquals(emptyList(), voice.said, "off the route, rerouting or arrived, nothing is said")

        guidance.guide(at(turning, nearTheTurn))
        assertEquals(listOf(NavigationInstructionKey.TurnLeft), voice.said.map { it.key })
    }

    @Test
    fun aStateOfAnotherRouteOrOfARouteThatNamesNoPlaceSaysNothing() {
        val voice = RecordingVoice()
        val guidance = GuidanceEngine(voice)

        guidance.guide(landed(turning))
        guidance.follow(eastward)
        guidance.guide(landed(turning))
        assertEquals(emptyList(), voice.said, "only the route guided is guided")

        val placeless =
            assertNotNull(
                followedRouteOf(NavigationRoute(listOf(listOf(Double.NaN, 1.0), listOf(2.0)))),
                "the route is one a navigation follows",
            )
        assertTrue(guidance.follow(placeless))
        assertNull(guidance.plan)
        guidance.guide(landed(placeless))
        assertEquals(emptyList(), voice.said)
    }

    @Test
    fun aHaltForgetsTheRouteAndWhatWasSaidAndHushesTheVoice() {
        val voice = RecordingVoice()
        val guidance = GuidanceEngine(voice)
        guidance.follow(turning)
        guidance.guide(landed(turning))

        guidance.halt()
        assertEquals(2, voice.hushed)
        assertNull(guidance.route)
        assertNull(guidance.plan)
        guidance.guide(landed(turning))
        assertEquals(1, voice.said.size, "a halted guidance says nothing")

        assertTrue(guidance.follow(turning))
        guidance.guide(landed(turning))
        assertEquals(
            listOf(NavigationInstructionKey.Depart, NavigationInstructionKey.Depart),
            voice.said.map { it.key },
        )
    }
}
