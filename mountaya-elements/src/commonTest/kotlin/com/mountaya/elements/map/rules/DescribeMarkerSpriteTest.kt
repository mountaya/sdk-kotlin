package com.mountaya.elements

import com.mountaya.geo.GlyphSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DescribeMarkerSpriteTest {
    private fun set(
        paint: GlyphSet.Paint,
        width: Double = 2.0,
        box: Double = 24.0,
        glyphs: List<String>,
    ): GlyphSet =
        assertNotNull(
            GlyphSet.of("held", paint, width, box, glyphs.associateWith { "<path d=\"M0 0h1\"/>" }),
            "the held set is a set",
        )

    private fun sprite(
        fill: String,
        scale: Double,
    ): MarkerSprite =
        MarkerSprite(
            48.0,
            MarkerSprite.Disc(listOf(24.0, 24.0), 21.0, fill, "#ffffff", 3.0),
            MarkerSprite.GlyphFit(12.0, scale, "#ffffff"),
        )

    @Test
    fun aSpriteIsA48PixelDiscOfRadius21UnderA3PixelWhiteBorderTheGlyphWhite() {
        assertEquals(
            sprite("#0b7285", 1.0),
            describeMarkerSprite("hut", "#0b7285", set(GlyphSet.Paint.Fill, glyphs = listOf("hut"))),
        )
        // The border, centred on the disc's edge, stands whole inside the image, a pixel and a half from its edge.
        val drawn = assertNotNull(describeMarkerSprite("hut", "#000000", GlyphSet.marker), "the hut has a sprite")
        assertEquals(1.5, drawn.size / 2 - (drawn.disc.radius + drawn.disc.borderWidth / 2))
    }

    @Test
    fun theGlyphFillsThe24PixelSquareAtTheCentreWhateverGridTheSetIsDrawnOn() {
        val grid48 = set(GlyphSet.Paint.Stroke, width = 3.0, box = 48.0, glyphs = listOf("tent"))
        val grid16 = set(GlyphSet.Paint.Stroke, box = 16.0, glyphs = listOf("flag-start"))

        assertEquals(sprite("#5f3dc5", 0.5), describeMarkerSprite("tent", "#5f3dc5", grid48))
        assertEquals(sprite("#e03131", 1.5), describeMarkerSprite("flag-start", "#e03131", grid16))
        for (set in listOf(grid48, grid16, GlyphSet.marker)) {
            val fit = assertNotNull(describeMarkerSprite(set.markup.keys.first(), "#000000", set), "a sprite").glyph
            assertEquals(24.0, fit.scale * set.viewBox)
            assertEquals(48.0, fit.offset * 2 + 24)
        }
    }

    @Test
    fun theDiscIsFilledWithTheColourAsGivenNeverReadOrRewritten() {
        val set = set(GlyphSet.Paint.Stroke, glyphs = listOf("hut"))

        assertEquals("#1C7ED6", describeMarkerSprite("hut", "#1C7ED6", set)?.disc?.fill)
        assertEquals("teal", describeMarkerSprite("hut", "teal", set)?.disc?.fill)
    }

    @Test
    fun aGlyphTheSetDoesNotHoldMakesNoSpriteOneItHoldsWithNoShapeMakesOne() {
        assertNull(describeMarkerSprite("tent", "#5f3dc5", set(GlyphSet.Paint.Stroke, glyphs = listOf("hut"))))
        assertNull(describeMarkerSprite("activity-hiking", "#5f3dc5", GlyphSet.marker))
        val empty =
            assertNotNull(
                GlyphSet.of("empty", GlyphSet.Paint.Stroke, markup = mapOf("blank" to "")),
                "a set of an empty glyph is a set",
            )
        assertNotNull(describeMarkerSprite("blank", "#5f3dc5", empty), "the sprite is described")
    }
}
