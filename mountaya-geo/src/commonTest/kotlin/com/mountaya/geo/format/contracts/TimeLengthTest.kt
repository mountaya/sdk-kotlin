package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeLengthTest {
    /** A long and a full time name the zone, so a time of day has the two shortest lengths alone. */
    @Test
    fun theLengthsAreTheShortestTwo() {
        assertEquals(listOf("Short", "Medium"), TimeLength.entries.map { it.name })
    }
}
