package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchNeedleOfTest {
    @Test
    fun aQueryIsFoldedOnceAsATitleIsFolded() {
        assertEquals(SearchNeedle("evian"), searchNeedleOf("Évian"))
        assertEquals(SearchNeedle("strasse"), searchNeedleOf("STRAẞE"))
        assertEquals(SearchNeedle(foldForSearch("Łódź").text), searchNeedleOf("Łódź"))
    }

    @Test
    fun anEmptyQueryAndOneThatFoldsToNothingAreTheEmptyNeedle() {
        assertEquals(SearchNeedle(""), searchNeedleOf(""))
        assertEquals(SearchNeedle(""), searchNeedleOf("\u0301"))
    }

    /** A round matches every title against one fold of its query, and finds what folding it per title found. */
    @Test
    fun aTitleMatchedAgainstTheNeedleIsMatchedAsAgainstTheQuery() {
        val titles =
            listOf("Évian-les-Bains", "Col de la Croix de Fer", "Bahnhofstraße", "İstanbul", "가나다", "", "Chamonix")
        for (query in listOf("evian", "de", "STRASSE", "stan", "가", "", "\u0301", "zermatt")) {
            val needle = searchNeedleOf(query)
            for (title in titles) {
                assertEquals(
                    highlightRanges(title, query),
                    highlightRanges(title, needle),
                    "$query in $title",
                )
            }
        }
    }
}
