package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutStatTest {
    @Test
    fun eachFigureIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("distance", "elevation", "ascent", "descent"), ReadoutStat.entries.map { it.id })
    }
}
