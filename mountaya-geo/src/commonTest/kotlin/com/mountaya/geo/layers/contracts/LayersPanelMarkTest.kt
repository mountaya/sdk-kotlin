package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class LayersPanelMarkTest {
    @Test
    fun eachMarkIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("lock", "tick"), LayersPanelMark.entries.map { it.id })
    }
}
