package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeSchemeTest {
    @Test
    fun theTwoSchemesAreSpelledAsTheContractSpellsThem() {
        assertEquals(listOf("light", "dark"), ThemeScheme.entries.map { it.id })
        assertEquals(ThemeScheme.Dark, ThemeScheme.entries.firstOrNull { it.id == "dark" })
        assertEquals(null, ThemeScheme.entries.firstOrNull { it.id == "Dark" })
    }
}
