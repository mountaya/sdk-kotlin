package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParsePaceClockTest {
    @Test
    fun aClockOrABareNumberOfMinutesIsRead() {
        assertEquals(13.333333333333334, parsePaceClock("13:20"))
        assertEquals(4.083333333333333, parsePaceClock("4:05"))
        assertEquals(13.0, parsePaceClock("13"))
        assertEquals(6.5, parsePaceClock("6,5"))
        assertEquals(6.5, parsePaceClock("6.5"))
        assertEquals(7.75, parsePaceClock("  7:45 "))
    }

    @Test
    fun anythingElseIsNoPace() {
        for (text in listOf(
            "13:5",
            "13:60",
            "1:02:03",
            "0:00",
            "0",
            "fast",
            "",
            "-4",
            "+4",
            "4.",
            ".5",
            "4:05 min",
            "4 :05",
        )) {
            assertNull(parsePaceClock(text), "`$text`")
        }
    }
}
