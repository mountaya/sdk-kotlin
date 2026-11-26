package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTokenEngineTest {
    private class MintFailed : Exception("the mint failed")

    /** Counts the mints, answering each with the next of [sessions], built at the time it is asked for. */
    private class Mints(
        private val sessions: List<(TestScope) -> RestSchema.Session>,
    ) {
        var count = 0

        fun mint(scope: TestScope): RestSchema.Session {
            val index = minOf(count, sessions.size - 1)
            count += 1
            return sessions[index](scope)
        }
    }

    private fun session(
        token: String,
        lifetime: Duration,
        user: String? = "u1",
    ): (TestScope) -> RestSchema.Session =
        { scope ->
            RestSchema.Session(expires_at = Scripted.clock(scope).now() + lifetime, token = token, user_id = user)
        }

    private fun failure(): (TestScope) -> RestSchema.Session = { throw MintFailed() }

    private fun TestScope.engine(mints: Mints): SessionTokenEngine =
        SessionTokenEngine(backgroundScope, mint = { mints.mint(this) }, clock = Scripted.clock(this))

    @Test
    fun theSnapshotIsTheStateNowAndReadingItMintsNothing() =
        runTest {
            val mints = Mints(listOf(session("sess_a", 300.seconds)))
            val engine = engine(mints)
            val source: SessionTokenSource = engine

            assertEquals(SessionSnapshot(), engine.snapshot.value)
            advanceTimeBy(20.seconds)
            assertEquals(SessionSnapshot(), engine.snapshot.value)
            assertEquals(0, mints.count, "a read is no subscription")

            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(1, mints.count)
            assertEquals("sess_a", engine.snapshot.value.token)
            assertSame(engine.snapshot, source.snapshot, "the engine is a source by its one state")
        }

    @Test
    fun nothingIsMintedBeforeASubscriptionAndOneMintsAndReportsTheTokenItsUserAndExpiry() =
        runTest {
            val mints = Mints(listOf(session("sess_a", 300.seconds), session("sess_b", 300.seconds)))
            val engine = engine(mints)
            advanceTimeBy(20.seconds)
            assertEquals(0, mints.count)

            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals("sess_a", engine.snapshot.value.token)
            assertEquals("u1", engine.snapshot.value.userId)
            assertEquals(Scripted.clock(this).now() + 300.seconds, engine.snapshot.value.expiresAt)

            // Lifetime less the smaller of the 60-second margin and half the lifetime: 240 seconds.
            advanceTimeBy(240.seconds - 1.seconds)
            assertEquals(1, mints.count)
            advanceTimeBy(1.seconds)
            runCurrent()
            assertEquals(2, mints.count)
            assertEquals("sess_b", engine.snapshot.value.token)
        }

    @Test
    fun aSpentTokenAndAFailedMintWaitTheFloorAndAFailureKeepsTheTokenHeld() =
        runTest {
            val mints = Mints(listOf(session("sess_a", (-10).seconds), failure(), session("sess_c", 300.seconds)))
            val engine = engine(mints)
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(1, mints.count)

            advanceTimeBy(5.seconds)
            runCurrent()
            assertEquals(2, mints.count)
            assertIs<MintFailed>(engine.snapshot.value.error)
            assertEquals("sess_a", engine.snapshot.value.token)
            assertFalse(engine.snapshot.value.isMinting)

            // A failed mint after a spent token is the second spent in a row: twice the floor.
            advanceTimeBy(10.seconds - 1.seconds)
            assertEquals(2, mints.count)
            advanceTimeBy(1.seconds)
            runCurrent()
            assertEquals(3, mints.count)
            assertNull(engine.snapshot.value.error)
        }

    /**
     * A failed mint waits as a spent token does: the floor after the first of a run, twice the wait
     * before it after each one that follows, up to the margin; a token whose timer is armed ends the run.
     */
    @Test
    fun failedMintsInARowWaitTwiceAsLongEachUpToTheMarginAndAnArmedTokenEndsTheRun() =
        runTest {
            val failures = List(6) { failure() }
            val mints =
                Mints(failures + session("sess_a", 300.seconds) + failure() + session("sess_b", 300.seconds))
            val engine = engine(mints)
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(1, mints.count)

            for ((index, wait) in listOf(5, 10, 20, 40, 60, 60).withIndex()) {
                advanceTimeBy(wait.seconds - 1.seconds)
                assertEquals(index + 1, mints.count, "a failure in a row of ${index + 1} waits $wait seconds")
                advanceTimeBy(1.seconds)
                runCurrent()
                assertEquals(index + 2, mints.count)
            }
            assertEquals("sess_a", engine.snapshot.value.token)
            assertNull(engine.snapshot.value.error)

            // The token's timer is armed, 240 seconds on, and the failure after it waits the floor again.
            advanceTimeBy(240.seconds)
            runCurrent()
            assertEquals(8, mints.count)
            assertIs<MintFailed>(engine.snapshot.value.error)
            assertEquals("sess_a", engine.snapshot.value.token)
            advanceTimeBy(5.seconds)
            runCurrent()
            assertEquals(9, mints.count)
            assertEquals("sess_b", engine.snapshot.value.token)
        }

    @Test
    fun theLastSubscriptionEndingParksTheEngineAndReplacingTheMintMintsNothing() =
        runTest {
            val mints = Mints(listOf(session("sess_a", 300.seconds)))
            val engine = engine(mints)
            val reader = backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(1, mints.count)
            engine.mint =
                { RestSchema.Session(expires_at = Scripted.clock(this).now(), token = "sess_b", user_id = null) }
            reader.cancel()
            advanceTimeBy(3_600.seconds)

            assertEquals(1, mints.count)
            assertEquals("sess_a", engine.snapshot.value.token)
        }

    @Test
    fun disablingClearsTheTokenItsUserItsExpiryAndTheLastErrorAndStopsMinting() =
        runTest {
            val mints = Mints(listOf(session("sess_a", 300.seconds)))
            val engine = engine(mints)
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            engine.disable()
            engine.refresh()
            advanceTimeBy(600.seconds)

            assertEquals(SessionSnapshot(), engine.snapshot.value)
            assertEquals(1, mints.count)
            engine.enable()
            runCurrent()
            assertEquals(2, mints.count)
        }

    @Test
    fun eachSubscriptionIsToldTheSnapshotNowAndThenEachChange() =
        runTest {
            val engine = engine(Mints(listOf(session("sess_a", 300.seconds))))
            val told = listOf(mutableListOf<String?>(), mutableListOf())
            for (list in told) backgroundScope.launch { engine.snapshot.collect { list += it.token } }
            runCurrent()

            assertEquals(listOf(null, "sess_a"), told[0].distinct())
            assertEquals(told[0].distinct(), told[1].distinct())
        }

    @Test
    fun aRefreshMintsAtOnceWhileASubscriptionIsHeld() =
        runTest {
            val mints = Mints(listOf(session("sess_a", 300.seconds), session("sess_b", 300.seconds)))
            val engine = engine(mints)
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            engine.refresh()
            runCurrent()

            assertEquals(2, mints.count)
            assertEquals("sess_b", engine.snapshot.value.token)
        }

    /**
     * N52: a cancellation is never a failed mint. One the mint raised of its own, the application
     * cancelling its call, is not minted again on the schedule a failure waits; it passes through, and
     * the next mint is the application's to ask for.
     */
    @Test
    fun aMintThatThrowsACancellationIsNeverRepeatedAndTheNextMintIsARefreshs() =
        runTest {
            val mints =
                Mints(
                    listOf(
                        { throw CancellationException("the application cancelled its call") },
                        session("sess_b", 300.seconds),
                    ),
                )
            val engine = engine(mints)
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()

            assertEquals(1, mints.count)
            assertIs<CancellationException>(engine.snapshot.value.error)
            assertFalse(engine.snapshot.value.isMinting, "a request awaiting a first token goes on at once")
            assertNull(engine.snapshot.value.token)

            advanceTimeBy(3_600.seconds)
            assertEquals(1, mints.count, "the schedule a failure waits mints nothing after a cancellation")

            engine.refresh()
            runCurrent()
            assertEquals(2, mints.count)
            assertEquals("sess_b", engine.snapshot.value.token)
            assertNull(engine.snapshot.value.error)
        }

    /**
     * SPEC.md section 4: after the application cancelled a mint, the next subscription mints again, as `sdk-swift`
     * and `sdk-dart` start on each new subscription, even where a subscription was held all along, as a `Mountaya`
     * holds one for its whole life. One arriving while the engine mints on its schedule restarts nothing.
     */
    @Test
    fun aSubscriptionArrivingAfterTheApplicationCancelledAMintMintsAgainWhileAnotherIsHeld() =
        runTest {
            val mints =
                Mints(
                    listOf(
                        { throw CancellationException("the application cancelled its call") },
                        session("sess_b", 300.seconds),
                        session("sess_c", 300.seconds),
                    ),
                )
            val engine = engine(mints)
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(1, mints.count)
            assertIs<CancellationException>(engine.snapshot.value.error)
            advanceTimeBy(3_600.seconds)
            assertEquals(1, mints.count, "nothing mints until it is asked to")

            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(2, mints.count, "the subscription that arrived mints again")
            assertEquals("sess_b", engine.snapshot.value.token)
            assertNull(engine.snapshot.value.error)

            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(
                2,
                mints.count,
                "a subscription arriving while the engine mints on its schedule restarts nothing",
            )
            advanceTimeBy(300.seconds)
            assertEquals(3, mints.count, "the schedule goes on as it stood")
        }

    /**
     * A deadline the application set on its own call ends that call and nothing else, so the mint is a
     * failed one, waited out on the schedule: the floor, then minted again.
     */
    @Test
    fun aMintTheApplicationTimesOutIsAFailureMintedAgainAfterTheFloor() =
        runTest {
            var asked = 0
            val engine =
                SessionTokenEngine(
                    backgroundScope,
                    mint = {
                        asked += 1
                        if (asked == 1) withTimeout(2.seconds) { awaitCancellation() }
                        RestSchema.Session(
                            expires_at = Scripted.clock(this).now() + 300.seconds,
                            token = "sess_b",
                            user_id = null,
                        )
                    },
                    clock = Scripted.clock(this),
                )
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()

            assertEquals(1, asked)
            assertIs<TimeoutCancellationException>(engine.snapshot.value.error)
            assertNull(engine.snapshot.value.token)

            advanceTimeBy(5.seconds)
            runCurrent()
            assertEquals(2, asked)
            assertEquals("sess_b", engine.snapshot.value.token)
            assertNull(engine.snapshot.value.error)
        }

    @Test
    fun anEngineWhoseScopeIsCancelledLeavesNoMintRunning() =
        runTest {
            val scope = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
            var asked = 0
            var cancelled = false
            val engine =
                SessionTokenEngine(
                    scope,
                    mint = {
                        asked += 1
                        try {
                            delay(1.hours)
                        } catch (cancellation: CancellationException) {
                            cancelled = true
                            throw cancellation
                        }
                        RestSchema.Session(expires_at = Scripted.clock(this).now(), token = "sess_a", user_id = null)
                    },
                    clock = Scripted.clock(this),
                )
            backgroundScope.launch { engine.snapshot.collect() }
            runCurrent()
            assertEquals(1, asked)

            scope.cancel()
            runCurrent()
            assertTrue(cancelled, "the mint in flight is cancelled with its engine")
            assertTrue(scope.coroutineContext[Job]?.children?.none() ?: false, "nothing of the engine runs")

            advanceTimeBy(3_600.seconds)
            assertEquals(1, asked)
            assertNull(engine.snapshot.value.token)
        }
}
