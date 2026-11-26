package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * The prompt due is read from Geo's progress, by the trigger rule every SDK applies: of the prompts of the
 * step the traveller is in, the last whose trigger stands within 5 m of the distance left to the step's end.
 */
class DuePromptTest {
    /** Departs east for about 1,112 m, turns north for as long, and arrives at its end. */
    private val route: FollowedRoute =
        assertNotNull(
            followedRouteOf(
                NavigationRoute(
                    listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.01, 0.01)),
                    steps =
                        listOf(
                            NavigationStep(0, 1, RestSchema.ManeuverType.Depart),
                            NavigationStep(1, 2, RestSchema.ManeuverType.TurnLeft),
                            NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                        ),
                ),
            ),
            "the route is one a navigation follows",
        )
    private val plan: GuidancePlan = assertNotNull(guidancePlanOf(route), "the route has a guidance plan")

    /** The progress of a traveller in [step] with [left] meters to its end. */
    private fun at(
        step: Int,
        left: Double,
    ) = NavigationProgress(0.0, step, step + 1, NavigationRemainder(left, null), NavigationRemainder(left, null))

    @Test
    fun beforeAnyPositionTheFirstStepsDepartureIsDue() {
        assertEquals(0, duePrompt(plan, null)?.id)
    }

    /** The farther a prompt is from its step's end, the earlier it stands, so the one nearest the maneuver wins. */
    @Test
    fun ofTheStepsPromptsDueTheLastWins() {
        assertEquals(0, duePrompt(plan, at(0, 400.0))?.id, "far from the turn, the departure stands")
        assertEquals(1, duePrompt(plan, at(0, 30.0))?.id, "within 50 m of the turn, the turn stands")
        assertEquals(2, duePrompt(plan, at(1, 20.0))?.id, "the arrival, from the turn's step")
    }

    /** A prompt read a little early is said rather than missed between two positions: 5 m of slack, no more. */
    @Test
    fun aPromptFallsDueWithinFiveMetersOfItsTrigger() {
        assertEquals(1, duePrompt(plan, at(0, 50.0 + PROMPT_SLACK_METERS))?.id)
        assertEquals(0, duePrompt(plan, at(0, 50.0 + PROMPT_SLACK_METERS + 0.01))?.id)
    }

    @Test
    fun aStepPastThePlansLastReadsItsLastAndAStepWithNoPromptHasNoneDue() {
        assertNull(duePrompt(plan, at(5, 0.0)), "the last step announces nothing")
        assertNull(duePrompt(plan, at(2, 0.0)))
        assertNull(duePrompt(GuidancePlan(emptyList()), null))
    }

    /** The distance read is Geo's own, so every SDK finds a prompt due at the same metre of the route. */
    @Test
    fun aPositionOnTheRouteIsReadWhereGeosProgressReadsIt() {
        val nearTheTurn = navigationProgressAt(route, listOf(0.0097, 0.0), 0.0)
        val farFromIt = navigationProgressAt(route, listOf(0.002, 0.0), 0.0)

        assertEquals(1, duePrompt(plan, nearTheTurn)?.id)
        assertEquals(0, duePrompt(plan, farFromIt)?.id)
    }
}
