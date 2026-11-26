package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

@OptIn(InternalMountayaApi::class)
class ParseTimestampTest {
    private val instant = Instant.fromEpochSeconds(1_773_480_413)

    @Test
    fun aFullTimestampWithAZoneIsAnInstant() {
        assertEquals(instant, parseTimestamp("2026-03-14T09:26:53Z"))
        assertEquals(instant, parseTimestamp("2026-03-14T09:26:53.000Z"))
        assertEquals(instant, parseTimestamp("2026-03-14T10:26:53+01:00"))
        assertEquals(instant, parseTimestamp("2026-03-14T04:26:53-05:00"))
        assertEquals(instant, parseTimestamp("2026-03-14T04:56:53-04:30"))
        assertEquals(instant, parseTimestamp("2026-03-14T09:26:53-00:00"))
    }

    @Test
    fun everyOffsetTheGrammarWritesIsRead() {
        assertEquals(instant, parseTimestamp("2026-03-15T09:25:53+23:59"))
        assertEquals(instant, parseTimestamp("2026-03-13T09:27:53-23:59"))
    }

    @Test
    fun aFractionKeepsItsPrecision() {
        assertEquals(Instant.fromEpochSeconds(1_773_480_413, 500_000_000), parseTimestamp("2026-03-14T09:26:53.5Z"))
        val nanosecond = Instant.fromEpochSeconds(1_773_480_413, 123_456_789)
        assertEquals(nanosecond, parseTimestamp("2026-03-14T09:26:53.123456789Z"))
    }

    @Test
    fun aFractionPastTheNanosecondIsReadToTheNanosecond() {
        val nanosecond = Instant.fromEpochSeconds(1_773_480_413, 123_456_789)
        assertEquals(nanosecond, parseTimestamp("2026-03-14T09:26:53.123456789987Z"))
    }

    @Test
    fun aValueThatIsNotWhollyATimestampStaysAString() {
        val values =
            listOf(
                "2026-03-14",
                "09:26:53Z",
                "2026-03-14T09:26:53.000Z looks like a date",
                "on 2026-03-14T09:26:53Z",
                "itin_1",
                "",
                "P1D",
            )
        for (value in values) assertNull(parseTimestamp(value), value)
    }

    @Test
    fun aTimestampWithoutAZoneStaysAString() {
        assertNull(parseTimestamp("2026-03-14T09:26:53"))
        assertNull(parseTimestamp("2026-03-14T09:26:53.000"))
    }

    @Test
    fun eachChoiceTheContractLeavesOpenIsResolvedTowardAString() {
        val values =
            listOf(
                "2026-03-14t09:26:53Z",
                "2026-03-14T09:26:53z",
                "2026-03-14T10:26:53+0100",
                "2026-03-14T10:26:53+01",
                "2026-03-14 09:26:53Z",
                "2026-03-14T09:26Z",
                "+2026-03-14T09:26:53Z",
            )
        for (value in values) assertNull(parseTimestamp(value), value)
    }

    @Test
    fun aComponentOutOfRangeIsNotAnInstant() {
        val values =
            listOf(
                "2026-13-14T09:26:53Z",
                "2026-02-30T09:26:53Z",
                "2026-02-29T09:26:53Z",
                "2026-03-14T24:00:00Z",
                "2026-03-14T25:26:53Z",
                "2026-03-14T09:60:53Z",
                "2026-03-14T09:26:60Z",
                "2026-03-14T09:26:53+24:00",
                "2026-03-14T09:26:53+25:00",
                "2026-03-14T09:26:53+01:60",
            )
        for (value in values) assertNull(parseTimestamp(value), value)
    }

    @Test
    fun aFractionWithNoDigitAndATwoDigitYearStayStrings() {
        assertNull(parseTimestamp("2026-03-14T09:26:53.Z"))
        assertNull(parseTimestamp("26-03-14T09:26:53Z"))
    }
}
