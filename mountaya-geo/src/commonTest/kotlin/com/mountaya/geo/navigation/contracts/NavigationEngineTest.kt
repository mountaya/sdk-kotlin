package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The engine contract every `NavigationEngine` answers alike, held here on the one engine this
 * module ships, the scripted one, which follows a route over the same rules as every other: the
 * three edges the contract leaves open (decided as `sdk-typescript`'s engine decides them), and what
 * the state reports, when, and in what order.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NavigationEngineTest {
    /** Two stretches east along the equator, followed as a departure and an arrival. */
    private val eastward =
        NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.02, 0.0)), duration = 1_200.0)

    private val length = routeLengthMeters(eastward.line)

    /** A position this many meters north of the equator at [longitude]. */
    private fun north(
        longitude: Double,
        meters: Double,
    ) = listOf(longitude, meters / METERS_PER_DEGREE_MEAN_SPHERE)

    /** An engine started and answered with [route]. */
    private fun navigating(route: NavigationRoute = eastward): ScriptedNavigationEngine<String> =
        ScriptedNavigationEngine<String>().apply {
            start("summit")
            sent.single().answer(route)
        }

    /** Every state [engine] reports from now on, its current one first, as a collector that misses none sees them. */
    private fun TestScope.recorded(engine: NavigationEngine<String>): List<NavigationState> {
        val seen = mutableListOf<NavigationState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.state.toList(seen) }
        return seen
    }

    @Test
    fun anEngineIsIdleAndAsksForNothingUntilItIsStarted() {
        val engine = ScriptedNavigationEngine<String>()

        assertEquals(NavigationState.idle, engine.state.value)
        assertTrue(engine.sent.isEmpty())
    }

    @Test
    fun aStartRoutesAndTheRouteThatLandsIsFollowedByItsSteps() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("summit")
        assertEquals(NavigationStatus.Routing, engine.state.value.status)
        assertEquals("summit", engine.sent.single().request)

        engine.sent.single().answer(eastward)
        val (status, route) = engine.state.value.let { it.status to it.route }
        assertEquals(NavigationStatus.Navigating, status)
        assertSame(eastward.line, route?.line)
        assertEquals(
            listOf(RestSchema.ManeuverType.Depart, RestSchema.ManeuverType.Goal),
            route?.steps?.map { it.maneuver },
        )
    }

    /** N12, the arrival: raised where the traveller is on the route and progress stands at its end. */
    @Test
    fun aTravellerArrivesOnTheRouteWithProgressAtItsEnd() {
        val engine = navigating()

        engine.setPosition(north(0.005, 10.0))
        assertEquals(NavigationStatus.Navigating, engine.state.value.status)
        assertEquals(
            900.0,
            engine.state.value.progress
                ?.toArrival
                ?.duration ?: Double.NaN,
            1e-6,
        )

        // Past the end, and within the threshold of it: the foot stands on the last vertex.
        engine.setPosition(listOf(0.0204, 0.0))
        assertEquals(NavigationStatus.Arrived, engine.state.value.status)
        assertEquals(
            NavigationRemainder(0.0, 0.0),
            engine.state.value.progress
                ?.toArrival,
        )
    }

    @Test
    fun aTravellerWhoStopsShortNeverArrives() {
        val engine = navigating()

        engine.setPosition(listOf(0.0199, 0.0))

        assertEquals(NavigationStatus.Navigating, engine.state.value.status)
        assertTrue(
            (
                engine.state.value.progress
                    ?.toArrival
                    ?.distance ?: 0.0
            ) > 0.0,
        )
    }

    /** At the end's foot but past the threshold beside it, the traveller is off the route, not there. */
    @Test
    fun aTravellerOffTheRouteAtItsEndHasNotArrived() {
        val engine = navigating()

        engine.setPosition(north(0.02, 60.0))

        assertEquals(NavigationStatus.OffRoute, engine.state.value.status)
        assertEquals(
            0.0,
            engine.state.value.progress
                ?.toArrival
                ?.distance,
        )
    }

    /**
     * N12, progress off the route: read on every position while a route is held, so a far-off position
     * can push it ahead for good, since progress never moves back.
     */
    @Test
    fun progressIsReadOnEveryPositionWhileARouteIsHeld() {
        val engine = navigating()

        engine.setPosition(north(0.019, 200.0))
        val pushed =
            engine.state.value.progress
                ?.along ?: Double.NaN
        assertEquals(NavigationStatus.OffRoute, engine.state.value.status)
        assertEquals(length * 0.95, pushed, 1.0)

        engine.reroute("from here")
        engine.setPosition(north(0.019, 300.0))
        assertEquals(NavigationStatus.Rerouting, engine.state.value.status)
        assertEquals(
            pushed,
            engine.state.value.progress
                ?.along ?: Double.NaN,
            1e-6,
        )

        engine.sent.last().fail(IllegalStateException("no signal"))
        engine.setPosition(listOf(0.005, 0.0))
        assertEquals(NavigationStatus.Navigating, engine.state.value.status)
        assertEquals(
            pushed,
            engine.state.value.progress
                ?.along ?: Double.NaN,
            1e-6,
        )
    }

    /** N12, the reroute: the host asks, through `reroute`, and nothing asks on its own. */
    @Test
    fun aNewRouteIsAskedForByRerouteAloneAndOnlyOffTheRoute() {
        val engine = navigating()

        engine.reroute("on the route")
        assertEquals(1, engine.sent.size)

        engine.setPosition(north(0.01, 200.0))
        engine.setPosition(north(0.01, 300.0))
        assertEquals(NavigationStatus.OffRoute, engine.state.value.status)
        assertEquals(1, engine.sent.size, "off the route, no request leaves on its own")

        engine.reroute("from here")
        engine.reroute("from here, again")
        assertEquals(NavigationStatus.Rerouting, engine.state.value.status)
        assertEquals(listOf("summit", "from here"), engine.sent.map { it.request })

        val failure = IllegalStateException("no signal")
        engine.sent.last().fail(failure)
        assertEquals(NavigationStatus.OffRoute, engine.state.value.status)
        assertSame(failure, (engine.state.value.error as? NavigationError.RouteFailed)?.cause)
        assertSame(
            eastward.line,
            engine.state.value.route
                ?.line,
        )
        assertEquals(2, engine.sent.size, "a failed reroute is not retried")

        engine.reroute("from here, once more")
        assertEquals(3, engine.sent.size)
    }

    @Test
    fun aNewRouteThatLandsIsFollowedAndTheLastPositionReadOnIt() {
        val engine = navigating()
        val from = north(0.01, 200.0)

        engine.setPosition(from)
        engine.reroute("from here")
        engine.setPosition(north(0.01, 0.0))
        assertEquals(
            NavigationStatus.Rerouting,
            engine.state.value.status,
            "a position moves nothing while it is awaited",
        )

        val back = NavigationRoute(listOf(north(0.01, 200.0), listOf(0.02, 0.0)), duration = 600.0)
        engine.setPosition(from)
        engine.sent.last().answer(back)

        assertEquals(NavigationStatus.Navigating, engine.state.value.status)
        assertSame(
            back.line,
            engine.state.value.route
                ?.line,
        )
        assertEquals(
            0.0,
            engine.state.value.progress
                ?.along,
        )
        assertNull(engine.state.value.error)
    }

    /**
     * What a renderer is told, and in what order: a route that lands with a position held is the
     * route first, unread, then that position's reading on it.
     */
    @Test
    fun aRouteThatLandsIsReportedThenThePositionHeldIsReadOnIt() =
        runTest {
            val engine = ScriptedNavigationEngine<String>()
            val seen = recorded(engine)

            engine.setPosition(listOf(0.005, 0.0))
            engine.start("summit")
            engine.sent.single().answer(eastward)

            assertEquals(
                listOf(
                    NavigationStatus.Idle,
                    NavigationStatus.Routing,
                    NavigationStatus.Navigating,
                    NavigationStatus.Navigating,
                ),
                seen.map { it.status },
            )
            assertNull(seen[2].progress)
            assertEquals(length / 4, seen[3].progress?.along ?: Double.NaN, 1e-6)
        }

    /**
     * One state a command at most: a position that reaches the end is reported arrived, never on the
     * route first, and one that moves nothing reports nothing.
     */
    @Test
    fun eachCommandReportsOneStateAtMostAndOnlyAChange() =
        runTest {
            val engine = navigating()
            val seen = recorded(engine)

            engine.setPosition(listOf(0.005, 0.0))
            engine.setPosition(listOf(0.005, 0.0))
            engine.reroute("on the route")
            engine.setPosition(listOf(0.0204, 0.0))
            engine.stop()
            engine.stop()

            assertEquals(
                listOf(
                    NavigationStatus.Navigating,
                    NavigationStatus.Navigating,
                    NavigationStatus.Arrived,
                    NavigationStatus.Idle,
                ),
                seen.map { it.status },
            )
            assertNull(seen[0].progress)
            assertEquals(NavigationState.idle, seen.last())
        }

    @Test
    fun aRerouteReportsReroutingThenTheNewRouteThenItsReading() =
        runTest {
            val engine = navigating()
            engine.setPosition(north(0.01, 200.0))
            val seen = recorded(engine)

            engine.reroute("from here")
            engine.sent.last().answer(NavigationRoute(listOf(north(0.01, 200.0), listOf(0.02, 0.0)), duration = 600.0))

            assertEquals(
                listOf(
                    NavigationStatus.OffRoute,
                    NavigationStatus.Rerouting,
                    NavigationStatus.Navigating,
                    NavigationStatus.Navigating,
                ),
                seen.map { it.status },
            )
            assertEquals(seen[0].route, seen[1].route)
            assertEquals(seen[0].progress, seen[1].progress)
            assertNull(seen[2].progress)
            assertEquals(0.0, seen[3].progress?.along)
        }

    @Test
    fun aPositionStatedWithNoRouteIsKeptAndReadOnTheRouteThatLandsNext() {
        val engine = ScriptedNavigationEngine<String>()

        engine.setPosition(listOf(0.005, 0.0))
        assertNull(engine.state.value.progress)
        engine.start("summit")
        engine.sent.single().answer(eastward)

        assertTrue(
            (
                engine.state.value.progress
                    ?.along ?: 0.0
            ) > 0.0,
        )
    }

    /** A host that reuses one list for every fix moves nothing the engine holds. */
    @Test
    fun aPositionIsCopiedAsItIsStated() {
        val engine = ScriptedNavigationEngine<String>()
        val fix = mutableListOf(0.005, 0.0, 1_200.0)

        engine.setPosition(fix)
        fix[0] = 0.015
        engine.start("summit")
        engine.sent.single().answer(eastward)

        assertEquals(
            length / 4,
            engine.state.value.progress
                ?.along ?: Double.NaN,
            1e-6,
        )
    }

    @Test
    fun aStartMadeInFlightIsSentOnceTheAnswerLandsAndOnlyTheNewestIsFollowed() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("a")
        engine.start("b")
        engine.start("c")
        assertEquals(listOf("a"), engine.sent.map { it.request })

        engine.sent[0].answer(eastward)
        assertEquals(NavigationStatus.Routing, engine.state.value.status)
        assertEquals(listOf("a", "c"), engine.sent.map { it.request })

        val newest = NavigationRoute(listOf(listOf(1.0, 1.0)))
        engine.sent[1].answer(newest)
        assertSame(
            newest.line,
            engine.state.value.route
                ?.line,
        )
    }

    @Test
    fun aStopAbandonsWhatIsInFlightAndDropsItsLateAnswer() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("a")
        engine.stop()
        assertTrue(engine.sent[0].isAbandoned)
        assertEquals(NavigationStatus.Idle, engine.state.value.status)

        engine.start("b")
        assertEquals(2, engine.sent.size, "the next start is sent at once")

        engine.sent[0].answer(eastward)
        assertEquals(NavigationStatus.Routing, engine.state.value.status)
        assertNull(engine.state.value.route)
    }

    @Test
    fun aFirstRouteThatFailsLeavesNothingToFollow() {
        val engine = ScriptedNavigationEngine<String>()
        val offline = IllegalStateException("offline")

        engine.start("summit")
        engine.sent.single().fail(offline)

        val state = engine.state.value
        assertEquals(NavigationStatus.Failed, state.status)
        assertNull(state.route)
        assertSame(offline, (state.error as? NavigationError.RouteFailed)?.cause)
    }

    @Test
    fun aRouteWhoseLineHoldsNoVertexFails() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("summit")
        engine.sent.single().answer(NavigationRoute(emptyList()))

        assertEquals(NavigationStatus.Failed, engine.state.value.status)
        assertTrue(engine.state.value.error is NavigationError.EmptyRoute)
    }

    /**
     * A failure is identified where the engine records it, so two failures recorded apart are two, whatever their
     * sources threw: a state holding one never equals a state holding another, and an observer is told of each.
     */
    @Test
    fun twoFailuresRecordedApartAreTwoWhateverTheirSourcesThrew() {
        val engine = ScriptedNavigationEngine<String>()
        val offline = IllegalStateException("offline")

        engine.start("summit")
        engine.sent.last().fail(offline)
        val first = engine.state.value
        engine.start("summit, again")
        engine.sent.last().fail(offline)
        val second = engine.state.value
        engine.start("hut")
        engine.sent.last().answer(NavigationRoute(emptyList()))
        val empty = engine.state.value
        engine.start("hut, again")
        engine.sent.last().answer(NavigationRoute(emptyList()))

        assertEquals(NavigationStatus.Failed, second.status)
        assertTrue(first != second, "two failures of one throwable recorded apart are two")
        assertTrue(empty != engine.state.value, "two empty routes recorded apart are two failures")
        assertEquals(first, first)
    }

    /**
     * The error is why the last request acted on failed: kept through positions and a reroute, and
     * cleared by a start and by a route that lands.
     */
    @Test
    fun theErrorStandsUntilAStartOrARouteThatLands() {
        val engine = navigating()
        engine.setPosition(north(0.01, 200.0))
        engine.reroute("from here")
        engine.sent.last().fail(IllegalStateException("no signal"))

        engine.setPosition(north(0.01, 0.0))
        assertEquals(NavigationStatus.Navigating, engine.state.value.status)
        assertTrue(engine.state.value.error is NavigationError.RouteFailed)

        engine.start("elsewhere")
        assertNull(engine.state.value.error)

        engine.sent.last().fail(IllegalStateException("offline"))
        engine.start("again")
        engine.sent.last().answer(eastward)
        assertNull(engine.state.value.error)
    }

    /** As `sdk-typescript`'s engine does, a stop forgets the route and keeps the last position stated. */
    @Test
    fun aStopKeepsTheLastPositionForTheNextRoute() {
        val engine = navigating()

        engine.setPosition(listOf(0.005, 0.0))
        engine.stop()
        engine.start("again")
        engine.sent.last().answer(eastward)

        assertEquals(
            length / 4,
            engine.state.value.progress
                ?.along ?: Double.NaN,
            1e-6,
        )
    }

    @Test
    fun aPositionThatNamesNoPlaceIsRefusedByItsValue() {
        val engine = navigating()

        val refusal = assertFailsWith<IllegalArgumentException> { engine.setPosition(listOf(Double.NaN, 45.0)) }
        assertTrue(refusal.message?.contains("[NaN, 45.0]") ?: false)
        assertFailsWith<IllegalArgumentException> { engine.setPosition(listOf(6.0)) }
        assertNull(engine.state.value.progress)
    }

    @Test
    fun afterACloseEveryCommandDoesNothingAndALateAnswerIsDropped() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("summit")
        engine.close()
        assertTrue(engine.sent.single().isAbandoned)

        engine.sent.single().answer(eastward)
        engine.start("again")
        engine.setPosition(listOf(Double.NaN, 0.0))
        engine.close()

        assertEquals(NavigationStatus.Routing, engine.state.value.status)
        assertEquals(1, engine.sent.size)
    }
}
