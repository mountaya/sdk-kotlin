package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SectionChipStatTest {
    @Test
    fun eachFigureIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("distance", "ascent", "descent", "grade"), SectionChipStat.entries.map { it.id })
    }
}
