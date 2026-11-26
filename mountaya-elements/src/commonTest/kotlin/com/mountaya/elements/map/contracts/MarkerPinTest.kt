package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MarkerPinTest {
    @Test
    fun twoPinsAreOneWhereEveryMemberIsAGlyphsCentreDifferingFromADots() {
        val hut = MarkerPin(28.0, "#123456", "#ffffff", 3.0, MarkerPin.Centre.Glyph("hut", 16.0, "#ffffff"))
        assertEquals(MarkerPin(28.0, "#123456", "#ffffff", 3.0, MarkerPin.Centre.Glyph("hut", 16.0, "#ffffff")), hut)
        assertNotEquals(MarkerPin(28.0, "#123456", "#ffffff", 3.0, MarkerPin.Centre.Dot(8.0, "#ffffff")), hut)
        assertNotEquals(MarkerPin(28.0, null, "#ffffff", 3.0, MarkerPin.Centre.Glyph("hut", 16.0, "#ffffff")), hut)
    }
}
