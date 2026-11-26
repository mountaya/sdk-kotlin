package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationInstructionArgumentTest {
    @Test
    fun anInstructionTakesAnExitAndAName() {
        assertEquals(listOf("exit", "name"), NavigationInstructionArgument.entries.map { it.id })
    }
}
