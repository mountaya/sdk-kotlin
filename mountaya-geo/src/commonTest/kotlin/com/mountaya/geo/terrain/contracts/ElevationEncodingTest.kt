package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ElevationEncodingTest {
    @Test
    fun anEncodingIsHeldAsTheDocumentSpellsItAndComparedWhole() {
        assertEquals("mapbox", ElevationEncoding.Mapbox.value)
        assertEquals("terrarium", ElevationEncoding.Terrarium.value)
        assertEquals(ElevationEncoding.Mapbox, ElevationEncoding("mapbox"))
        assertNotEquals(ElevationEncoding.Mapbox, ElevationEncoding("Mapbox"))
        assertEquals("custom", ElevationEncoding("custom").value)
    }
}
