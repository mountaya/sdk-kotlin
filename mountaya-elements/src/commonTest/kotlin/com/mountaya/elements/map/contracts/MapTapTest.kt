package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MapTapTest {
    @Test
    fun aTapIsThePlaceThePointAndTheMarkerTappedEachComparedAsGeosValuesAre() {
        val tap = MapTap(listOf(6.86, 45.83), listOf(120.0, 340.0), marker = "hut")

        assertEquals("hut", tap.marker)
        assertEquals(MapTap(listOf(6.86, 45.83), listOf(120.0, 340.0), marker = "hut"), tap)
        assertNotEquals(MapTap(listOf(6.86, 45.83), listOf(120.0, 340.0)), tap)
        assertNotEquals(MapTap(listOf(6.86, 45.83), listOf(121.0, 340.0), marker = "hut"), tap)
        assertEquals(
            MapTap(listOf(Double.NaN, 0.0), listOf(0.0, 0.0)),
            MapTap(listOf(Double.NaN, 0.0), listOf(0.0, 0.0)),
        )
        assertNotEquals(MapTap(listOf(0.0, 0.0), listOf(0.0, 0.0)), MapTap(listOf(-0.0, 0.0), listOf(0.0, 0.0)))
    }
}
