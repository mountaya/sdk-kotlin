package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchQueryChangeOfTest {
    @Test
    fun theTrimmedQueryIsTheRoundsIdentity() {
        assertEquals(SearchQueryChange.Ask("mont"), searchQueryChangeOf("", " mont "))
        assertEquals(SearchQueryChange.Unchanged, searchQueryChangeOf("mont", " mont "))
        assertEquals(SearchQueryChange.Unchanged, searchQueryChangeOf("mont", "mont"))
    }

    @Test
    fun aQueryThatTrimsToNothingClearsTheSearchAndAsksNothing() {
        assertEquals(SearchQueryChange.Clear, searchQueryChangeOf("mont", ""))
        assertEquals(SearchQueryChange.Clear, searchQueryChangeOf("mont", "　\t"))
        // An empty box before any query, or after a cleared one, asks nothing at all.
        assertEquals(SearchQueryChange.Unchanged, searchQueryChangeOf("", "  "))
    }

    @Test
    fun letterCaseIsPartOfTheQuery() {
        assertEquals(SearchQueryChange.Ask("Mont Blanc"), searchQueryChangeOf("mont blanc", "Mont Blanc"))
    }

    /** Only the ECMAScript set is trimmed: a zero width space is part of the query. */
    @Test
    fun theWhiteSpaceTrimmedIsTheOneSet() {
        assertEquals(SearchQueryChange.Ask("​mont"), searchQueryChangeOf("mont", "​mont"))
    }
}
