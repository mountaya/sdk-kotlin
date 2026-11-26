package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FoldForSearchTest {
    @Test
    fun aSegmentIsLowercasedAndStrippedOfItsAccents() {
        assertEquals("evian", foldForSearch("Évian").text)
        assertEquals("evian", foldForSearch("Évian").text)
        assertEquals("da nang", foldForSearch("Đà Nẵng").text)
        assertEquals("οδοσ", foldForSearch("ΟΔΟΣ").text)
    }

    @Test
    fun aSharpSAndAStrokeLetterReadAsTheLettersAReaderTypes() {
        assertEquals("strasse", foldForSearch("Straße").text)
        assertEquals("strasse", foldForSearch("STRAẞE").text)
        assertEquals("lodz", foldForSearch("Łódź").text)
        assertEquals("tromso", foldForSearch("Tromsø").text)
        assertEquals("hdt", foldForSearch("ĦĐŦ").text)
    }

    /** Every folded unit maps back to the whole segment it came from. */
    @Test
    fun eachFoldedUnitMapsBackToItsSegment() {
        val folded = foldForSearch("Straße")
        assertContentEquals(intArrayOf(0, 1, 2, 3, 4, 4, 5), folded.starts)
        assertContentEquals(intArrayOf(1, 2, 3, 4, 5, 5, 6), folded.ends)
        val accent = foldForSearch("Év")
        assertContentEquals(intArrayOf(0, 2), accent.starts)
        assertContentEquals(intArrayOf(2, 3), accent.ends)
    }

    /** A mark that is no accent is kept, and a letter that composes with the one before joins its segment. */
    @Test
    fun aMarkThatIsNoAccentIsKeptAndAComposingLetterJoins() {
        assertEquals("بَ", foldForSearch("بَ").text)
        val hangul = foldForSearch("가")
        assertEquals("가", hangul.text)
        assertContentEquals(intArrayOf(0), hangul.starts)
        assertContentEquals(intArrayOf(2), hangul.ends)
        // A code point beyond the basic plane is one code point, two units.
        assertContentEquals(intArrayOf(0, 0), foldForSearch("😀").starts)
    }

    @Test
    fun anAsciiCharacterIsOnlyLowercased() {
        assertEquals("a", foldSegment("A"))
        assertEquals("", foldForSearch("").text)
    }

    /**
     * Jamo written apart join into the syllable they compose, and a vowel after a letter it composes with
     * nothing starts a segment of its own.
     */
    @Test
    fun aHangulVowelAndFinalJoinTheJamoTheyComposeWith() {
        val composing = foldForSearch("\u1100\u1161\u11A8")
        assertContentEquals(intArrayOf(0), composing.starts.distinct().toIntArray())
        assertContentEquals(intArrayOf(3), composing.ends.distinct().toIntArray())
        val apart = foldForSearch("a\u1161")
        assertContentEquals(intArrayOf(0, 1), apart.starts)
    }

    @Test
    fun theTableNamesTheLettersThatComposeWithTheOneBefore() {
        assertTrue(composesWithLetterBefore(0x1161))
        assertTrue(composesWithLetterBefore(0x11C2))
        assertTrue(composesWithLetterBefore(0x16D68))
        assertFalse(composesWithLetterBefore(0x1160))
        assertFalse(composesWithLetterBefore(0x11C3))
        assertFalse(composesWithLetterBefore('a'.code))
        assertFalse(composesWithLetterBefore(0x0301), "a mark joins as a mark, not as a letter")
    }

    /**
     * The join test asks the normalizer only for the letters its table names, so the table is held to the
     * platform's own normalizer here: every code point that is no mark and begins, decomposed, with a letter
     * that ends a composition this platform makes, is in it.
     */
    @Test
    fun everyLetterThisPlatformComposesWithTheOneBeforeIsInTheTable() {
        val composingSeconds = HashSet<Int>()
        val firsts = HashMap<Int, Int>()
        for (codePoint in 0x300..0x1FFFF) {
            if (codePoint in 0xD800..0xDFFF) continue
            val text = textOf(codePoint)
            val decomposed = canonicalDecomposition(text)
            val points = codePointsOf(decomposed)
            if (!isCombiningMark(codePoint)) firsts[codePoint] = points.first()
            if (points.size < 2 || canonicalComposition(decomposed) != text) continue
            if (!isCombiningMark(points.last())) composingSeconds += points.last()
        }
        val missing =
            firsts.filter { (codePoint, first) -> first in composingSeconds && !composesWithLetterBefore(codePoint) }
        assertTrue(
            missing.isEmpty(),
            "letters that compose with the one before, missing from the table: ${missing.keys}",
        )
    }

    /** The text of one code point, a surrogate pair beyond the basic plane. */
    private fun textOf(codePoint: Int): String =
        if (codePoint <= 0xFFFF) {
            codePoint.toChar().toString()
        } else {
            val offset = codePoint - 0x10000
            charArrayOf((0xD800 + (offset shr 10)).toChar(), (0xDC00 + (offset and 0x3FF)).toChar()).concatToString()
        }

    /** The code points of [text], a surrogate pair read as one. */
    private fun codePointsOf(text: String): List<Int> {
        val points = ArrayList<Int>(text.length)
        var index = 0
        while (index < text.length) {
            val high = text[index]
            val low = text.getOrNull(index + 1)
            if (high.isHighSurrogate() && low != null && low.isLowSurrogate()) {
                points += ((high.code - 0xD800) shl 10) + (low.code - 0xDC00) + 0x10000
                index += 2
            } else {
                points += high.code
                index += 1
            }
        }
        return points
    }
}
