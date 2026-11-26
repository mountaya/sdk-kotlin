package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BadgeFallbackTest {
    @Test
    fun aBadgeFillingATableCellIsWrittenAsTheAmenitysNameAsGiven() {
        assertEquals("Drop bag", badgeFallback("Drop bag", BadgeSurface.Cell))
        assertEquals(" eau ", badgeFallback(" eau ", BadgeSurface.Cell))
        assertEquals("", badgeFallback("", BadgeSurface.Cell))
    }

    @Test
    fun aBadgeBesideALabelOfItsOwnIsDrawnAsNothing() {
        assertNull(badgeFallback("Drop bag", BadgeSurface.Labelled))
    }

    @Test
    fun aBadgeInAProfilesStackIsTheNamesFirstLetterInCapitals() {
        assertEquals("D", badgeFallback("Drop bag", BadgeSurface.Stack))
        assertEquals("E", badgeFallback("eau potable", BadgeSurface.Stack))
        assertEquals("SS", badgeFallback("ß", BadgeSurface.Stack))
        assertEquals("Ǆ", badgeFallback("ǆ", BadgeSurface.Stack))
        assertEquals("", badgeFallback("", BadgeSurface.Stack))
    }

    /** The first character a reader sees is an extended grapheme cluster, never a code point or a code unit. */
    @Test
    fun theFirstLetterIsTheFirstGraphemeClusterItsMarksAndAnEmojiKeptWhole() {
        assertEquals("É", badgeFallback("étape", BadgeSurface.Stack))
        assertEquals("🚰", badgeFallback("🚰 Water", BadgeSurface.Stack))
        assertEquals("🇫🇷", badgeFallback("🇫🇷 Refuge", BadgeSurface.Stack))
        val family = "👨‍👩‍👧"
        assertEquals(family, badgeFallback("$family Famille", BadgeSurface.Stack))
        assertEquals("\r\n", badgeFallback("\r\nhut", BadgeSurface.Stack))
        assertEquals("각", badgeFallback("각", BadgeSurface.Stack))
    }
}
