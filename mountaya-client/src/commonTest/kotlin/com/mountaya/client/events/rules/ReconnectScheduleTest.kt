package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ReconnectScheduleTest {
    private val dropped: MountayaException = ConnectionException()

    private val origin = Instant.fromEpochSeconds(0)

    private fun answered(
        status: Int,
        retryAfter: Duration? = null,
    ): MountayaException = ApiException.of(status, retryAfter = retryAfter)

    /** Counts every failure in turn, each ended at its instant, and returns the wait after each, null where the reader stopped, with the count after the last. */
    private fun opens(
        failures: List<Pair<MountayaException?, Duration>>,
        rules: EventStreamRules,
        draw: Double = 1.0,
    ): Pair<List<Duration?>, ReconnectSchedule.Count> {
        var count = ReconnectSchedule.Count()
        val waits = mutableListOf<Duration?>()
        for ((failure, at) in failures) {
            val next = ReconnectSchedule.next(failure, count, origin + at, rules, draw)
            count = next.count
            waits += next.wait
        }
        return waits to count
    }

    @Test
    fun theCeilingDoublesPerOpenWithoutProgressUpToItsCapAndTheDrawScalesIt() {
        val waits = (1..6).map { ReconnectSchedule.drawnWait(it, EventStreamRules.agentRun, 1.0) }

        assertEquals(listOf(500.milliseconds, 1.seconds, 2.seconds, 4.seconds, 8.seconds, 8.seconds), waits)
        assertEquals(1.seconds, ReconnectSchedule.drawnWait(3, EventStreamRules.agentRun, 0.5))
        assertEquals(15.seconds, ReconnectSchedule.drawnWait(7, EventStreamRules.collaborationFeed, 1.0))
    }

    @Test
    fun anAgentRunsReaderStopsPastSixReopensWithoutProgressAndAFeedsNever() {
        val drops = List(7) { dropped to Duration.ZERO }

        assertNull(opens(drops, EventStreamRules.agentRun).first.last())
        assertTrue(opens(drops.take(6), EventStreamRules.agentRun).first.all { it != null })
        val closes = List(1000) { null to Duration.ZERO }
        assertTrue(opens(closes, EventStreamRules.collaborationFeed).first.all { it != null })
        assertTrue(opens(closes, EventStreamRules.collaborationEditor).first.all { it != null })
    }

    @Test
    fun a429OrA503NamingItsWaitIsWaitedOutWithUpToASecondsDrawAboveIt() {
        for (rules in listOf(
            EventStreamRules.agentRun,
            EventStreamRules.collaborationFeed,
            EventStreamRules.collaborationEditor,
        )) {
            for (status in listOf(429, 503)) {
                val named =
                    ReconnectSchedule.next(
                        answered(status, 1.seconds),
                        ReconnectSchedule.Count(),
                        origin,
                        rules,
                        0.5,
                    )
                val thirty =
                    ReconnectSchedule.next(
                        answered(status, 30.seconds),
                        ReconnectSchedule.Count(),
                        origin,
                        rules,
                        1.0,
                    )
                assertEquals(1500.milliseconds, named.wait, "$status")
                assertEquals(31.seconds, thirty.wait, "$status")
            }
        }
    }

    @Test
    fun aWaitOfZeroSecondsNamedInRetryAfterIsTheDrawAloneNeverTheSchedule() {
        for (rules in listOf(
            EventStreamRules.agentRun,
            EventStreamRules.collaborationFeed,
            EventStreamRules.collaborationEditor,
        )) {
            // The schedule's wait would be a quarter of the ceiling's 8 seconds after 5 opens.
            val count = opens(List(4) { dropped to Duration.ZERO }, rules).second
            val zero = ReconnectSchedule.next(answered(429, Duration.ZERO), count, origin, rules, 0.25)
            assertEquals(250.milliseconds, zero.wait)
        }
    }

    @Test
    fun aNamedWaitStillDoublesTheCeilingAndA503NamingNoneWaitsOnTheSchedule() {
        val named =
            opens(
                listOf(answered(429, 1.seconds) to Duration.ZERO, dropped to 2.seconds),
                EventStreamRules.agentRun,
                0.5,
            )
        val unnamed = opens(List(8) { answered(503) to Duration.ZERO }, EventStreamRules.agentRun)

        assertEquals(listOf(1500.milliseconds, 500.milliseconds), named.first)
        assertEquals(
            listOf(500.milliseconds, 1.seconds, 2.seconds, 4.seconds, 8.seconds, 8.seconds, 8.seconds, 8.seconds),
            unnamed.first,
        )
    }

    @Test
    fun onlyA429sAndA503sRetryAfterNameAWait() {
        assertEquals(Duration.ZERO, ReconnectSchedule.namedWait(answered(429, Duration.ZERO)))
        assertEquals(1.seconds, ReconnectSchedule.namedWait(answered(503, 1.seconds)))
        assertNull(ReconnectSchedule.namedWait(answered(504, 1.seconds)))
        assertNull(ReconnectSchedule.namedWait(answered(503)))
        assertNull(ReconnectSchedule.namedWait(dropped))
        assertNull(ReconnectSchedule.namedWait(null))
    }

    @Test
    fun anAgentRunsOpensAnswered429AreCountedAmongTheSix() {
        val waits = opens(List(7) { answered(429, 1.seconds) to Duration.ZERO }, EventStreamRules.agentRun).first

        assertTrue(waits.take(6).all { it == 2.seconds })
        assertNull(waits.last())
    }

    @Test
    fun anAgentRunsOpensAnswered503NeitherAddToTheSixNorClearThem() {
        val unavailable = answered(503, 1.seconds) to Duration.ZERO
        val (waits, count) =
            opens(
                List(5) { dropped to Duration.ZERO } + List(20) { unavailable } + listOf(dropped to Duration.ZERO) +
                    listOf(dropped to Duration.ZERO),
                EventStreamRules.agentRun,
            )

        assertTrue(waits.dropLast(1).all { it != null })
        assertNull(waits.last())
        assertEquals(27, count.opens)
        assertEquals(20, count.unavailableOpens)
    }

    @Test
    fun anAgentRunsOpenAnswered503TwoMinutesOrMoreAfterTheFirstStopsTheReader() {
        val unavailable = answered(503, 30.seconds)
        val within =
            opens(
                listOf(unavailable to 10.seconds, unavailable to 129.seconds + 999.milliseconds),
                EventStreamRules.agentRun,
            )
        val past = opens(listOf(unavailable to 10.seconds, unavailable to 130.seconds), EventStreamRules.agentRun)

        assertEquals(listOf(31.seconds, 31.seconds), within.first)
        assertEquals(origin + 10.seconds, within.second.firstUnavailable)
        assertEquals(listOf(31.seconds, null), past.first)
    }

    @Test
    fun aFailureBetweenTwoOpensAnswered503NeitherRestartsTheTwoMinutesNorStopsThem() {
        val unavailable = answered(503, 30.seconds)
        val waits =
            opens(
                listOf(
                    unavailable to Duration.ZERO,
                    dropped to 31.seconds,
                    dropped to 200.seconds,
                    unavailable to 201.seconds,
                ),
                EventStreamRules.agentRun,
            ).first

        assertEquals(listOf(31.seconds, 1.seconds, 2.seconds, null), waits)
    }

    @Test
    fun aCollaborationFeedsOpensAnswered503NeverStopItsReader() {
        for (rules in listOf(EventStreamRules.collaborationFeed, EventStreamRules.collaborationEditor)) {
            val unavailable = answered(503, 30.seconds)
            val waits = opens((0 until 10).map { unavailable to (31 * it).seconds }, rules).first
            assertTrue(waits.all { it == 31.seconds })
        }
    }
}
