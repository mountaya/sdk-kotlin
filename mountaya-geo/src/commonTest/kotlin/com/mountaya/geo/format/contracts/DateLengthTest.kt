package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class DateLengthTest {
    @Test
    fun theLengthsRunFromTheShortestToTheFullest() {
        assertEquals(listOf("Short", "Medium", "Long", "Full"), DateLength.entries.map { it.name })
    }
}
