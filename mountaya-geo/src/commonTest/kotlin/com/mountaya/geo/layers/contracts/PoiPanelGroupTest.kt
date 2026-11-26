package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class PoiPanelGroupTest {
    @Test
    fun twoGroupsAreEqualWhereEveryMemberIs() {
        val water = PoiPanelGroup("water", "Water", listOf("spring", "fountain"))

        assertNull(water.glyph)
        assertEquals(water, PoiPanelGroup("water", "Water", listOf("spring", "fountain")))
        assertNotEquals(water, PoiPanelGroup("water", "Water", listOf("fountain", "spring")))
        assertNotEquals(water, PoiPanelGroup("water", "Water", listOf("spring")))
        assertNotEquals(water, PoiPanelGroup("water", "Water", listOf("spring", "fountain"), glyph = "pin"))
        assertNotEquals(PoiPanelGroup("é", "", emptyList()), PoiPanelGroup("é", "", emptyList()))
    }
}
