package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class LayersPanelChoiceTest {
    @Test
    fun eachChoiceIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("terrain", "overlay", "enrichment", "time"), LayersPanelChoice.entries.map { it.id })
    }
}
