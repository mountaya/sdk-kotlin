package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MapMarkerTest {
    /**
     * A position compares as a `List<Double>` compares, which is Geo's relation on every target: `NaN`
     * the same as `NaN`, and 0 another number than -0.
     */
    @Test
    fun twoMarkersAreEqualWhereEveryMemberIsTheSameATextAsWritten() {
        val hut = MapMarker("hut", listOf(6.86, 45.83), color = "#e64980", label = "Refuge")

        assertEquals(MapMarker("hut", listOf(6.86, 45.83), color = "#e64980", label = "Refuge"), hut)
        assertEquals(
            MapMarker("hut", listOf(6.86, 45.83), color = "#e64980", label = "Refuge").hashCode(),
            hut.hashCode(),
        )
        assertNotEquals(MapMarker("hut", listOf(6.86, 45.84), color = "#e64980", label = "Refuge"), hut)
        assertNotEquals(MapMarker("hut", listOf(6.86, 45.83), label = "Refuge"), hut)
        assertNotEquals(MapMarker("café", listOf(0.0, 0.0)), MapMarker("café", listOf(0.0, 0.0)))
        assertEquals(MapMarker("a", listOf(Double.NaN, 0.0)), MapMarker("a", listOf(Double.NaN, 0.0)))
        assertEquals(
            MapMarker("a", listOf(Double.NaN, 0.0)).hashCode(),
            MapMarker("a", listOf(Double.NaN, 0.0)).hashCode(),
        )
        assertNotEquals(MapMarker("a", listOf(0.0, 0.0)), MapMarker("a", listOf(-0.0, 0.0)))
        assertNotEquals(
            MapMarker("a", listOf(0.0, 0.0), glyph = "hut"),
            MapMarker("a", listOf(0.0, 0.0), glyph = "mountain"),
        )
        assertNotEquals(MapMarker("a", listOf(0.0, 0.0), glyph = "hut"), MapMarker("a", listOf(0.0, 0.0)))
    }
}
