package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class HighlightPartsTest {
    @Test
    fun theTitleIsSplitIntoMatchedAndUnmatchedRuns() {
        assertEquals(
            listOf(HighlightPart("Col du ", false), HighlightPart("Gali", true), HighlightPart("bier", false)),
            highlightParts("Col du Galibier", "gali"),
        )
        assertEquals(
            listOf(HighlightPart("İ", false), HighlightPart("stan", true), HighlightPart("bul", false)),
            highlightParts("İstanbul", "stan"),
        )
        assertEquals(listOf(HighlightPart("Straß", true), HighlightPart("e", false)), highlightParts("Straße", "stras"))
    }

    @Test
    fun aTitleWithNoMatchIsOneUnmatchedPart() {
        assertEquals(listOf(HighlightPart("Chamonix", false)), highlightParts("Chamonix", "zermatt"))
        assertEquals(listOf(HighlightPart("Chamonix", false)), highlightParts("Chamonix", ""))
        assertEquals(listOf(HighlightPart("", false)), highlightParts("", "mont"))
    }
}
