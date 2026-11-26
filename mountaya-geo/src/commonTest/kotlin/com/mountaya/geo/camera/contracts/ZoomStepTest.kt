package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomStepTest {
    @Test
    fun eachControlIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("zoom-in", "zoom-out"), ZoomStep.entries.map { it.id })
    }
}
