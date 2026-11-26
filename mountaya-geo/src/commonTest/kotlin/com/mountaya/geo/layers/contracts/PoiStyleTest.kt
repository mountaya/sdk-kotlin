package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PoiStyleTest {
    private val hut = PoiStyle("hut", HexColor("#ff9800"))

    @Test
    fun twoStylesAreEqualWhereTheirGlyphAndColourAreAndHashAlike() {
        val copy = PoiStyle("hut", HexColor("#ff9800"))
        assertEquals(hut, copy)
        assertEquals(hut.hashCode(), copy.hashCode())
        assertNotEquals(hut, PoiStyle("campground", HexColor("#ff9800")))
        assertNotEquals(hut, PoiStyle("hut", HexColor("#ff9801")))
        assertNotEquals(hut, PoiStyle("hut", HexColor("#ff9800", 0.5)))
    }

    @Test
    fun twoStylesCompareTheirTextsByCodePointAndTheirOpacityAsTheSameNumber() {
        assertNotEquals(PoiStyle("café", hut.color), PoiStyle("café", hut.color))
        val unknown = PoiStyle("hut", HexColor("#ff9800", Double.NaN))
        assertEquals(unknown, PoiStyle("hut", HexColor("#ff9800", Double.NaN)))
        assertEquals(unknown.hashCode(), PoiStyle("hut", HexColor("#ff9800", Double.NaN)).hashCode())
        assertNotEquals(PoiStyle("hut", HexColor("#ff9800", 0.0)), PoiStyle("hut", HexColor("#ff9800", -0.0)))
    }
}
