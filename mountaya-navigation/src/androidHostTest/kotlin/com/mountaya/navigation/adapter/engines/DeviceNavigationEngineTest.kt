package com.mountaya.navigation

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationEngine
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.NavigationRouteSource
import com.mountaya.geo.NavigationState
import com.mountaya.geo.NavigationStatus
import com.mountaya.geo.NavigationStep
import com.mountaya.geo.PROMPT_METERS
import com.mountaya.geo.PROMPT_SLACK_METERS
import com.mountaya.geo.ScriptedNavigationEngine
import com.mountaya.navigation.Journeys.north
import com.mountaya.navigation.Journeys.walked
import com.mountaya.navigation.Journeys.west
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

/**
 * The engine an Android app navigates with, on the host: Geo's machine with its guidance, held state for
 * state to the scripted engine's for the same inputs, with its prompts said where `duePrompt` finds them
 * due, its location source run from the first start to a stop, and its voice hushed at each new route, at
 * a stop and at a close. The route source answers at once, and its answer lands as the test's scope next
 * runs, as it lands on the main thread's next turn in an app.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceNavigationEngineTest {
    /** Two stretches of about 775 m east, at Chamonix's latitude. */
    private val eastward =
        NavigationRoute(listOf(listOf(6.86, 45.92), listOf(6.87, 45.92), listOf(6.88, 45.92)), duration = 1_200.0)

    /** 200 m north of the route's middle vertex. */
    private val off = north(listOf(6.87, 45.92), 200.0)

    /** Leaves from [off] and rejoins the route at its end. */
    private val detour = NavigationRoute(listOf(off, listOf(6.88, 45.92)), duration = 900.0)

    private val turn = listOf(6.87, 45.92)

    /** Departs east along a way, turns north onto another for 600 m, and arrives at its end. */
    private val turning =
        NavigationRoute(
            listOf(listOf(6.86, 45.92), turn, north(turn, 600.0)),
            steps =
                listOf(
                    NavigationStep(0, 1, RestSchema.ManeuverType.Depart, name = "Chemin"),
                    NavigationStep(1, 2, RestSchema.ManeuverType.TurnLeft, name = "Sentier"),
                    NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                ),
        )

    private class NoRoute : Exception("no route for the request")

    /** A route source answering [routes] by request, at once, and keeping every request it was asked. */
    private class Source(
        private val routes: Map<String, NavigationRoute>,
    ) : NavigationRouteSource<String> {
        val asked = mutableListOf<String>()

        override suspend fun route(request: String): NavigationRoute {
            asked += request
            return routes[request] ?: throw NoRoute()
        }
    }

    /** Every state [engine] reports from now on, its current one first, as a collector that misses none sees them. */
    private fun TestScope.recorded(engine: NavigationEngine<String>): List<NavigationState> {
        val seen = mutableListOf<NavigationState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.state.toList(seen) }
        return seen
    }

    @Test
    fun aJourneyIsTheScriptedEnginesStateForStateAlongTheRouteOffItARerouteTheArrivalAndAStop() =
        runTest {
            val device =
                DeviceNavigationEngine(
                    backgroundScope,
                    Source(
                        mapOf(
                            "summit" to eastward,
                            "from-here" to detour,
                        ),
                    ),
                )
            val scripted = ScriptedNavigationEngine<String>()
            val fromDevice = recorded(device)
            val fromScripted = recorded(scripted)

            device.start("summit")
            scripted.start("summit")
            assertEquals(scripted.state.value, device.state.value)
            runCurrent()
            scripted.sent[0].answer(eastward)
            assertEquals(scripted.state.value, device.state.value)

            for (position in walked(eastward.line.subList(0, 2))) {
                device.setPosition(position)
                scripted.setPosition(position)
                assertEquals(scripted.state.value, device.state.value)
            }
            assertEquals(NavigationStatus.Navigating, device.state.value.status)

            device.setPosition(off)
            scripted.setPosition(off)
            assertEquals(NavigationStatus.OffRoute, device.state.value.status)
            device.reroute("from-here")
            scripted.reroute("from-here")
            assertEquals(scripted.state.value, device.state.value)
            runCurrent()
            scripted.sent[1].answer(detour)
            assertEquals(scripted.state.value, device.state.value)

            for (position in walked(detour.line)) {
                device.setPosition(position)
                scripted.setPosition(position)
                assertEquals(scripted.state.value, device.state.value)
            }
            assertEquals(NavigationStatus.Arrived, device.state.value.status)

            device.stop()
            scripted.stop()
            device.close()
            scripted.close()
            assertEquals(fromScripted, fromDevice)
            assertEquals(NavigationState.idle, fromDevice.first())
            assertEquals(NavigationState.idle, fromDevice.last())
            assertTrue(fromDevice.any { it.status == NavigationStatus.Rerouting })
        }

    /**
     * A walk of the route a metre at a time: each prompt is said at the first fix where the distance Geo's
     * progress reads to its step's end, less the prompt's 50 m, is 5 m or less, so 55 m before its
     * maneuver, to the metre, and once.
     */
    @Test
    fun aRouteFollowedFixByFixSaysEachPromptOnceAtTheMetreDuePromptPicks() =
        runTest {
            val voice = RecordingVoice()
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to turning)), voice = voice)

            device.start("summit")
            runCurrent()
            assertEquals(
                listOf(NavigationInstructionKey.Depart),
                voice.said.map { it.key },
                "the departure is said as the route lands",
            )

            // Each prompt said on the walk, with the distance left to its step's end at the fix before and at it.
            val heard = mutableListOf<Triple<NavigationInstructionKey, Double, Double>>()
            for (position in walked(turning.line, meters = 1.0)) {
                val before =
                    device.state.value.progress
                        ?.toManeuver
                        ?.distance ?: Double.POSITIVE_INFINITY
                val said = voice.said.size
                device.setPosition(position)
                if (voice.said.size == said) continue
                val left =
                    assertNotNull(
                        device.state.value.progress,
                        "the navigation states its progress",
                    ).toManeuver.distance
                heard += Triple(voice.said.last().key, before, left)
            }

            assertEquals(
                listOf(NavigationInstructionKey.TurnLeft, NavigationInstructionKey.GoalPlain),
                heard.map { it.first },
            )
            for ((key, before, left) in heard) {
                assertTrue(left - PROMPT_METERS <= PROMPT_SLACK_METERS, "$key is said where it is due, $left m before")
                assertTrue(
                    before - PROMPT_METERS > PROMPT_SLACK_METERS,
                    "$key is said at the first fix it is due, not $before m before",
                )
                assertTrue(
                    left > PROMPT_METERS + PROMPT_SLACK_METERS - 1.0,
                    "$key is said 55 m before its maneuver, to the metre",
                )
            }
            assertEquals(NavigationStatus.Arrived, device.state.value.status)
            assertEquals(
                listOf(
                    NavigationInstructionKey.Depart,
                    NavigationInstructionKey.TurnLeft,
                    NavigationInstructionKey.GoalPlain,
                ),
                voice.said.map { it.key },
                "each prompt is said once",
            )
            assertEquals(listOf("Chemin", "Sentier", null), voice.said.map { it.name })
            device.close()
        }

    @Test
    fun nothingIsSaidOffTheRouteAndTheManeuverAheadIsSaidOnceBackOnIt() =
        runTest {
            val voice = RecordingVoice()
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to turning)), voice = voice)

            device.start("summit")
            runCurrent()
            // 40 m before the turn, 80 m south of the way: the turn is due, and the traveller is off.
            val beforeTurn = west(turn, 40.0)
            device.setPosition(north(beforeTurn, -80.0))
            assertEquals(NavigationStatus.OffRoute, device.state.value.status)
            assertEquals(listOf(NavigationInstructionKey.Depart), voice.said.map { it.key })

            device.setPosition(beforeTurn)
            assertEquals(NavigationStatus.Navigating, device.state.value.status)
            assertEquals(
                listOf(NavigationInstructionKey.Depart, NavigationInstructionKey.TurnLeft),
                voice.said.map { it.key },
            )
            device.setPosition(beforeTurn)
            assertEquals(2, voice.said.size)
            device.close()
        }

    /**
     * A new route is the host's to ask for: off the route the engine waits for `reroute`, asks nothing on
     * its own, and asks nothing for a reroute on the route or while one is awaited.
     */
    @Test
    fun offTheRouteANewRouteIsAskedForWhereTheHostAsksAndNowhereElse() =
        runTest {
            val source = Source(mapOf("summit" to eastward, "from-here" to detour))
            val device = DeviceNavigationEngine(backgroundScope, source)

            device.start("summit")
            runCurrent()
            device.setPosition(listOf(6.865, 45.92))
            device.reroute("from-here")
            runCurrent()
            assertEquals(listOf("summit"), source.asked, "on the route, a reroute asks for nothing")

            for (meters in listOf(0.0, 10.0, 20.0)) device.setPosition(north(off, meters))
            runCurrent()
            assertEquals(NavigationStatus.OffRoute, device.state.value.status)
            assertEquals(listOf("summit"), source.asked, "off the route, nothing is asked on its own")

            device.reroute("from-here")
            assertEquals(NavigationStatus.Rerouting, device.state.value.status)
            device.reroute("again")
            runCurrent()
            assertEquals(listOf("summit", "from-here"), source.asked, "one new route, while none is awaited")
            assertSame(
                detour.line,
                device.state.value.route
                    ?.line,
            )
            assertEquals(NavigationStatus.Navigating, device.state.value.status)
            device.close()
        }

    /**
     * The arrival is Geo's: on the route, with no distance left to its end, so a traveller who stops short
     * never arrives and one a little past the end has.
     */
    @Test
    fun theArrivalIsRaisedWhereGeoReadsTheTravellerOnTheRouteAtItsEnd() =
        runTest {
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to eastward)))
            val end = eastward.line.last()

            device.start("summit")
            runCurrent()
            device.setPosition(west(end, 10.0))
            assertEquals(NavigationStatus.Navigating, device.state.value.status)
            assertTrue(
                (
                    device.state.value.progress
                        ?.toArrival
                        ?.distance ?: 0.0
                ) > 9.0,
            )

            device.setPosition(west(end, -20.0))
            assertEquals(NavigationStatus.Arrived, device.state.value.status)
            assertEquals(
                0.0,
                device.state.value.progress
                    ?.toArrival
                    ?.distance,
            )
            device.close()
        }

    @Test
    fun theLocationSourceRunsFromTheFirstStartToAStopAndEachPositionItStatesIsRead() =
        runTest {
            val location = RecordingLocationSource()
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to eastward)), location)

            device.start("summit")
            device.start("summit")
            assertEquals(1, location.started)
            runCurrent()
            assertTrue(location.state(listOf(6.865, 45.92)))
            assertTrue(
                (
                    device.state.value.progress
                        ?.along ?: 0.0
                ) > 0.0,
            )
            assertFailsWith<IllegalArgumentException> { location.state(listOf(Double.NaN, 45.92)) }

            device.stop()
            assertEquals(1, location.stopped)
            assertFalse(location.state(listOf(6.866, 45.92)), "a stopped source states nothing")
            device.start("summit")
            assertEquals(2, location.started)
            device.close()
            assertEquals(2, location.stopped)
        }

    /**
     * The source is told of each route the engine follows once its guidance is laid out, and of none at a
     * stop, so a replay walks the route followed and nothing after.
     */
    @Test
    fun theLocationSourceIsToldOfEachRouteFollowedAndOfNoneAtAStop() =
        runTest {
            val location = RecordingLocationSource()
            val voice = RecordingVoice()
            val device =
                DeviceNavigationEngine(
                    backgroundScope,
                    Source(mapOf("summit" to eastward, "from-here" to detour)),
                    location,
                    voice,
                )

            device.start("summit")
            runCurrent()
            val hushed = voice.hushed
            device.setPosition(off)
            device.reroute("from-here")
            runCurrent()

            assertEquals(hushed + 1, voice.hushed, "a new route hushes the voice")
            assertEquals(listOf(eastward.line, detour.line), location.followed.map { it?.line })
            assertSame(
                detour.line,
                device.state.value.route
                    ?.line,
            )

            device.stop()
            assertEquals(hushed + 2, voice.hushed, "a stop hushes it once")
            assertNull(location.followed.last())
            device.close()
        }

    /**
     * A route that lands while a position is held is read there next, and guided there: 40 m before the turn
     * the turn is due, and the departure the unread route would say is never said.
     */
    @Test
    fun aRouteThatLandsWhileAPositionIsHeldSaysNothingUntilThatPositionIsReadOnIt() =
        runTest {
            val voice = RecordingVoice()
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to turning)), voice = voice)

            device.start("summit")
            device.setPosition(west(turn, 40.0))
            runCurrent()

            assertEquals(listOf(NavigationInstructionKey.TurnLeft), voice.said.map { it.key })
            device.close()
        }

    @Test
    fun aPositionASourceStatesAsItIsToldOfARouteIsReadOnThatRouteAndGuidedThere() =
        runTest {
            val voice = RecordingVoice()
            val device =
                DeviceNavigationEngine(
                    backgroundScope,
                    Source(mapOf("summit" to turning)),
                    EagerLocationSource(west(turn, 40.0)),
                    voice,
                )

            device.start("summit")
            runCurrent()

            assertTrue(
                (
                    device.state.value.progress
                        ?.along ?: 0.0
                ) > 0.0,
                "what the source stated as it was told is read",
            )
            assertEquals(listOf(NavigationInstructionKey.TurnLeft), voice.said.map { it.key })
            device.close()
        }

    /**
     * The machine tells the engine each state inline, before its state flow holds it, so a collector that
     * runs where a state is set, as one on the main thread's immediate dispatcher does, never reads a route
     * the source has not been told of or whose departure was not said.
     */
    @Test
    fun theSourceAndTheVoiceKeepToEachRouteBeforeAnyCollectorIsHandedIt() =
        runTest {
            val location = RecordingLocationSource()
            val voice = RecordingVoice()
            val device =
                DeviceNavigationEngine(
                    backgroundScope,
                    Source(mapOf("summit" to eastward, "from-here" to detour)),
                    location,
                    voice,
                )
            val readers =
                (0 until 4).map {
                    // Whether the source followed the route as it landed, and how much the voice had said.
                    val kept = mutableListOf<Pair<Boolean, Int>>()
                    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                        device.state.collect { state ->
                            // A route that lands unread, the state a collector sees first of each.
                            if (state.status == NavigationStatus.Navigating && state.progress == null) {
                                kept += (location.followed.lastOrNull() == state.route) to voice.said.size
                            }
                        }
                    }
                    kept
                }

            device.start("summit")
            runCurrent()
            device.setPosition(off)
            device.reroute("from-here")
            runCurrent()

            // The first route's departure is said as it lands; the second lands while a position is held.
            for (kept in readers) assertEquals(listOf(true to 1, true to 1), kept)
            device.close()
        }

    @Test
    fun aStopEndsTheNavigationAndWhatIsSaidAndACloseEndsEveryCommand() =
        runTest {
            val location = RecordingLocationSource()
            val voice = RecordingVoice()
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to turning)), location, voice)
            val states = recorded(device)

            device.start("summit")
            runCurrent()
            var hushed = voice.hushed
            device.stop()
            assertEquals(NavigationState.idle, device.state.value)
            assertEquals(hushed + 1, voice.hushed, "a stop hushes the voice")

            device.setPosition(listOf(6.865, 45.92))
            assertEquals(
                listOf(NavigationInstructionKey.Depart),
                voice.said.map { it.key },
                "a stopped navigation says nothing",
            )

            device.start("summit")
            runCurrent()
            hushed = voice.hushed
            device.close()
            assertEquals(hushed + 1, voice.hushed, "a close hushes the voice")
            assertEquals(2, location.stopped)
            val closed = device.state.value
            device.start("summit")
            device.setPosition(listOf(6.866, 45.92))
            device.reroute("summit")
            device.stop()
            device.close()
            runCurrent()
            assertEquals(closed, device.state.value, "after a close every command does nothing")
            assertEquals(closed, states.last())
            assertEquals(hushed + 1, voice.hushed)
            assertEquals(2, location.started)
            assertFalse(location.state(listOf(6.866, 45.92)), "the source states to nobody")
        }

    /**
     * A collector that closes the engine as it is handed the stop's state halts it there, and the stop halts
     * nothing more: the voice is hushed once and the source stopped once.
     */
    @Test
    fun aCloseACollectorMakesAsAStopIsHandedOnHaltsTheEngineOnce() =
        runTest {
            val location = RecordingLocationSource()
            val voice = RecordingVoice()
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to eastward)), location, voice)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                device.state.collect { state ->
                    if (state.status == NavigationStatus.Idle && location.started > 0) device.close()
                }
            }

            device.start("summit")
            runCurrent()
            val hushed = voice.hushed
            device.stop()

            assertEquals(NavigationState.idle, device.state.value)
            assertEquals(hushed + 1, voice.hushed)
            assertEquals(1, location.stopped)
            assertNull(location.followed.last())
        }

    @Test
    fun aReplayWalksEachRouteFollowedToItsArrivalWithNoDeviceLocation() =
        runTest {
            val device =
                DeviceNavigationEngine(
                    backgroundScope,
                    Source(mapOf("summit" to eastward)),
                    location =
                        ReplayLocationSource(
                            backgroundScope,
                            metersPerSecond = 400.0,
                            timeSource = testScheduler.timeSource,
                        ),
                )

            device.start("summit")
            advanceTimeBy(1.hours)

            assertEquals(NavigationStatus.Arrived, device.state.value.status)
            assertEquals(
                0.0,
                device.state.value.progress
                    ?.toArrival
                    ?.distance,
            )
            device.close()
        }

    /** The route [turning] follows, its maneuvers at the turn laid out by [steps]. */
    private fun turningBy(vararg steps: NavigationStep): NavigationRoute =
        NavigationRoute(turning.line, steps = steps.toList())

    /**
     * Walks [route] fix by fix, a metre apart, from its start, the first fix held before the route lands where
     * [heldAtStart] says, and answers each prompt said with the distance left to its step's end as it was said.
     */
    private fun TestScope.heardAlong(
        route: NavigationRoute,
        heldAtStart: Boolean = false,
    ): List<Pair<NavigationInstructionKey, Double>> {
        val voice = RecordingVoice()
        val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("route" to route)), voice = voice)
        val heard = mutableListOf<Pair<NavigationInstructionKey, Double>>()
        device.start("route")
        if (heldAtStart) device.setPosition(route.line.first())
        runCurrent()
        heard += voice.said.map { it.key to Double.NaN }
        for (position in walked(route.line, meters = 1.0)) {
            val said = voice.said.size
            device.setPosition(position)
            for (instruction in voice.said.drop(said)) {
                heard +=
                    instruction.key to
                    assertNotNull(
                        device.state.value.progress,
                        "the navigation states its progress",
                    ).toManeuver.distance
            }
        }
        assertEquals(NavigationStatus.Arrived, device.state.value.status)
        device.close()
        return heard
    }

    /**
     * Two maneuvers on one vertex, the second a step of no length: both are said, in order, the first 50 m
     * before the vertex and the second at it, where before the second was never said.
     */
    @Test
    fun twoManeuversOnOneMidRouteVertexAreSaidInOrderTheFirstFiftyMetersOutTheSecondAtIt() =
        runTest {
            val heard =
                heardAlong(
                    turningBy(
                        NavigationStep(0, 1, RestSchema.ManeuverType.Depart, name = "Chemin"),
                        NavigationStep(1, 1, RestSchema.ManeuverType.TurnLeft, name = "Pont"),
                        NavigationStep(1, 2, RestSchema.ManeuverType.TurnRight, name = "Sentier"),
                        NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                    ),
                )

            assertEquals(
                listOf(
                    NavigationInstructionKey.Depart,
                    NavigationInstructionKey.TurnLeft,
                    NavigationInstructionKey.TurnRight,
                    NavigationInstructionKey.GoalPlain,
                ),
                heard.map { it.first },
            )
            val (_, firstOut) = heard[1]
            val (_, secondOut) = heard[2]
            assertTrue(
                firstOut in (PROMPT_METERS + PROMPT_SLACK_METERS - 1.0)..(PROMPT_METERS + PROMPT_SLACK_METERS),
                "$firstOut",
            )
            assertTrue(secondOut <= PROMPT_SLACK_METERS, "the second is said at the vertex, $secondOut m before it")
        }

    /**
     * Three maneuvers or more on one vertex: the first is said 50 m out and the last at the vertex; those
     * between are never the last prompt due, and are not said.
     */
    @Test
    fun consecutiveStepsOfNoLengthMidRouteSayTheFirstFiftyMetersOutTheLastAtTheVertexAndNoneBetween() =
        runTest {
            val heard =
                heardAlong(
                    turningBy(
                        NavigationStep(0, 1, RestSchema.ManeuverType.Depart),
                        NavigationStep(1, 1, RestSchema.ManeuverType.TurnLeft),
                        NavigationStep(1, 1, RestSchema.ManeuverType.Continue),
                        NavigationStep(1, 2, RestSchema.ManeuverType.TurnRight),
                        NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                    ),
                )

            assertEquals(
                listOf(
                    NavigationInstructionKey.DepartPlain,
                    NavigationInstructionKey.TurnLeftPlain,
                    NavigationInstructionKey.TurnRightPlain,
                    NavigationInstructionKey.GoalPlain,
                ),
                heard.map { it.first },
            )
            assertTrue(heard[1].second > PROMPT_METERS, "the first, ${heard[1].second} m out")
            assertTrue(heard[2].second <= PROMPT_SLACK_METERS, "the last, ${heard[2].second} m out")
        }

    /**
     * A first step of no length covers no ground: its departure is dropped, and the maneuver after it, the
     * first the traveller makes, is said as the walk begins, whether a position is held as the route lands
     * or the first fix follows it. Before, a held position was read past it, and the turn was never said.
     */
    @Test
    fun aFirstStepOfNoLengthSaysTheManeuverAfterItAsTheWalkBeginsAndNotTheDeparture() =
        runTest {
            val route =
                turningBy(
                    NavigationStep(0, 0, RestSchema.ManeuverType.Depart, name = "Parking"),
                    NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft, name = "Chemin"),
                    NavigationStep(1, 2, RestSchema.ManeuverType.TurnRight, name = "Sentier"),
                    NavigationStep(2, 2, RestSchema.ManeuverType.Goal),
                )
            for (heldAtStart in listOf(true, false)) {
                val heard = heardAlong(route, heldAtStart)

                assertEquals(
                    listOf(
                        NavigationInstructionKey.TurnLeft,
                        NavigationInstructionKey.TurnRight,
                        NavigationInstructionKey.GoalPlain,
                    ),
                    heard.map { it.first },
                    "held at the start: $heldAtStart",
                )
            }
        }

    /**
     * A replay walked to its arrival, stopped and started again on the same route, from a source that answers
     * without suspending: the machine keeps the last position across a stop, so the route that lands must be
     * read where the new walk begins, never at the old walk's arrival, which would hold the navigation in
     * `arrived` with nothing said. On a dispatcher that runs a coroutine where it is launched the route lands
     * within `start` itself, and on one that queues it, as the scope next runs.
     */
    @Test
    fun aReplayStartedAgainOnTheSameRouteWalksFromItsStartAndSaysItsDepartureAgain() =
        runTest {
            for (immediate in listOf(true, false)) {
                val scope =
                    if (immediate) {
                        CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
                    } else {
                        backgroundScope
                    }
                val voice = RecordingVoice()
                val device =
                    DeviceNavigationEngine(
                        scope,
                        Source(mapOf("summit" to turning)),
                        ReplayLocationSource(scope, metersPerSecond = 10.0, timeSource = testScheduler.timeSource),
                        voice,
                    )
                val walk =
                    listOf(
                        NavigationInstructionKey.Depart,
                        NavigationInstructionKey.TurnLeft,
                        NavigationInstructionKey.GoalPlain,
                    )

                device.start("summit")
                advanceTimeBy(1.hours)
                assertEquals(NavigationStatus.Arrived, device.state.value.status, "immediate: $immediate")
                assertEquals(walk, voice.said.map { it.key }, "immediate: $immediate")
                device.stop()
                voice.said.clear()

                device.start("summit")
                runCurrent()
                assertEquals(NavigationStatus.Navigating, device.state.value.status, "immediate: $immediate")
                assertEquals(
                    0.0,
                    device.state.value.progress
                        ?.along,
                    "immediate: $immediate",
                )
                assertEquals(
                    listOf(NavigationInstructionKey.Depart),
                    voice.said.map { it.key },
                    "immediate: $immediate",
                )

                advanceTimeBy(1.hours)
                assertEquals(NavigationStatus.Arrived, device.state.value.status, "immediate: $immediate")
                assertEquals(
                    walk,
                    voice.said.map { it.key },
                    "the second walk says each prompt once, immediate: $immediate",
                )
                device.close()
            }
        }

    @Test
    fun aPositionThatNamesNoPlaceIsRefusedByItsValueAndMovesNothing() =
        runTest {
            val device = DeviceNavigationEngine(backgroundScope, Source(mapOf("summit" to eastward)))
            device.start("summit")
            runCurrent()
            val before = device.state.value

            val refused = assertFailsWith<IllegalArgumentException> { device.setPosition(listOf(Double.NaN, 45.92)) }
            assertTrue("NaN" in refused.message.orEmpty())
            assertEquals(before, device.state.value)
            device.close()
        }
}
