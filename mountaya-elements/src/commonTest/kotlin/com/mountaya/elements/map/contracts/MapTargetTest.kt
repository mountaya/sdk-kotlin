package com.mountaya.elements

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MapTargetTest {
    @Test
    fun twoTargetsAreEqualWhereTheirPositionAndTheirStretchBothAre() {
        val stretch = GeoJsonLineString(listOf(listOf(6.0, 45.0), listOf(6.0, 45.01)))
        val other = GeoJsonLineString(listOf(listOf(6.0, 45.01), listOf(6.0, 45.02)))
        val hut = listOf(6.0, 45.005)

        assertEquals(MapTarget(), MapTarget(), "a target of nothing is nothing")
        assertEquals(MapTarget(hut, stretch), MapTarget(listOf(6.0, 45.005), stretch))
        assertEquals(MapTarget(hut, stretch).hashCode(), MapTarget(listOf(6.0, 45.005), stretch).hashCode())
        assertNotEquals(MapTarget(hut, stretch), MapTarget(listOf(6.0, 45.01), stretch))
        assertNotEquals(MapTarget(hut, stretch), MapTarget(hut, other))
        assertNotEquals(MapTarget(hut), MapTarget(hut, stretch), "a stretch lit is not none")
        assertNotEquals(MapTarget(), MapTarget(hut))
    }
}
