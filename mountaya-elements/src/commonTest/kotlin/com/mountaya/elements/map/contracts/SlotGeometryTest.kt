package com.mountaya.elements

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SlotGeometryTest {
    private val line = GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)))

    @Test
    fun twoSlotsHoldingsAreOneWhereTheGeometryIsTheSameAndTheColourAsWritten() {
        val held = SlotGeometry(line, "#2b8a3e")
        assertEquals(SlotGeometry(line, "#2b8a3e"), held)
        assertEquals(SlotGeometry(line, "#2b8a3e").hashCode(), held.hashCode())
        assertNotEquals(SlotGeometry(null, "#2b8a3e"), held)
        assertNotEquals(SlotGeometry(line, "#2B8A3E"), held)
        assertNotEquals(
            SlotGeometry(GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.85))), "#2b8a3e"),
            held,
        )
        assertNotEquals(SlotGeometry(null, "café"), SlotGeometry(null, "café"))
        assertEquals(SlotGeometry(null, "#f59f00"), SlotGeometry(null, "#f59f00"))
    }
}
