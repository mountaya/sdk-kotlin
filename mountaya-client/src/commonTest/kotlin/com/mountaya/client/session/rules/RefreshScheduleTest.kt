package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class RefreshScheduleTest {
    @Test
    fun theConstantsAreTheContracts() {
        assertEquals(60.seconds, RefreshSchedule.margin)
        assertEquals(5.seconds, RefreshSchedule.floor)
        assertEquals(10.minutes, RefreshSchedule.longestHop)
    }

    @Test
    fun aLongLivedTokenIsMintedAgainAMarginBeforeItsExpiry() {
        val schedule = RefreshSchedule.forLifetime(60.minutes)

        assertEquals(59.minutes, schedule.delay)
        assertTrue(schedule.isArmed)
    }

    @Test
    fun aShortLivedTokenIsMintedAgainHalfwayThroughItsLife() {
        assertEquals(60.seconds, RefreshSchedule.forLifetime(120.seconds).delay)
        assertEquals(45.seconds, RefreshSchedule.forLifetime(90.seconds).delay)
        assertEquals(5.seconds, RefreshSchedule.forLifetime(10.seconds).delay)
        assertTrue(RefreshSchedule.forLifetime(10.seconds).isArmed)
    }

    @Test
    fun aDelayUnderTheFloorIsNotArmed() {
        val justUnder = RefreshSchedule.forLifetime(9_998.milliseconds)
        val expired = RefreshSchedule.forLifetime((-5).seconds)

        assertEquals(4_999.milliseconds, justUnder.delay)
        assertFalse(justUnder.isArmed)
        assertFalse(RefreshSchedule.forLifetime(Duration.ZERO).isArmed)
        assertEquals((-2_500).milliseconds, expired.delay)
        assertFalse(expired.isArmed)
    }

    @Test
    fun anExpiryThatDidNotParseTakesTheFlatMargin() {
        val schedule = RefreshSchedule.forLifetime(null)

        assertEquals(60.seconds, schedule.delay)
        assertTrue(schedule.isArmed)
    }

    @Test
    fun eachSpentTokenWaitsTwiceAsLongUpToTheMargin() {
        val waits = (1..7).map { RefreshSchedule.afterSpentTokens(it).delay }

        assertEquals(listOf(5, 10, 20, 40, 60, 60, 60).map { it.seconds }, waits)
        assertTrue((1..7).all { RefreshSchedule.afterSpentTokens(it).isArmed })
    }

    @Test
    fun aRunOfSpentTokensAsLongAsAnIntStaysAtTheMargin() {
        assertEquals(60.seconds, RefreshSchedule.afterSpentTokens(Int.MAX_VALUE).delay)
    }

    @Test
    fun aCountOfSpentTokensStartsAtOne() {
        assertFailsWith<IllegalArgumentException> { RefreshSchedule.afterSpentTokens(0) }
    }

    private val now = Instant.fromEpochSeconds(1_800_000_000)

    @Test
    fun aHopIsTheRestOfTheWaitWhenThatIsShorterThanTheLongestHop() {
        assertEquals(3.minutes, RefreshSchedule.nextHop(now + 3.minutes, now))
    }

    @Test
    fun aLongWaitIsWalkedInHopsThatEachReReadTheClock() {
        val deadline = now + 59.minutes
        var clock = now
        val hops = mutableListOf<Duration>()
        while (true) {
            val hop = RefreshSchedule.nextHop(deadline, clock)
            if (hop == Duration.ZERO) break
            hops += hop
            clock += hop
        }

        assertEquals(listOf(10, 10, 10, 10, 10, 9).map { it.minutes }, hops)
    }

    @Test
    fun aDeviceThatSleptThroughItsDeadlineMintsOnWaking() {
        val deadline = now + 59.minutes

        assertEquals(Duration.ZERO, RefreshSchedule.nextHop(deadline, now + 3.hours))
        assertEquals(Duration.ZERO, RefreshSchedule.nextHop(deadline, deadline))
    }
}
