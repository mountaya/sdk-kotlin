package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PlaceGlyphTest {
    private fun holding(vararg glyphs: String): GlyphSet =
        assertNotNull(
            GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = glyphs.associateWith { "<path d=\"M0 0h1\"/>" }),
            "the held set is a set",
        )

    @Test
    fun aPlaceTheSetDrawsIsMarkedWithItsOwnGlyph() {
        assertEquals("hut", placeGlyph("hut", holding("hut", "map-pin")))
        assertEquals("hut", placeGlyph("hut", holding("hut")))
        assertEquals("hut", placeGlyph("hut", GlyphSet.marker))
    }

    @Test
    fun aPlaceTheSetCannotDrawOrThatNamesNoneIsMarkedWithTheFallbackPin() {
        val set = holding("map-pin")

        assertEquals("map-pin", placeGlyph("hut", set))
        assertEquals("map-pin", placeGlyph(null, set))
        assertEquals("map-pin", placeGlyph("", set))
        assertEquals(WAYPOINT_FALLBACK_GLYPH, placeGlyph("activity-hiking", GlyphSet.marker))
    }

    @Test
    fun aPlaceWhoseSetHoldsNeitherItsGlyphNorThePinIsDrawnAsNothing() {
        assertNull(placeGlyph("hut", holding()))
        assertNull(placeGlyph(null, holding("hut")))
    }
}
