package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class PoiPanelRowTest {
    @Test
    fun twoRowsAreEqualWhereEveryMemberIs() {
        val row = PoiPanelRow("water", "Water", keys = listOf("spring"), isVisible = true)

        assertNull(row.glyph)
        assertEquals(row, PoiPanelRow("water", "Water", keys = listOf("spring"), isVisible = true))
        assertNotEquals(row, PoiPanelRow("water", "Water", keys = listOf("spring"), isVisible = false))
        assertNotEquals(row, PoiPanelRow("water", "Water", keys = emptyList(), isVisible = true))
        assertNotEquals(row, PoiPanelRow("water", "Water", glyph = "pin", keys = listOf("spring"), isVisible = true))
    }
}
