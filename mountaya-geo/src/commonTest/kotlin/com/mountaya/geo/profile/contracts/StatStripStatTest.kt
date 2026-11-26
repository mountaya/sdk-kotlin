package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class StatStripStatTest {
    @Test
    fun eachFigureIsSpelledAsTheContractSpellsIt() {
        assertEquals(
            listOf("distance", "duration", "ascent", "descent", "elevation", "grade", "difficulty"),
            StatStripStat.entries.map { it.id },
        )
    }
}
