package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HideCursorMarkerTest {
    @Test
    fun anElementTakesDownTheMarkerItAddedAndNeverOneItDidNot() {
        val map = ShowCursorMarkerTest.Markers()
        map.addMarker(MapMarker("c", listOf(6.0, 45.0), glyph = "map-pin"))

        hideCursorMarker(map, null, "c")
        assertNotNull(map.held["c"], "the marker is held")
        hideCursorMarker(map, CursorMarkerPin(null, "pin-filled"), "c")
        assertNull(map.held["c"])
        assertEquals("remove", map.writes.last())
    }
}
