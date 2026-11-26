package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class ControlMoveTest {
    @Test
    fun eachMoveIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("zoom-in", "zoom-out", "reset-north"), ControlMove.entries.map { it.id })
    }
}
