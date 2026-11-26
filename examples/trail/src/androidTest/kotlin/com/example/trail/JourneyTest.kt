package com.example.trail

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.geo.followedRouteOf
import com.mountaya.geo.stepInstruction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** `journey.replayMetersPerSecond` of the contract's `sdk/trail.yaml`: the replay speed only a test hands the app. */
private const val REPLAY_METERS_PER_SECOND = 100.0

/** `journey.arrivalSeconds`: the longest the arrival may take after the press on Start. */
private const val ARRIVAL_SECONDS = 30

/** `journey.waypoints`: the names of the waypoint instructions the demo's route yields, in travel order. */
private val waypoints = listOf("Les Nants", "Clairière des Nants")

/**
 * The journey of the contract's `sdk/trail.yaml`, walked through Trail itself: no setting is set, so the app shows its
 * notice and holds the demo itinerary, and Replay walks the route at the journey's speed. Each check is the journey's, in
 * its order, by the accessibility id it acts on.
 */
class JourneyTest {
    @get:Rule
    val compose = createComposeRule()

    /** Every sentence the voice may say on the demo's route, one for each step of the route the engine follows. */
    private val said: Set<String> by lazy {
        val demo = TrailItinerary.demo(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        val route = assertNotNull(followedRouteOf(joinedRoute(demo)), "the demo's route is one to follow")
        route.steps.map { sentence(stepInstruction(it)) }.toSet()
    }

    /** The instruction shown, or none. */
    private fun instructions(): List<String> =
        compose.onAllNodesWithTag("trail.instruction").fetchSemanticsNodes().map { node ->
            node.config[SemanticsProperties.Text].joinToString("") { it.text }
        }

    @Test
    fun theJourneyFollowsTheDemoItineraryPastItsWaypointsToItsEnd() {
        compose.setContent { Trail(TrailSettings(replayMetersPerSecond = REPLAY_METERS_PER_SECOND)) }

        // trail.notice, see: the notice stands in place of the map and the search, since no publishable key is set.
        compose.onNodeWithTag("trail.notice").assertIsDisplayed()
        // trail.profile, see: the profile draws the demo itinerary's elevation, since no itinerary id is set.
        compose.onNodeWithTag("trail.profile").assertIsDisplayed()
        // trail.replay, press: Replay is switched on, so the route is walked at the journey's speed.
        compose.onNodeWithTag("trail.replay").performClick().assertIsOn()
        // trail.start, press: Start begins following the demo itinerary, its segments joined into one route.
        compose.onNodeWithTag("trail.start").performClick()
        val pressed = TimeSource.Monotonic.markNow()

        // trail.instruction, read: an instruction is shown, in the wording the voice says it with.
        compose.waitUntil(ARRIVAL_SECONDS * 1_000L) { instructions().isNotEmpty() }
        val first = instructions().single()
        assertTrue(first in said, "`$first` is no sentence the voice says on the demo's route: $said")

        // trail.instruction, read: a waypoint's instruction is shown at least once, said with the `waypoint` key and
        // naming one of the journey's waypoints; trail.arrived, see: the arrival, within the journey's time of Start.
        val shown = mutableSetOf(first)
        compose.waitUntil(ARRIVAL_SECONDS * 1_000L) {
            shown += instructions()
            compose.onAllNodesWithTag("trail.arrived").fetchSemanticsNodes().isNotEmpty()
        }
        val took = pressed.elapsedNow()
        val passed = waypoints.map { name -> "Pass $name" }
        assertTrue(shown.any { it in passed }, "no instruction named a waypoint of $waypoints: $shown")
        assertTrue(took <= ARRIVAL_SECONDS.seconds, "the arrival took $took after Start")
        compose.onNodeWithTag("trail.arrived").assertIsDisplayed()

        // trail.stop, press: Stop ends the navigation, and Start is shown again.
        compose.onNodeWithTag("trail.stop").performClick()
        compose.onNodeWithTag("trail.start").assertIsDisplayed()
    }
}
