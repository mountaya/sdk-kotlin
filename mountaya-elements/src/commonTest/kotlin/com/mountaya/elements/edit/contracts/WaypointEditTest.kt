package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WaypointEditTest {
    @Test
    fun anEditIsTheWaypointWhereItWasPlacedAndWhetherTheMagnetPlacedIt() {
        val edit = WaypointEdit("w1", listOf(6.86, 45.83), isSnapped = true)

        assertEquals(WaypointEdit("w1", listOf(6.86, 45.83), isSnapped = true), edit)
        assertEquals(WaypointEdit("w1", listOf(6.86, 45.83), isSnapped = true).hashCode(), edit.hashCode())
        assertNotEquals(WaypointEdit("w1", listOf(6.86, 45.83)), edit)
        assertNotEquals(WaypointEdit("w1", isSnapped = true), edit, "a place against none")
        assertEquals(WaypointEdit("w1"), WaypointEdit("w1"))
        assertNotEquals(WaypointEdit("café"), WaypointEdit("café"))
    }
}
