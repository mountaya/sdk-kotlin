package com.mountaya.geo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScriptedNavigationEngineTest {
    private val route = NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0)), duration = 600.0)

    /** An app's code takes the interface; its test hands it the scripted engine and answers for it. */
    private fun startTrip(
        navigation: NavigationEngine<String>,
        destination: String,
    ) = navigation.start(destination)

    @Test
    fun eachRequestIsKeptInTheOrderItLeftWithWhatTheAppAsked() {
        val engine = ScriptedNavigationEngine<String>()

        startTrip(engine, "summit")
        engine.start("refuge")
        engine.start("lake")
        assertEquals(listOf("summit"), engine.sent.map { it.request })

        engine.sent[0].answer(route)
        assertEquals(listOf("summit", "lake"), engine.sent.map { it.request })
        assertTrue(engine.sent[0].isAnswered)
        assertFalse(engine.sent[1].isAnswered)
    }

    /** No coroutine, no dispatcher: the state a command leaves is the state's value when it returns. */
    @Test
    fun eachCommandHasItsEffectBeforeItReturns() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("summit")
        engine.sent.single().answer(route)
        engine.setPosition(listOf(0.01, 0.0))

        assertEquals(NavigationStatus.Arrived, engine.state.value.status)
    }

    @Test
    fun aRequestIsAnsweredOnce() {
        val engine = ScriptedNavigationEngine<String>()
        engine.start("summit")
        engine.sent.single().fail(IllegalStateException("offline"))

        val twice = assertFailsWith<IllegalStateException> { engine.sent.single().answer(route) }
        assertTrue(twice.message?.contains("summit") ?: false)
        assertEquals(NavigationStatus.Failed, engine.state.value.status)
    }

    @Test
    fun aStopOrACloseMarksTheRequestInFlightAbandonedAndItsAnswerIsDropped() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("summit")
        engine.stop()
        engine.start("refuge")
        engine.close()

        assertEquals(listOf(true, true), engine.sent.map { it.isAbandoned })
        engine.sent[1].answer(route)
        assertEquals(NavigationStatus.Routing, engine.state.value.status)
        assertEquals("ScriptedRouteRequest(refuge, answered=true, abandoned=true)", engine.sent[1].toString())
    }

    /**
     * A request answered while another waited behind it was replaced, not abandoned: its answer is
     * dropped by the contract's rule, and the engine stops nothing.
     */
    @Test
    fun aReplacedRequestIsNotAbandoned() {
        val engine = ScriptedNavigationEngine<String>()

        engine.start("summit")
        engine.start("refuge")
        engine.sent[0].answer(route)

        assertFalse(engine.sent[0].isAbandoned)
        assertEquals(NavigationStatus.Routing, engine.state.value.status)
    }

    @Test
    fun theSentListIsASnapshot() {
        val engine = ScriptedNavigationEngine<String>()
        val before = engine.sent

        engine.start("summit")

        assertTrue(before.isEmpty())
        assertEquals(1, engine.sent.size)
    }

    /**
     * A collector that runs where a state is set may stop the engine as `routing` is handed on: the
     * request that state asked for never leaves, and a stop later abandons the one that did by its own
     * ticket.
     */
    @Test
    fun aRequestStoppedBeforeItLeftIsNeverSentAndTheNextIsAbandonedByItsOwnTicket() =
        runTest {
            val engine = ScriptedNavigationEngine<String>()
            var stops = 0
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                engine.state.collect { state ->
                    if (state.status == NavigationStatus.Routing && stops == 0) {
                        stops += 1
                        engine.stop()
                    }
                }
            }

            engine.start("summit")
            assertEquals(emptyList(), engine.sent.map { it.request })
            assertEquals(NavigationStatus.Idle, engine.state.value.status)

            engine.start("refuge")
            engine.stop()
            assertEquals(listOf("refuge"), engine.sent.map { it.request })
            assertTrue(engine.sent.single().isAbandoned)
        }
}
