package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchListStatusTest {
    @Test
    fun eachStatusReadsTheKeyOfItsWordInTheSearchSection() {
        assertEquals("error", SearchListStatus.Error.key)
        assertEquals("emptyTitle", SearchListStatus.EmptyTitle.key)
        assertEquals("resultCount", SearchListStatus.ResultCount(3).key)
        assertEquals<SearchListStatus>(SearchListStatus.ResultCount(3), SearchListStatus.ResultCount(3))
        assertNotEquals<SearchListStatus>(SearchListStatus.ResultCount(3), SearchListStatus.ResultCount(1))
        assertNotEquals("search.error", ElementsScope.standard.word("search.${SearchListStatus.Error.key}"))
    }
}
