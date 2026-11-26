package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class OverlayInstantTest {
    private val pinned = Instant.parse("2026-03-14T09:26:53.900Z")

    @Test
    fun aPinnedInstantIsItselfWhateverTheClockReads() {
        for (now in listOf(1773489600000L, 1773489659999L, 1773480000000L, -30000L)) {
            assertEquals(pinned, overlayInstant(pinned, Instant.fromEpochMilliseconds(now)))
        }
        val nanosecond = Instant.fromEpochSeconds(1773480413, 900_000_001)
        assertEquals(nanosecond, overlayInstant(nanosecond, Instant.fromEpochMilliseconds(0)))
    }

    @Test
    fun withNonePinnedTheClocksReadingIsFlooredToItsMinute() {
        fun at(now: Long): Long = overlayInstant(null, Instant.fromEpochMilliseconds(now)).toEpochMilliseconds()

        assertEquals(1773489600000, at(1773489600000))
        assertEquals(1773489600000, at(1773489659999))
        assertEquals(1773489660000, at(1773489660000))
        // A reading before the epoch falls in the minute before it, never truncated toward zero.
        assertEquals(-60000, at(-30000))
        assertEquals(-60000, at(-1))
        assertEquals(-60000, at(-60000))
        assertEquals(0, overlayInstant(null, Instant.fromEpochSeconds(59, 999_999_999)).toEpochMilliseconds())
    }
}
