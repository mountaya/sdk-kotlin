package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MarkerImageTest {
    @Test
    fun twoImagesAreEqualWhereTheGlyphAndTheColourAreWrittenWithTheSameCharacters() {
        assertEquals(MarkerImage("hut", "#1c7ed6"), MarkerImage("hut", "#1c7ed6"))
        assertEquals(MarkerImage("hut", "#1c7ed6").hashCode(), MarkerImage("hut", "#1c7ed6").hashCode())
        assertNotEquals(MarkerImage("hut", "#1C7ED6"), MarkerImage("hut", "#1c7ed6"))
        assertNotEquals(MarkerImage("é", "#000000"), MarkerImage("é", "#000000"))
    }
}
