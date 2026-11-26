package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileAnchorTest {
    @Test
    fun eachAnchorIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("route", "sea-level", "window"), ProfileAnchor.entries.map { it.id })
        assertEquals(ProfileAnchor.SeaLevel, ProfileAnchor.entries.firstOrNull { it.id == "sea-level" })
        assertNull(ProfileAnchor.entries.firstOrNull { it.id == "seaLevel" })
    }
}
