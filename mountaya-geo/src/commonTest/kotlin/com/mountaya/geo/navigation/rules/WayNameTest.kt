package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WayNameTest {
    @Test
    fun anEmptyNameAndTheApisDashAreNoName() {
        assertNull(wayName(""))
        assertNull(wayName("-"))
        assertNull(wayName(null))
    }

    /** Kept as written: nothing is trimmed, and a dash that is part of a name is a name. */
    @Test
    fun anyOtherNameIsKeptAsWritten() {
        assertEquals("Route des Pèlerins", wayName("Route des Pèlerins"))
        assertEquals(" ", wayName(" "))
        assertEquals(" - ", wayName(" - "))
        assertEquals("--", wayName("--"))
    }
}
