package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GuidancePlanOfTest {
    private fun plan(route: NavigationRoute): GuidancePlan =
        assertNotNull(
            guidancePlanOf(assertNotNull(followedRouteOf(route), "the route is one a navigation follows")),
            "the route has a guidance plan",
        )

    @Test
    fun aRouteWithNoStepsDepartsOverItsWholeLineThenAnnouncesTheArrival() {
        val line = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.02, 0.0))
        val length = routeLengthMeters(line)
        val laid = plan(NavigationRoute(line, duration = 1_200.0))

        assertEquals(2, laid.steps.size)
        assertEquals(length, laid.steps[0].meters)
        assertEquals(NavigationInstructionKey.DepartPlain, laid.steps[0].instruction.key)
        assertEquals(listOf(0, 1), laid.steps[0].prompts.map { it.id })
        assertEquals(
            listOf(NavigationInstructionKey.DepartPlain, NavigationInstructionKey.GoalPlain),
            laid.steps[0].prompts.map { it.instruction.key },
        )
        assertEquals(listOf(length, PROMPT_METERS), laid.steps[0].prompts.map { it.meters })
        assertEquals(0.0, laid.steps[1].meters)
        assertEquals(NavigationInstructionKey.GoalPlain, laid.steps[1].instruction.key)
        assertTrue(laid.steps[1].prompts.isEmpty(), "the last step leads to no maneuver")
    }

    @Test
    fun eachStepAnnouncesTheManeuverThatEndsItTheFarthestPromptFirst() {
        val line = listOf(listOf(0.0, 0.0), listOf(0.0002, 0.0), listOf(0.0002, 0.005), listOf(0.0002, 0.01))
        val route =
            NavigationRoute(
                line,
                steps =
                    listOf(
                        NavigationStep(0, 1, RestSchema.ManeuverType.Depart, name = "Chemin"),
                        NavigationStep(
                            1,
                            3,
                            RestSchema.ManeuverType.TurnLeft,
                            name = "Sentier",
                            duration = 300.0,
                        ),
                        NavigationStep(3, 3, RestSchema.ManeuverType.Goal),
                    ),
            )
        val measured = cumulativeMeters(line)
        val laid = plan(route)

        assertEquals(listOf(measured[1], measured[3] - measured[1], 0.0), laid.steps.map { it.meters })
        assertEquals(
            listOf(
                NavigationInstructionKey.Depart,
                NavigationInstructionKey.TurnLeft,
                NavigationInstructionKey.GoalPlain,
            ),
            laid.steps.map { it.instruction.key },
        )
        // The first step is 22 m, shorter than the prompt's lead: the turn is said as it begins.
        assertEquals(listOf(measured[1], measured[1]), laid.steps[0].prompts.map { it.meters })
        assertEquals(listOf("Chemin", "Sentier"), laid.steps[0].prompts.map { it.instruction.name })
        assertEquals(listOf(PROMPT_METERS), laid.steps[1].prompts.map { it.meters })
        assertEquals(listOf(NavigationInstructionKey.GoalPlain), laid.steps[1].prompts.map { it.instruction.key })
        assertEquals(listOf(0, 1, 2), laid.steps.flatMap { it.prompts }.map { it.id })
    }

    @Test
    fun aVertexThatNamesNoPlaceIsBridgedByTheLength() {
        val laid =
            plan(NavigationRoute(listOf(listOf(0.0, 0.0, 120.0), listOf(Double.NaN, 0.0), listOf(0.01, 0.0, 150.0))))

        assertEquals(haversineMeters(listOf(0.0, 0.0), listOf(0.01, 0.0)), laid.steps[0].meters)
    }

    @Test
    fun aStretchWithNoVertexThatNamesAPlaceIsNoLengthAtAll() {
        val route =
            NavigationRoute(
                listOf(
                    listOf(0.0, 0.0),
                    listOf(Double.NaN, Double.NaN),
                    listOf(Double.NaN, Double.NaN),
                    listOf(0.01, 0.0),
                ),
                steps =
                    listOf(
                        NavigationStep(0, 1, RestSchema.ManeuverType.Depart),
                        NavigationStep(1, 2, RestSchema.ManeuverType.Continue),
                        NavigationStep(2, 3, RestSchema.ManeuverType.TurnRight),
                        NavigationStep(3, 3, RestSchema.ManeuverType.Goal),
                    ),
            )

        assertEquals(
            listOf(0.0, 0.0, haversineMeters(listOf(0.0, 0.0), listOf(0.01, 0.0)), 0.0),
            plan(route).steps.map { it.meters },
        )
    }

    /**
     * No step of a route of one vertex covers any ground, so there is nowhere to move a prompt to: each stays on
     * its step, the departure with it.
     */
    @Test
    fun aRouteOfOneVertexIsNoLengthKeepsItsPromptsAndOneWhoseVerticesNameNoPlaceHasNoPlan() {
        val single = plan(NavigationRoute(listOf(listOf(6.8652, 45.9237))))
        assertTrue(single.steps.all { it.meters == 0.0 })
        assertEquals(listOf(0, 1), single.steps.flatMap { it.prompts }.map { it.id })
        assertEquals(
            listOf(NavigationInstructionKey.DepartPlain, NavigationInstructionKey.GoalPlain),
            single.steps[0].prompts.map { it.instruction.key },
        )
        assertEquals(listOf(0.0, 0.0), single.steps[0].prompts.map { it.meters })

        val placeless =
            assertNotNull(
                followedRouteOf(NavigationRoute(listOf(listOf(Double.NaN, 1.0), listOf(2.0)))),
                "the route is one a navigation follows",
            )
        assertNull(guidancePlanOf(placeless))
    }

    /** About 1,112 m east, then 1,112 m north, then 1,112 m east again, as a line of four vertices. */
    private val zigzag = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.01, 0.01), listOf(0.02, 0.01))

    /**
     * A step of no length is never where the traveller's progress stands, so the maneuver it announces would
     * never be said: it is announced from the step before it, due as that step ends, at the vertex the two
     * maneuvers share, after the step's own announcement 50 m before.
     */
    @Test
    fun aStepOfNoLengthMidRouteHasItsPromptsMovedToTheEndOfTheStepBeforeIt() {
        val route =
            NavigationRoute(
                zigzag.take(3),
                steps =
                    listOf(
                        NavigationStep(0, 1, RestSchema.ManeuverType.Depart, name = "Chemin"),
                        NavigationStep(1, 1, RestSchema.ManeuverType.TurnLeft, name = "Pont"),
                        NavigationStep(1, 2, RestSchema.ManeuverType.TurnRight, name = "Sentier"),
                        NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                    ),
            )
        val measured = cumulativeMeters(route.line)
        val laid = plan(route)

        assertEquals(listOf(measured[1], 0.0, measured[2] - measured[1], 0.0), laid.steps.map { it.meters })
        assertEquals(
            listOf(
                NavigationInstructionKey.Depart,
                NavigationInstructionKey.TurnLeft,
                NavigationInstructionKey.TurnRight,
            ),
            laid.steps[0].prompts.map { it.instruction.key },
        )
        assertEquals(listOf(measured[1], PROMPT_METERS, 0.0), laid.steps[0].prompts.map { it.meters })
        assertTrue(laid.steps[1].prompts.isEmpty(), "a step of no length keeps no prompt")
        assertEquals(listOf(NavigationInstructionKey.GoalPlain), laid.steps[2].prompts.map { it.instruction.key })
        assertEquals(listOf(0, 1, 2, 3), laid.steps.flatMap { it.prompts }.map { it.id })
    }

    /**
     * Steps of no length one after another mid-route: every maneuver they announce moves to the end of the step
     * before them, in travel order, so `duePrompt` says the first 50 m out and the last at the vertex.
     */
    @Test
    fun consecutiveStepsOfNoLengthMidRouteMoveEveryPromptToTheStepBeforeThemInTravelOrder() {
        val route =
            NavigationRoute(
                zigzag.take(3),
                steps =
                    listOf(
                        NavigationStep(0, 1, RestSchema.ManeuverType.Depart),
                        NavigationStep(1, 1, RestSchema.ManeuverType.TurnLeft),
                        NavigationStep(1, 1, RestSchema.ManeuverType.Continue),
                        NavigationStep(1, 2, RestSchema.ManeuverType.TurnRight),
                        NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                    ),
            )
        val laid = plan(route)

        assertEquals(
            listOf(
                NavigationInstructionKey.DepartPlain,
                NavigationInstructionKey.TurnLeftPlain,
                NavigationInstructionKey.ContinuePlain,
                NavigationInstructionKey.TurnRightPlain,
            ),
            laid.steps[0].prompts.map { it.instruction.key },
        )
        assertEquals(
            listOf(PROMPT_METERS, 0.0, 0.0),
            laid.steps[0]
                .prompts
                .drop(1)
                .map { it.meters },
        )
        assertTrue(laid.steps[1].prompts.isEmpty() && laid.steps[2].prompts.isEmpty())
        assertEquals(listOf(0, 1, 2, 3, 4), laid.steps.flatMap { it.prompts }.map { it.id })
    }

    /**
     * Steps of no length that open the route cover no ground before the first step that does: their departure
     * is dropped, and what they announce is due as that first step begins, before its own prompts.
     */
    @Test
    fun stepsOfNoLengthThatOpenTheRouteDropTheDepartureAndAreDueAsTheFirstStepOfSomeLengthBegins() {
        val route =
            NavigationRoute(
                zigzag.take(3),
                steps =
                    listOf(
                        NavigationStep(0, 0, RestSchema.ManeuverType.Depart, name = "Parking"),
                        NavigationStep(0, 0, RestSchema.ManeuverType.Continue),
                        NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft, name = "Chemin"),
                        NavigationStep(1, 2, RestSchema.ManeuverType.TurnRight),
                        NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                    ),
            )
        val measured = cumulativeMeters(route.line)
        val laid = plan(route)

        assertTrue(laid.steps[0].prompts.isEmpty() && laid.steps[1].prompts.isEmpty())
        assertEquals(
            listOf(
                NavigationInstructionKey.ContinuePlain,
                NavigationInstructionKey.TurnLeft,
                NavigationInstructionKey.TurnRightPlain,
            ),
            laid.steps[2].prompts.map { it.instruction.key },
        )
        assertEquals(listOf(measured[1], measured[1], PROMPT_METERS), laid.steps[2].prompts.map { it.meters })
        assertTrue(
            laid.steps.flatMap { it.prompts }.none { it.instruction.key == NavigationInstructionKey.Depart },
            "the departure covers no ground and is dropped",
        )
        assertEquals(listOf(0, 1, 2, 3), laid.steps.flatMap { it.prompts }.map { it.id })
    }

    @Test
    fun thePromptsLeadIsFiftyMeters() {
        assertEquals(50.0, PROMPT_METERS)
    }

    /**
     * The plan reads the measure the route was followed with, never measuring the line again, so a step's
     * length is the one the traveller is told.
     */
    @Test
    fun thePlanReadsTheMeasureTheRouteWasFollowedWith() {
        val followed =
            assertNotNull(
                followedRouteOf(
                    NavigationRoute(
                        listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(Double.NaN, 1.0), listOf(0.01, 0.01)),
                    ),
                ),
                "the route is one a navigation follows",
            )
        val laid = assertNotNull(guidancePlanOf(followed), "the route has a guidance plan")

        assertEquals(
            followed.steps.map { followed.measured[it.toIndex] - followed.measured[it.fromIndex] },
            laid.steps.map { it.meters },
        )
    }
}
