package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class FormatTimestampTest {
    @Test
    fun anInstantIsWrittenInUtcToTheMillisecondWithThreeDigitsAlways() {
        assertEquals("2026-03-14T09:26:53.000Z", formatTimestamp(Instant.parse("2026-03-14T09:26:53Z")))
        assertEquals("2026-03-14T09:26:53.120Z", formatTimestamp(Instant.parse("2026-03-14T09:26:53.12Z")))
        assertEquals("2026-03-14T09:26:53.000Z", formatTimestamp(Instant.parse("2026-03-14T10:26:53+01:00")))
    }

    @Test
    fun theDigitsPastTheMillisecondAreDroppedAndNeverRounded() {
        assertEquals("2026-03-14T09:26:53.999Z", formatTimestamp(Instant.parse("2026-03-14T09:26:53.999999999Z")))
        assertEquals("2026-03-14T09:26:53.123Z", formatTimestamp(Instant.parse("2026-03-14T09:26:53.123999Z")))
    }

    @Test
    fun anInstantBeforeTheEpochIsFlooredToo() {
        assertEquals("1969-12-31T23:59:59.999Z", formatTimestamp(Instant.fromEpochSeconds(-1, 999_999_999)))
        assertEquals("1969-12-31T23:59:59.000Z", formatTimestamp(Instant.fromEpochSeconds(-1, 500)))
    }

    @Test
    fun anInstantIsWrittenToTheSecondTheFractionDroppedBeforeTheEpochToo() {
        assertEquals("2026-11-26T00:00:00Z", formatTimestampToTheSecond(Instant.parse("2026-11-26T00:00:00.999Z")))
        assertEquals(
            "2026-11-14T00:00:01Z",
            formatTimestampToTheSecond(Instant.fromEpochMilliseconds(1_794_614_401_250)),
        )
        assertEquals("1969-12-31T23:59:59Z", formatTimestampToTheSecond(Instant.fromEpochSeconds(-1, 999_999_999)))
    }
}
