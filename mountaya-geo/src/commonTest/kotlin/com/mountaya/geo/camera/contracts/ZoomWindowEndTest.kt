package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomWindowEndTest {
    @Test
    fun eachEndIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("none", "floor", "ceiling", "both"), ZoomWindowEnd.entries.map { it.id })
    }
}
