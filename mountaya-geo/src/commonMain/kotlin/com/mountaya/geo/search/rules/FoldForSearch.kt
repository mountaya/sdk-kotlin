package com.mountaya.geo

/**
 * The first code point that can belong to the one before it. Below it there is no combining mark and
 * nothing that composes with a letter before it, so a segment starts there without asking the
 * normalizer.
 */
private const val FIRST_JOINING_CODE_POINT = 0x300

/** The last code point of the basic multilingual plane. */
private const val LAST_BASIC_CODE_POINT = 0xFFFF

/** The block of combining diacritical marks a fold drops, the accents a decomposition leaves after a letter. */
private val accents = '̀'..'ͯ'

/**
 * The letters a fold reads as the plain letters a reader types for them, which neither lowercasing
 * nor a decomposition says: a sharp s stands for two letters, and a stroke is no combining mark, so
 * `ł` keeps its stroke where a reader typed `l`. Keyed small, since a segment is lowercased first.
 */
private val typedLetters = mapOf('ß' to "ss", 'đ' to "d", 'ħ' to "h", 'ł' to "l", 'ø' to "o", 'ŧ' to "t")

/**
 * FoldedText is a string folded for matching, [text], with, for each of its UTF-16 units, where the
 * segment it came from starts and ends in the string as written.
 */
internal class FoldedText(
    val text: String,
    val starts: IntArray,
    val ends: IntArray,
)

/**
 * Returns [segment], a code point with every code point joined to it, folded for matching:
 * lowercased, decomposed, stripped of the combining diacritical marks U+0300 to U+036F, recomposed,
 * a final sigma read as a sigma, and a sharp s and a Latin letter with a stroke read as the letters a
 * reader types. One ASCII character is only lowercased.
 */
internal fun foldSegment(segment: String): String {
    if (segment.length == 1 && segment[0].code < 0x80) return asciiLowercase(segment)
    val stripped = canonicalDecomposition(segment.lowercase()).filterNot { it in accents }
    return buildString {
        for (char in canonicalComposition(stripped)) {
            when (char) {
                'ς' -> append('σ')
                else -> typedLetters[char]?.let(::append) ?: append(char)
            }
        }
    }
}

/**
 * foldForSearch folds [value] one segment at a time, recording where each folded unit came from. A
 * segment is a code point and every code point after it that joins it: a combining mark of any kind,
 * and a letter that composes with the segment under canonical composition, as a Hangul vowel
 * completes the jamo before it. Every unit maps back to its whole segment, so no run of the title ends
 * inside one.
 */
internal fun foldForSearch(value: String): FoldedText {
    val text = StringBuilder(value.length)
    val starts = ArrayList<Int>(value.length)
    val ends = ArrayList<Int>(value.length)
    var start = 0

    fun close(end: Int) {
        val folded = foldSegment(value.substring(start, end))
        repeat(folded.length) {
            starts += start
            ends += end
        }
        text.append(folded)
        start = end
    }

    var index = 0
    while (index < value.length) {
        val codePoint = codePointAt(value, index)
        val width = if (codePoint > LAST_BASIC_CODE_POINT) 2 else 1
        val startsSegment =
            index > start &&
                (codePoint < FIRST_JOINING_CODE_POINT || !joinsSegment(value, start, index, width, codePoint))
        if (startsSegment) close(index)
        index += width
    }
    if (index > start) close(index)
    return FoldedText(text.toString(), starts.toIntArray(), ends.toIntArray())
}

/**
 * The code points that are no combining mark and can still join the segment before them under canonical
 * composition, as the first and the last of each range: those whose canonical decomposition begins with a
 * letter that composes with the one before it, the Hangul medial vowels and final consonants and Kirat
 * Rai's vowel signs E and AI, as Unicode 17 assigns them. The second letter of every other composition is a
 * combining mark, so any other code point that is none starts a segment of its own.
 */
private val composingLetters = intArrayOf(0x1161, 0x1175, 0x11A8, 0x11C2, 0x16D67, 0x16D68)

/**
 * Whether [codePoint] is a letter that may compose with the one before it, which only the composition of
 * the segment can then say: a lookup in a table of three ranges, which asks the normalizer nothing.
 */
internal fun composesWithLetterBefore(codePoint: Int): Boolean {
    var range = 0
    while (range < composingLetters.size) {
        if (codePoint >= composingLetters[range] && codePoint <= composingLetters[range + 1]) return true
        range += 2
    }
    return false
}

/**
 * Whether the code point [codePoint], [width] units at [index] of [value], belongs to the segment that
 * starts at [start] rather than starting one of its own: it is a combining mark, or composing the two
 * writes another text than composing each on its own. A table answers for every code point but the few
 * letters that compose with the one before them, so the normalizer is asked only for those.
 */
private fun joinsSegment(
    value: String,
    start: Int,
    index: Int,
    width: Int,
    codePoint: Int,
): Boolean {
    if (isCombiningMark(codePoint)) return true
    if (!composesWithLetterBefore(codePoint)) return false
    val segment = value.substring(start, index)
    val point = value.substring(index, index + width)
    return canonicalComposition(segment + point) != canonicalComposition(segment) + canonicalComposition(point)
}

/** The code point at [index] of [value]: a surrogate pair read as one, a lone surrogate as itself. */
private fun codePointAt(
    value: String,
    index: Int,
): Int {
    val high = value[index]
    if (high.isHighSurrogate() && index + 1 < value.length) {
        val low = value[index + 1]
        if (low.isLowSurrogate()) return ((high.code - 0xD800) shl 10) + (low.code - 0xDC00) + 0x10000
    }
    return high.code
}
