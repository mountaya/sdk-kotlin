package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CursorMarkerPinTest {
    @Test
    fun twoPinsAreEqualWhereBothAreWrittenAlikeTheMapsOwnColourOnlyToItself() {
        assertEquals(CursorMarkerPin(null, "pin-filled"), CursorMarkerPin(null, "pin-filled"))
        assertNotEquals(CursorMarkerPin("#e8590c", "pin-filled"), CursorMarkerPin(null, "pin-filled"))
        assertNotEquals(CursorMarkerPin(null, "pin-filled"), CursorMarkerPin(null, "pin"))
        assertEquals(CursorMarkerPin("#fff", "pin").hashCode(), CursorMarkerPin("#fff", "pin").hashCode())
    }
}
