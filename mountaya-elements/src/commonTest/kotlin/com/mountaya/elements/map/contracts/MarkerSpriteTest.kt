package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MarkerSpriteTest {
    private fun sprite(
        size: Double = 48.0,
        fill: String = "#5f3dc5",
        scale: Double = 1.0,
    ): MarkerSprite =
        MarkerSprite(
            size,
            MarkerSprite.Disc(listOf(24.0, 24.0), 21.0, fill, "#ffffff", 3.0),
            MarkerSprite.GlyphFit(12.0, scale, "#ffffff"),
        )

    @Test
    fun twoSpritesAreEqualWhereEveryMemberIsAColourByItsCharacters() {
        assertEquals(sprite(), sprite())
        assertEquals(sprite().hashCode(), sprite().hashCode())
        assertNotEquals(sprite(fill = "#5F3DC5"), sprite())
        assertNotEquals(sprite(scale = 0.5), sprite())
        assertEquals(sprite(size = Double.NaN), sprite(size = Double.NaN))
        assertNotEquals(sprite(fill = "é"), sprite(fill = "é"))
    }
}
