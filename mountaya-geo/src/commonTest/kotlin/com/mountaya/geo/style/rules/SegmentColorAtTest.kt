package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentColorAtTest {
    @Test
    fun aPositionReadsThePaletteWrapped() {
        assertEquals("#fa5252", segmentColorAt(0))
        assertEquals("#fd7e14", segmentColorAt(11))
        assertEquals("#fa5252", segmentColorAt(12))
        assertEquals("#e64980", segmentColorAt(37))
        assertEquals(12, livelyPalette.size)
        assertEquals("#868e96", DEFAULT_SEGMENT_COLOR)
    }

    @Test
    fun aPositionBeforeTheFirstReadsBackFromTheLast() {
        assertEquals("#fd7e14", segmentColorAt(-1))
        assertEquals("#fd7e14", segmentColorAt(-13))
        // -2147483644 is 8 past a whole number of turns of twelve.
        assertEquals("#40c057", segmentColorAt(Int.MIN_VALUE + 4))
    }

    /** A fraction reads the position it is past, below zero too, and a number that is none the first. */
    @Test
    fun aPositionThatIsNoWholeNumberReadsThePositionItIsPast() {
        assertEquals("#be4bdb", segmentColorAt(2.7))
        assertEquals("#fd7e14", segmentColorAt(-0.5))
        assertEquals("#fa5252", segmentColorAt(-0.0))
        assertEquals("#fa5252", segmentColorAt(Double.NaN))
        assertEquals("#fa5252", segmentColorAt(Double.POSITIVE_INFINITY))
        // 1e20 is 12 * 8333333333333333333 + 4, read exactly as the number it is, past any Int.
        assertEquals("#4c6ef5", segmentColorAt(1e20))
    }

    @Test
    fun aPaletteOfItsOwnIsReadAndOneThatListsNoColourReadsTheLivelyOne() {
        assertEquals("#ffffff", segmentColorAt(3, listOf("#000000", "#ffffff")))
        assertEquals("#7950f2", segmentColorAt(3, emptyList()))
        assertEquals("#7950f2", segmentColorAt(3.0, emptyList()))
    }
}
