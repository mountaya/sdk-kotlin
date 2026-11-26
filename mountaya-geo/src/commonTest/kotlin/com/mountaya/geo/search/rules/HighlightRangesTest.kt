package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class HighlightRangesTest {
    @Test
    fun aQueryMatchesWithoutRegardToCaseOrAccents() {
        assertEquals(listOf(0 until 4), highlightRanges("MONT blanc", "mont"))
        assertEquals(listOf(0 until 5), highlightRanges("Évian-les-Bains", "evian"))
        assertEquals(listOf(0 until 6), highlightRanges("Geneve", "GENÈVE"))
        assertEquals(listOf(0 until 6), highlightRanges("Évian", "evian"))
    }

    @Test
    fun everyMatchIsFoundLeftToRightAndNeverOverlapping() {
        assertEquals(listOf(4 until 6, 16 until 18), highlightRanges("Col de la Croix de Fer", "de"))
        assertEquals(listOf(0 until 2, 2 until 4), highlightRanges("aaaa", "aa"))
    }

    /** The emphasis never moves with a fold that changes a length, and a run takes a segment whole. */
    @Test
    fun aRunIsMadeOfWholeSegmentsOfTheTitleAsWritten() {
        assertEquals(listOf(1 until 5), highlightRanges("İstanbul", "stan"))
        assertEquals(listOf(0 until 5), highlightRanges("Straße", "stras"))
        assertEquals(listOf(7 until 13), highlightRanges("Bahnhofstraße", "strasse"))
        assertEquals(listOf(5 until 12), highlightRanges("Hauptstrasse", "straße"))
        // Both halves of a sharp s's fold are one run of the one letter.
        assertEquals(listOf(4 until 5), highlightRanges("Straße", "s").drop(1))
        // A consonant matched with the vowel sign on it, never alone.
        assertEquals(listOf(0 until 2), highlightRanges("किला", "क"))
    }

    @Test
    fun aStrokeLetterMatchesThePlainLetterOnEitherSide() {
        assertEquals(listOf(0 until 4), highlightRanges("Łódź", "lodz"))
        assertEquals(listOf(0 until 6), highlightRanges("Tromso", "tromsø"))
        assertEquals(listOf(0 until 7), highlightRanges("Đà Nẵng", "da nang"))
    }

    @Test
    fun noRunWhereEitherSideIsEmptyOrTheQueryFoldsToNothing() {
        assertEquals(emptyList(), highlightRanges("Chamonix", ""))
        assertEquals(emptyList(), highlightRanges("", "mont"))
        assertEquals(emptyList(), highlightRanges("Chamonix", "́"))
        assertEquals(emptyList(), highlightRanges("Chamonix", "zermatt"))
    }
}
