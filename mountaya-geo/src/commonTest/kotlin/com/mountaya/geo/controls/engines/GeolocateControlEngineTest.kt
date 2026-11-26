package com.mountaya.geo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Every read and fix runs in the test's background scope and suspends on a deferred the test completes, so
 * `runCurrent` lets each one land, in the order sent, without a clock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GeolocateControlEngineTest {
    /** A state as `busy|blocked|live`, each `y` or `n`. */
    private fun shown(state: GeolocateControlState): String =
        listOf(state.isBusy, state.isBlocked, state.isLive).joinToString("|") { if (it) "y" else "n" }

    @Test
    fun aControlReadsThePermissionAsItComesToStandOverAMapAndAsksForNoFix() =
        runTest {
            val map = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, map)
            assertEquals("n|n|y", shown(engine.state.value))
            runCurrent()
            map.answer(0, refused = true)
            runCurrent()

            assertEquals("n|y|y", shown(engine.state.value))
            assertEquals(1, map.reads)
            assertEquals(0, map.fixes)
            map.release()
            engine.close()
        }

    @Test
    fun aPressIsBusyUntilEveryFixItAskedForSettlesEachFailureReported() =
        runTest {
            val map = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, map)
            var reported = 0
            runCurrent()
            map.answer(0, refused = false)
            engine.locate(onError = { reported += 1 })
            engine.locate(onError = { reported += 1 })
            assertEquals("y|n|y", shown(engine.state.value))
            runCurrent()

            map.land(0)
            runCurrent()
            assertEquals("y|n|y", shown(engine.state.value))
            map.refuse(1)
            runCurrent()

            assertEquals("n|n|y", shown(engine.state.value))
            assertEquals(1, reported)
            assertEquals(2, map.fixes)
            // A read after each fix that settled.
            assertEquals(3, map.reads)
            map.release()
            engine.close()
        }

    @Test
    fun blockedOnlyOnThePermissionsWordARefusedFixWithThePermissionUnaskedIsNot() =
        runTest {
            val map = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, map)
            runCurrent()
            map.answer(0, refused = false)
            engine.locate()
            runCurrent()
            map.refuse(0)
            runCurrent()
            assertEquals("n|n|y", shown(engine.state.value))
            map.answer(1, refused = false)
            runCurrent()
            assertEquals("n|n|y", shown(engine.state.value))

            engine.locate()
            runCurrent()
            map.refuse(1)
            runCurrent()
            map.answer(2, refused = true)
            runCurrent()
            assertEquals("n|y|y", shown(engine.state.value))

            // A fix that lands lowers the blocked wording once its read says so.
            engine.locate()
            runCurrent()
            map.land(2)
            runCurrent()
            assertEquals("n|y|y", shown(engine.state.value))
            map.answer(3, refused = false)
            runCurrent()
            assertEquals("n|n|y", shown(engine.state.value))
            map.release()
            engine.close()
        }

    @Test
    fun aReadThatFailsSaysNothingIsBlockedAndOneThatNeverAnswersChangesNothing() =
        runTest {
            val map = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, map)
            runCurrent()
            map.answer(0, refused = true)
            engine.locate()
            runCurrent()
            map.refuse(0)
            runCurrent()
            assertEquals("n|y|y", shown(engine.state.value))

            map.fail(1)
            runCurrent()

            assertEquals("n|n|y", shown(engine.state.value))
            map.release()
            engine.close()
        }

    @Test
    fun anAnswerFromAnEarlierReadLandsNowhere() =
        runTest {
            val map = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, map)
            engine.locate()
            runCurrent()
            map.land(0)
            runCurrent()
            map.answer(1, refused = true)
            runCurrent()

            map.answer(0, refused = false)
            runCurrent()

            assertEquals("n|y|y", shown(engine.state.value))
            map.release()
            engine.close()
        }

    @Test
    fun aMapInPlaceOfAnotherSaysNothingIsBlockedUntilItIsReadOwingTheOtherNothing() =
        runTest {
            val a = HeldLocateMap()
            val b = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, a)
            runCurrent()
            a.answer(0, refused = true)
            runCurrent()
            assertEquals("n|y|y", shown(engine.state.value))

            engine.locate()
            engine.configure(b)
            assertEquals("y|n|y", shown(engine.state.value))
            runCurrent()
            a.refuse(0)
            runCurrent()
            // The fix settled after its map was replaced: nothing is read through it.
            assertEquals("n|n|y", shown(engine.state.value))
            assertEquals(1, a.reads)
            b.answer(0, refused = true)
            runCurrent()
            assertEquals("n|y|y", shown(engine.state.value))

            engine.configure(b)
            assertEquals(1, b.reads)
            engine.configure(null)
            assertEquals("n|n|n", shown(engine.state.value))
            engine.locate()
            runCurrent()
            assertEquals(0, b.fixes)
            a.release()
            b.release()
            engine.close()
        }

    @Test
    fun aControlWithNoMapAsksNothingAndIsNotLive() =
        runTest {
            val engine = GeolocateControlEngine(backgroundScope)
            engine.locate()
            engine.configure(null)
            runCurrent()

            assertEquals("n|n|n", shown(engine.state.value))
            engine.close()
        }

    /**
     * A state flow is the stream: a collector reads the state as it is now, then each state that differs. It never
     * ends, so a closed engine is told by nothing landing, and nothing reported, after it.
     */
    @Test
    fun theStateReportsEachStateThatDiffersAndAClosedEngineReportsAndReadsNothing() =
        runTest {
            val map = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, map)
            val seen = mutableListOf<String>()
            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler),
            ) { engine.state.collect { seen += shown(it) } }
            var reported = 0
            runCurrent()
            engine.locate(onError = { reported += 1 })
            runCurrent()

            engine.close()
            map.refuse(0)
            map.answer(0, refused = true)
            runCurrent()

            assertEquals(listOf("n|n|y", "y|n|y"), seen)
            assertEquals(0, reported)
            assertEquals("y|n|y", shown(engine.state.value))
            map.release()
        }

    /** Every engine is closeable as a resource, so `use` stops it: nothing it asked for lands after. */
    @Test
    fun theEngineIsClosedAsAResource() =
        runTest {
            val map = HeldLocateMap()
            val engine = GeolocateControlEngine(backgroundScope, map)
            var reported = 0

            engine.use { it.locate(onError = { reported += 1 }) }
            runCurrent()
            map.refuse(0)
            runCurrent()

            assertEquals(0, reported)
            map.release()
        }
}
