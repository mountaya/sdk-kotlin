package com.mountaya.navigation

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The source over an app's own flow of positions, collected from a start to a stop in the caller's scope,
 * whose failures reach the app's `onError` and never its scope.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FlowLocationSourceTest {
    @Test
    fun eachPositionTheFlowEmitsBetweenAStartAndAStopIsStated() =
        runTest {
            val fixes = MutableSharedFlow<List<Double>>()
            val source = FlowLocationSource(backgroundScope, fixes)
            val stated = mutableListOf<List<Double>>()

            fixes.emit(listOf(6.0, 45.0))
            source.start { stated += it }
            source.start { stated += listOf(0.0, 0.0) }
            runCurrent()
            fixes.emit(listOf(6.1, 45.1))
            fixes.emit(listOf(6.2, 45.2))
            runCurrent()
            source.stop()
            fixes.emit(listOf(6.3, 45.3))
            runCurrent()

            assertEquals(listOf(listOf(6.1, 45.1), listOf(6.2, 45.2)), stated, "between the start and the stop, once")
        }

    /** Each start collects the flow again: a cold flow from its beginning. */
    @Test
    fun aNavigationStartedAgainCollectsAColdFlowAgainFromItsBeginning() =
        runTest {
            var collections = 0
            val track =
                flow {
                    collections += 1
                    emit(listOf(6.0, 45.0))
                    emit(listOf(6.1, 45.1))
                }
            val source = FlowLocationSource(backgroundScope, track)
            val stated = mutableListOf<List<Double>>()

            source.start { stated += it }
            runCurrent()
            source.stop()
            source.start { stated += it }
            runCurrent()
            source.stop()

            assertEquals(2, collections)
            assertEquals(4, stated.size)
        }

    /** A scope as an app's `viewModelScope` is, which every failure that reaches it would take down, recording each. */
    private fun TestScope.appScope(reached: MutableList<Throwable>): CoroutineScope =
        CoroutineScope(
            SupervisorJob() + UnconfinedTestDispatcher(testScheduler) +
                CoroutineExceptionHandler { _, failure -> reached += failure },
        )

    /** Delivers [position] where it names a place, as the engine does, refusing it otherwise. */
    private fun MutableList<List<Double>>.stating(position: List<Double>) {
        require(position[0].isFinite()) { "refused" }
        add(position)
    }

    /**
     * A position the engine refuses ends nothing: it is told to onError, the next one is read, and the app's
     * scope never hears of it.
     */
    @Test
    fun aPositionTheEngineRefusesIsToldToOnErrorAndTheNextOneIsRead() =
        runTest {
            val reached = mutableListOf<Throwable>()
            val told = mutableListOf<Throwable>()
            val fixes =
                flow {
                    emit(listOf(Double.NaN, 45.0))
                    emit(listOf(6.0, 45.0))
                }
            val source = FlowLocationSource(appScope(reached), fixes) { told += it }
            val stated = mutableListOf<List<Double>>()

            source.start { stated.stating(it) }
            runCurrent()

            assertEquals(listOf(listOf(6.0, 45.0)), stated)
            assertIs<IllegalArgumentException>(told.single())
            assertEquals(emptyList(), reached, "the app's scope lives on")
            source.stop()
        }

    /**
     * A flow that fails ends the positions, which a flow cannot go on from: the failure, an `Error` among
     * them, is told to onError and never to the app's scope.
     */
    @Test
    fun aFailureOfTheFlowEndsThePositionsAndIsToldToOnErrorNeverToTheScope() =
        runTest {
            val reached = mutableListOf<Throwable>()
            val told = mutableListOf<Throwable>()
            val lost = IllegalStateException("the fused provider went away")
            val fixes =
                flow {
                    emit(listOf(6.0, 45.0))
                    throw lost
                }
            val source = FlowLocationSource(appScope(reached), fixes) { told += it }
            val stated = mutableListOf<List<Double>>()

            source.start { stated.stating(it) }
            runCurrent()
            source.stop()
            source.start { stated.stating(it) }
            runCurrent()
            val unimplemented = FlowLocationSource(appScope(reached), flow { TODO("no fixes yet") }) { told += it }
            unimplemented.start { stated.stating(it) }
            runCurrent()

            assertEquals(listOf(listOf(6.0, 45.0), listOf(6.0, 45.0)), stated, "started again, the flow is read again")
            assertEquals(listOf<Throwable>(lost, lost), told.take(2))
            assertIs<NotImplementedError>(told[2])
            assertEquals(emptyList(), reached, "the app's scope lives on")
            source.stop()
            unimplemented.stop()
        }

    /**
     * A stop's cancellation is no failure, and a cancellation the flow throws of its own, a timeout inside it,
     * is one; with no onError, every failure is dropped and the scope still hears none.
     */
    @Test
    fun aStopIsNoFailureAFlowsOwnCancellationIsAndWithNoOnErrorEachIsDropped() =
        runTest {
            val reached = mutableListOf<Throwable>()
            val told = mutableListOf<Throwable>()
            val waiting = FlowLocationSource(appScope(reached), MutableSharedFlow()) { told += it }
            waiting.start {}
            runCurrent()
            waiting.stop()
            runCurrent()
            assertEquals(emptyList(), told, "a stop is no failure")

            val timedOut = CancellationException("no fix within the minute")
            val timing = FlowLocationSource(appScope(reached), flow<List<Double>> { throw timedOut }) { told += it }
            timing.start {}
            runCurrent()
            assertEquals(listOf<Throwable>(timedOut), told)

            val unheard =
                FlowLocationSource(
                    appScope(reached),
                    flow {
                        emit(listOf(Double.NaN, 45.0))
                        throw IllegalStateException("lost")
                    },
                )
            unheard.start { position -> require(position[0].isFinite()) { "refused" } }
            runCurrent()

            assertEquals(emptyList(), reached, "nothing reaches the app's scope")
            timing.stop()
            unheard.stop()
        }
}
