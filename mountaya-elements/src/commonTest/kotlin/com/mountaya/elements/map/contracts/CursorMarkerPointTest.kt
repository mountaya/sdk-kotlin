package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class CursorMarkerPointTest {
    @Test
    fun aPointIsDrawnWithTheFilledPinInTheMapsOwnColourUnlessItNamesOthers() {
        val point = CursorMarkerPoint("mtya-profile-cursor", listOf(6.8652, 45.9237))

        assertEquals("pin-filled", point.glyph)
        assertNull(point.color)
    }

    @Test
    fun twoPointsAreEqualWhereEveryMemberIsPositionsByTheSameNumberRelation() {
        val point = CursorMarkerPoint("a", listOf(6.0, 45.0), "#e8590c", "pin")

        assertEquals(CursorMarkerPoint("a", listOf(6.0, 45.0), "#e8590c", "pin"), point)
        assertEquals(CursorMarkerPoint("a", listOf(6.0, 45.0), "#e8590c", "pin").hashCode(), point.hashCode())
        assertNotEquals(CursorMarkerPoint("a", listOf(-0.0, 45.0)), CursorMarkerPoint("a", listOf(0.0, 45.0)))
        assertEquals(CursorMarkerPoint("a", listOf(Double.NaN, 45.0)), CursorMarkerPoint("a", listOf(Double.NaN, 45.0)))
    }
}
