package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchQueryChangeTest {
    @Test
    fun twoRoundsAreEqualWhereTheyAskTheSameQuery() {
        assertEquals(SearchQueryChange.Ask("mont"), SearchQueryChange.Ask("mont"))
        assertEquals(SearchQueryChange.Ask("mont").hashCode(), SearchQueryChange.Ask("mont").hashCode())
        assertNotEquals<SearchQueryChange>(SearchQueryChange.Ask("mont"), SearchQueryChange.Ask("Mont"))
        assertNotEquals<SearchQueryChange>(SearchQueryChange.Clear, SearchQueryChange.Unchanged)
    }
}
