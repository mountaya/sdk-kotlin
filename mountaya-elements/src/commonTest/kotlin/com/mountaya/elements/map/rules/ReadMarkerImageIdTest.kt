package com.mountaya.elements

import com.mountaya.geo.GlyphSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReadMarkerImageIdTest {
    private val held = listOf("camping", "flag-start", "hut", "road-access-permit")

    private fun holding(glyphs: List<String>): GlyphSet =
        assertNotNull(
            GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = glyphs.associateWith { "<path d=\"M0 0h1\"/>" }),
            "the held set is a set",
        )

    @Test
    fun aWaypointsAndAPointOfInterestsIdsNameAGlyphAndTheColourAsWritten() {
        val set = holding(held)

        assertEquals(MarkerImage("flag-start", "#E03131"), readMarkerImageId("section-icon-flag-start-E03131", set))
        assertEquals(
            MarkerImage("road-access-permit", "#9c36b5"),
            readMarkerImageId("section-icon-road-access-permit-9c36b5", set),
        )
        assertEquals(MarkerImage("camping", "#0b7285"), readMarkerImageId("poi-icon-camping-0b7285", set))
        assertEquals(MarkerImage("hut", "#aBcDeF"), readMarkerImageId("section-icon-hut-aBcDeF", set))
        assertEquals(listOf("section-icon-", "poi-icon-"), markerImagePrefixes)
    }

    @Test
    fun anIdWhoseTailIsNoGlyphAHyphenAndSixHexadecimalDigitsIsLeftMissing() {
        val set = holding(held + listOf("hut-", "-hut"))

        for (id in listOf(
            "section-icon-hut1c7ed6",
            "section-icon--1c7ed6",
            "poi-icon-camping-nothex",
            "section-icon-hut-fff",
            "section-icon-hut-1c7ed6a",
            "section-icon-hut-1c7ed",
            "section-icon-",
            "section-icon-hut-1c7ed6\n",
            "section-icon-hut-1c7eg6",
            "section-icon-hut-１c7ed6",
        )) {
            assertNull(readMarkerImageId(id, set), id)
        }
        // The name is everything before the hyphen the digits follow, a hyphen of its own included.
        assertEquals("hut-", readMarkerImageId("section-icon-hut--1c7ed6", set)?.glyph)
        assertEquals("-hut", readMarkerImageId("section-icon--hut-1c7ed6", set)?.glyph)
    }

    @Test
    fun anIdUnderNoPrefixOrNamingAGlyphTheSetCannotDrawIsLeftMissing() {
        val set = holding(held)

        assertNull(readMarkerImageId("some-basemap-sprite", set))
        assertNull(readMarkerImageId("Section-icon-hut-1c7ed6", set))
        assertNull(readMarkerImageId("zone-clock-1c7ed6", set))
        assertNull(readMarkerImageId("section-icon-mountain-1c7ed6", set))
        assertNull(readMarkerImageId("section-icon-HUT-1c7ed6", set))
    }

    @Test
    fun aNameHoldingALineTerminatorNamesNoGlyph() {
        val set = holding(listOf("a\nb", "a b", "a\tb"))

        assertNull(readMarkerImageId("poi-icon-a\nb-000000", set))
        assertNull(readMarkerImageId("poi-icon-a b-000000", set))
        assertEquals("a\tb", readMarkerImageId("poi-icon-a\tb-000000", set)?.glyph)
    }

    @Test
    fun aPointOfInterestsSpriteIdNamesItsGlyphAndItsColoursSixSmallDigits() {
        assertEquals("poi-icon-mountain-5d4037", poiIconImageId("mountain", "#5D4037"))
        assertEquals("poi-icon-map-pin-1c7ed6", poiIconImageId("map-pin", "1c7ed6"))
        assertEquals("poi-icon-point-\u00C9", poiIconImageId("point", "#\u00C9"))
        assertEquals(
            MarkerImage("map-pin", "#1c7ed6"),
            readMarkerImageId(poiIconImageId("map-pin", "#1C7ED6"), holding(listOf("map-pin"))),
            "read back",
        )
    }
}
