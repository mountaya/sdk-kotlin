package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * The measurements of a line followed that every fix read along it may cost: one, as the route is followed. A
 * measurement is a haversine per vertex of the whole line, so one per fix would pay for the route's length again on
 * the path every fix takes (`route-measure` in the contract's `sdk/hot-paths.yaml`).
 */
private const val ROUTE_MEASURE_BUDGET = 1

@OptIn(ExperimentalCoroutinesApi::class, InternalMountayaApi::class)
class RouteFollowingEngineTest {
    private val route = NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0)), duration = 600.0)

    private val sent = mutableListOf<Pair<String, Int>>()
    private val abandoned = mutableListOf<Int>()
    private val engine =
        RouteFollowingEngine<String>(send = { request, ticket -> sent += request to ticket }, abandon = {
            abandoned +=
                it
        })

    /** A ticket is the count of requests sent with it, so an answer names the request it answers. */
    @Test
    fun eachRequestLeavesUnderTheNextTicket() {
        engine.start("a")
        engine.answered(1, RouteFollowingEngine.Answer.Route(route))
        engine.setPosition(listOf(0.005, 0.01))
        engine.reroute("b")

        assertEquals(listOf("a" to 1, "b" to 2), sent)
    }

    @Test
    fun answeringTheRequestInFlightSendsTheOneWaitingInsteadOfActingOnIt() {
        engine.start("a")
        engine.start("b")
        engine.answered(1, RouteFollowingEngine.Answer.Route(route))

        assertEquals(listOf("a" to 1, "b" to 2), sent)
        assertEquals(NavigationStatus.Routing, engine.state.value.status)

        engine.answered(2, RouteFollowingEngine.Answer.Route(route))
        assertEquals(NavigationStatus.Navigating, engine.state.value.status)
    }

    @Test
    fun aStopAbandonsTheTicketInFlightAndNothingWhenNoneIs() {
        engine.stop()
        assertTrue(abandoned.isEmpty())

        engine.start("a")
        engine.start("b")
        engine.stop()
        assertEquals(listOf(1), abandoned)

        engine.answered(1, RouteFollowingEngine.Answer.Route(route))
        assertEquals(NavigationStatus.Idle, engine.state.value.status)
        assertEquals(listOf("a" to 1), sent, "the request that waited was dropped")
    }

    @Test
    fun anAnswerToATicketNobodyAwaitsIsDropped() {
        engine.start("a")
        engine.answered(7, RouteFollowingEngine.Answer.Failure(IllegalStateException("late")))
        engine.answered(0, RouteFollowingEngine.Answer.Route(route))

        assertEquals(NavigationStatus.Routing, engine.state.value.status)
    }

    /** The position [meters] north of the equator at [longitude]. */
    private fun north(
        longitude: Double,
        meters: Double,
    ): List<Double> = listOf(longitude, meters / METERS_PER_DEGREE_MEAN_SPHERE)

    @Test
    fun hotPathBudgetRouteMeasureThirtyFixesARerouteAndThirtyMoreMeasureEachLineFollowedOnce() {
        // Two legs east along the equator, with no steps of their own, so the route is followed by a departure and an
        // arrival, its length read off the measure it holds.
        val eastward =
            NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.02, 0.0)), duration = 1_200.0)
        val leftAt = north(0.012, 200.0)
        val back = NavigationRoute(listOf(leftAt, north(0.02, 200.0)), duration = 600.0)
        val alongs = mutableListOf<Double>()
        val statuses = mutableListOf<NavigationStatus>()

        val measured =
            MeasureProbe.measuring {
                engine.start("out")
                engine.answered(1, RouteFollowingEngine.Answer.Route(eastward))
                // Thirty fixes along the way out, 5 m north of it, the traveller's progress moving on at each.
                for (fix in 1..30) {
                    engine.setPosition(north(0.01 * fix / 30, 5.0))
                    alongs += engine.state.value.progress
                        ?.along ?: Double.NaN
                }
                statuses += engine.state.value.status
                // Off the route, a new one asked for and followed, then thirty fixes along it to its end.
                engine.setPosition(leftAt)
                statuses += engine.state.value.status
                engine.reroute("from here")
                engine.answered(2, RouteFollowingEngine.Answer.Route(back))
                for (fix in 1..30) engine.setPosition(north(0.012 + 0.008 * fix / 30, 200.0))
                statuses += engine.state.value.status
            }

        // The work happened: each fix was read along the route followed, and the second route was followed to its end.
        assertTrue(alongs.zipWithNext().all { (before, after) -> before < after }, "$alongs")
        assertEquals(listOf(NavigationStatus.Navigating, NavigationStatus.OffRoute, NavigationStatus.Arrived), statuses)
        assertSame(
            back.line,
            engine.state.value.route
                ?.line,
        )
        // Each line followed was measured once at least and its budget at most, the line left behind never again.
        val out = measured.count { it === eastward.line }
        val home = measured.count { it === back.line }
        assertTrue(
            out in 1..ROUTE_MEASURE_BUDGET && home in 1..ROUTE_MEASURE_BUDGET,
            "the way out measured $out times and the way back $home",
        )
        // And nothing else was: a copy of a line, measured on the path a fix takes, counts against the budget too.
        assertEquals(out + home, measured.size, "every measurement: $measured")
    }

    @Test
    fun aCloseAbandonsTheRequestInFlightOnceAndIgnoresItsAnswer() {
        engine.start("a")
        engine.close()
        engine.close()
        engine.answered(1, RouteFollowingEngine.Answer.Route(route))

        assertEquals(listOf(1), abandoned)
        assertEquals(NavigationStatus.Routing, engine.state.value.status)
    }

    /**
     * Drives [machine] through a route, a position on it, one off it, a stop and a close, hands [after]
     * the state each command leaves, and answers every state it committed, in order, the first one held
     * included.
     */
    private fun journey(
        machine: RouteFollowingEngine<String>,
        after: (NavigationState) -> Unit = {},
    ): List<NavigationState> {
        val committed = mutableListOf(machine.state.value)

        fun note() {
            after(machine.state.value)
            if (committed.last() != machine.state.value) committed += machine.state.value
        }
        machine.start("a")
        note()
        machine.answered(1, RouteFollowingEngine.Answer.Route(route))
        note()
        machine.setPosition(listOf(0.005, 0.0))
        note()
        machine.setPosition(listOf(0.005, 0.01))
        note()
        machine.stop()
        note()
        machine.close()
        return committed
    }

    /** Every state [machine] reports from now on, its current one first, as a collector that runs where a state is set sees them. */
    private fun TestScope.collected(machine: RouteFollowingEngine<String>): List<NavigationState> {
        val seen = mutableListOf<NavigationState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { machine.state.toList(seen) }
        return seen
    }

    /**
     * A state flow conflates: a collector that runs late reads the newest state alone. One that runs
     * where each state is set, as the main thread's immediate dispatcher runs a collector there, is
     * handed every state, in order.
     */
    @Test
    fun eachCollectorThatRunsWhereAStateIsSetIsHandedEveryStateInOrder() =
        runTest {
            val collectors = (0 until 8).map { collected(engine) }

            val committed = journey(engine)

            assertEquals(
                listOf(
                    NavigationStatus.Idle,
                    NavigationStatus.Routing,
                    NavigationStatus.Navigating,
                    NavigationStatus.Navigating,
                    NavigationStatus.OffRoute,
                    NavigationStatus.Idle,
                ),
                committed.map { it.status },
            )
            for (seen in collectors.reversed()) assertEquals(committed, seen)
        }

    @Test
    fun onChangeIsToldEachStateInlineInOrderBeforeTheCommandThatMadeItReturns() =
        runTest {
            val told = mutableListOf<NavigationState>()
            lateinit var seen: List<NavigationState>
            val machine =
                RouteFollowingEngine<String>(send = { _, _ -> }, abandon = {}, onChange = { state ->
                    // The collector holds the first state and each one told before this one, and not this one.
                    assertEquals(told.size + 1, seen.size, "no collector was handed $state before onChange")
                    told += state
                })
            seen = collected(machine)

            val committed =
                journey(machine) { state ->
                    assertEquals(told.last(), state, "the state a command left was told before it returned")
                }

            assertEquals(committed.drop(1), told)
            assertEquals(committed, seen)
        }

    /**
     * A close makes the flow report nothing more, so a close made in `onChange` leaves the state it was
     * told in no collector: it reaches `onChange` before it reaches the flow.
     */
    @Test
    fun onChangeIsToldEachStateBeforeAnyCollectorIsHandedIt() =
        runTest {
            lateinit var owner: RouteFollowingEngine<String>
            val machine =
                RouteFollowingEngine<String>(send = { _, _ -> }, abandon = {}, onChange = { state ->
                    if (state.status == NavigationStatus.Routing) owner.close()
                })
            owner = machine
            val collectors = (0 until 4).map { collected(machine) }

            machine.start("a")

            assertEquals(NavigationState.idle, machine.state.value, "the state onChange closed on reached no collector")
            for (seen in collectors) assertEquals(listOf(NavigationStatus.Idle), seen.map { it.status })
        }

    /**
     * A collector started while a state is handed on starts at the state the flow holds, the last one
     * handed on, and is handed each one after it in order.
     */
    @Test
    fun aCommandOnChangeMakesIsHandedOnAfterTheStateItWasToldToItAndEveryCollector() =
        runTest {
            lateinit var owner: RouteFollowingEngine<String>
            val told = mutableListOf<NavigationStatus>()
            var late: List<NavigationState>? = null
            val machine =
                RouteFollowingEngine<String>(send = { _, _ -> }, abandon = {}, onChange = { state ->
                    told += state.status
                    if (state.status == NavigationStatus.Routing && late == null) {
                        owner.stop()
                        late = collected(owner)
                    }
                })
            owner = machine
            val early = collected(machine)

            machine.start("a")
            machine.close()

            assertEquals(listOf(NavigationStatus.Routing, NavigationStatus.Idle), told)
            val handedOn = listOf(NavigationStatus.Idle, NavigationStatus.Routing, NavigationStatus.Idle)
            assertEquals(handedOn, early.map { it.status })
            assertEquals(handedOn, late?.map { it.status })
        }

    /** What `onChange` throws is the command's, and the flow still reaches the machine's state. */
    @Test
    fun aThrowFromOnChangePassesThroughTheCommandAndTheFlowStillHoldsTheMachinesState() =
        runTest {
            lateinit var owner: RouteFollowingEngine<String>
            var thrown = false
            val machine =
                RouteFollowingEngine<String>(send = { _, _ -> }, abandon = {}, onChange = { state ->
                    if (state.status == NavigationStatus.Routing && !thrown) {
                        thrown = true
                        owner.stop()
                        throw IllegalStateException("refused")
                    }
                })
            owner = machine
            val seen = collected(machine)

            assertFailsWith<IllegalStateException> { machine.start("a") }

            assertEquals(NavigationState.idle, machine.state.value)
            assertEquals(
                listOf(NavigationStatus.Idle, NavigationStatus.Routing, NavigationStatus.Idle),
                seen.map { it.status },
            )
            machine.start("b")
            assertEquals(NavigationStatus.Routing, machine.state.value.status, "the machine goes on")
        }

    @Test
    fun builtOverARouteSourceItAsksTheSourceAndFollowsTheRouteItAnswers() =
        runTest {
            val asked = mutableListOf<String>()
            val following =
                RouteFollowingEngine<String>(backgroundScope) { request ->
                    asked += request
                    route
                }

            following.start("summit")
            following.setPosition(listOf(0.005, 0.0))
            assertEquals(
                NavigationStatus.Routing,
                following.state.value.status,
                "a command has its effect, and the answer lands later",
            )

            runCurrent()
            assertEquals(NavigationStatus.Navigating, following.state.value.status)
            assertSame(
                route.line,
                following.state.value.route
                    ?.line,
            )
            assertTrue(
                (
                    following.state.value.progress
                        ?.along ?: 0.0
                ) > 0.0,
                "the position held is read on the route",
            )
            assertEquals(listOf("summit"), asked)
            following.close()
        }

    @Test
    fun builtOverARouteSourceAFailureItThrowsIsTheNavigationsError() =
        runTest {
            val unreachable = IllegalStateException("unreachable")
            val following = RouteFollowingEngine<String>(backgroundScope) { throw unreachable }

            following.start("summit")
            runCurrent()

            val failed = following.state.value
            assertEquals(NavigationStatus.Failed, failed.status)
            assertSame(unreachable, assertIs<NavigationError.RouteFailed>(failed.error).cause)
            following.close()
        }

    /**
     * An `Error` is a failure too: it ends neither the host's scope nor the request's turn, so the
     * next start asks the source at once rather than waiting behind a request nobody answers.
     */
    @Test
    fun builtOverARouteSourceAnErrorItThrowsIsTheNavigationsErrorAndTheNextStartAsksAgain() =
        runTest {
            val asked = mutableListOf<String>()
            val following =
                RouteFollowingEngine<String>(backgroundScope) { request ->
                    asked += request
                    if (request == "summit") TODO("a route source not written yet") else route
                }

            following.start("summit")
            runCurrent()

            val failed = following.state.value
            assertEquals(NavigationStatus.Failed, failed.status)
            assertIs<NotImplementedError>(assertIs<NavigationError.RouteFailed>(failed.error).cause)

            following.start("refuge")
            runCurrent()

            assertEquals(listOf("summit", "refuge"), asked)
            assertEquals(NavigationStatus.Navigating, following.state.value.status)
            following.close()
        }

    @Test
    fun builtOverARouteSourceAStopCancelsTheCoroutineAskingAndDropsItsAnswer() =
        runTest {
            var cancelled: Boolean? = null
            val following =
                RouteFollowingEngine<String>(backgroundScope) {
                    try {
                        delay(1.hours)
                        cancelled = false
                    } catch (cancellation: CancellationException) {
                        cancelled = true
                    }
                    // A source that answers anyway: the answer is dropped.
                    route
                }

            following.start("summit")
            runCurrent()
            following.stop()
            runCurrent()

            assertEquals(true, cancelled)
            assertEquals(NavigationState.idle, following.state.value)
        }

    @Test
    fun builtOverARouteSourceACloseCancelsTheRequestInFlight() =
        runTest {
            var cancelled: Boolean? = null
            val following =
                RouteFollowingEngine<String>(backgroundScope) {
                    try {
                        delay(1.hours)
                        cancelled = false
                    } catch (cancellation: CancellationException) {
                        cancelled = true
                        throw cancellation
                    }
                    throw IllegalStateException("unreachable")
                }

            following.start("summit")
            runCurrent()
            following.close()
            runCurrent()

            assertEquals(true, cancelled)
            assertEquals(
                NavigationStatus.Routing,
                following.state.value.status,
                "after a close the state stays as it was",
            )
        }

    /**
     * A cancellation the navigation did not ask for, a timeout the source sets itself, is the source's
     * failure: the request is no longer awaited, and a navigation left routing would wait for good.
     */
    @Test
    fun builtOverARouteSourceACancellationItThrowsWhileStillAskedIsItsFailure() =
        runTest {
            val following =
                RouteFollowingEngine<String>(backgroundScope) {
                    withTimeout(1.seconds) {
                        delay(1.hours)
                        route
                    }
                }

            following.start("summit")
            advanceTimeBy(2.seconds)
            runCurrent()

            val failed = following.state.value
            assertEquals(NavigationStatus.Failed, failed.status)
            assertIs<TimeoutCancellationException>(assertIs<NavigationError.RouteFailed>(failed.error).cause)
            following.close()
        }

    /**
     * A dispatcher that runs a coroutine where it is launched, as the main thread's immediate one
     * does, lands an answer that needs no wait before the command that asked for it returns.
     */
    @Test
    fun builtOverARouteSourceThatAnswersAtOnceTheRouteIsFollowedBeforeTheStartReturns() =
        runTest {
            val scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
            val following = RouteFollowingEngine<String>(scope) { route }

            following.setPosition(listOf(0.005, 0.0))
            following.start("summit")
            assertEquals(NavigationStatus.Navigating, following.state.value.status)
            assertTrue(
                (
                    following.state.value.progress
                        ?.along ?: 0.0
                ) > 0.0,
            )

            following.start("summit")
            following.stop()
            assertEquals(NavigationState.idle, following.state.value)
            following.close()
        }

    /**
     * A request is asked for before the state that asks for it is committed, and leaves once that state
     * was handed on: a stop `onChange` makes on `routing` abandons it before it leaves, so it never does,
     * and nothing that never left is abandoned.
     */
    @Test
    fun aStopOnChangeMakesOnRoutingAbandonsTheRequestBeforeItLeaves() {
        lateinit var owner: RouteFollowingEngine<String>
        val sent = mutableListOf<Pair<String, Int>>()
        val abandoned = mutableListOf<Int>()
        var stops = 0
        val send: (String, Int) -> Unit = { request, ticket -> sent += request to ticket }
        val stopOnRouting: (NavigationState) -> Unit = { state ->
            if (state.status == NavigationStatus.Routing && stops == 0) {
                stops += 1
                owner.stop()
            }
        }
        val machine = RouteFollowingEngine(send = send, abandon = { abandoned += it }, onChange = stopOnRouting)
        owner = machine

        machine.start("a")
        assertEquals(emptyList(), sent, "the request left after the stop")
        assertEquals(emptyList(), abandoned, "a request that never left is not abandoned")
        assertEquals(NavigationState.idle, machine.state.value)

        machine.start("b")
        assertEquals(listOf("b" to 2), sent)
        machine.stop()
        assertEquals(listOf(2), abandoned)
    }

    @Test
    fun aStopOnChangeMakesOnReroutingAbandonsTheNewRequestBeforeItLeaves() {
        lateinit var owner: RouteFollowingEngine<String>
        val sent = mutableListOf<Pair<String, Int>>()
        val send: (String, Int) -> Unit = { request, ticket -> sent += request to ticket }
        val stopOnRerouting: (NavigationState) -> Unit = { state ->
            if (state.status == NavigationStatus.Rerouting) owner.stop()
        }
        val machine = RouteFollowingEngine(send = send, abandon = {}, onChange = stopOnRerouting)
        owner = machine

        machine.start("a")
        machine.answered(1, RouteFollowingEngine.Answer.Route(route))
        machine.setPosition(listOf(0.005, 0.01))
        assertEquals(NavigationStatus.OffRoute, machine.state.value.status)
        machine.reroute("b")

        assertEquals(listOf("a" to 1), sent)
        assertEquals(NavigationState.idle, machine.state.value)
    }

    /**
     * A close made in `onChange` ends the command it was told from: a route that lands while a position
     * is held is told, and the reading of that position, which the command would commit next, is not.
     */
    @Test
    fun onChangeIsToldNothingAfterACloseItMakes() {
        lateinit var owner: RouteFollowingEngine<String>
        val told = mutableListOf<NavigationStatus>()
        val machine =
            RouteFollowingEngine<String>(send = { _, _ -> }, abandon = {}, onChange = { state ->
                told += state.status
                if (state.route != null && state.progress == null) owner.close()
            })
        owner = machine

        machine.setPosition(listOf(0.005, 0.0))
        machine.start("a")
        machine.answered(1, RouteFollowingEngine.Answer.Route(route))

        assertEquals(listOf(NavigationStatus.Routing, NavigationStatus.Navigating), told)
        assertEquals(
            NavigationStatus.Routing,
            machine.state.value.status,
            "the state onChange closed on reached no collector",
        )
    }

    @Test
    fun aCloseOnChangeMakesOnRoutingSendsNothing() {
        lateinit var owner: RouteFollowingEngine<String>
        val sent = mutableListOf<String>()
        val abandoned = mutableListOf<Int>()
        val send: (String, Int) -> Unit = { request, _ -> sent += request }
        val machine = RouteFollowingEngine(send = send, abandon = { abandoned += it }, onChange = { owner.close() })
        owner = machine

        machine.start("a")
        machine.start("b")

        assertEquals(emptyList(), sent)
        assertEquals(emptyList(), abandoned)
    }

    /**
     * What `onChange` throws passes through the command, and the request the command asked for still
     * leaves, so the navigation it left routing is not left waiting on a request nobody sent.
     */
    @Test
    fun aThrowFromOnChangeOnRoutingStillSendsTheRequest() {
        val sent = mutableListOf<Pair<String, Int>>()
        val send: (String, Int) -> Unit = { request, ticket -> sent += request to ticket }
        val refuseRouting: (NavigationState) -> Unit = { state ->
            if (state.status == NavigationStatus.Routing) throw IllegalStateException("refused")
        }
        val machine = RouteFollowingEngine(send = send, abandon = {}, onChange = refuseRouting)

        assertFailsWith<IllegalStateException> { machine.start("a") }

        assertEquals(listOf("a" to 1), sent)
        assertEquals(NavigationStatus.Routing, machine.state.value.status)
        machine.answered(1, RouteFollowingEngine.Answer.Route(route))
        assertEquals(NavigationStatus.Navigating, machine.state.value.status)
    }

    /**
     * Built over a route source on a dispatcher that runs a coroutine where it is launched, a stop
     * `onChange` makes on `routing` leaves the source unasked.
     */
    @Test
    fun builtOverARouteSourceAStopOnChangeMakesOnRoutingLeavesTheSourceUnasked() =
        runTest {
            val scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
            val asked = mutableListOf<String>()
            lateinit var owner: RouteFollowingEngine<String>
            var stops = 0
            val following =
                RouteFollowingEngine<String>(scope, { request ->
                    asked += request
                    route
                }) { state ->
                    if (state.status == NavigationStatus.Routing && stops == 0) {
                        stops += 1
                        owner.stop()
                    }
                }
            owner = following

            following.start("summit")
            assertEquals(emptyList(), asked)
            assertEquals(NavigationState.idle, following.state.value)

            following.start("refuge")
            assertEquals(listOf("refuge"), asked)
            assertEquals(NavigationStatus.Navigating, following.state.value.status)
            following.close()
        }
}
