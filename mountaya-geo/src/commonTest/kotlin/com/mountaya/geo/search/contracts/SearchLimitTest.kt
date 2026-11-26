package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchLimitTest {
    @Test
    fun aCapIsAWholeNumberFrom1ToTheCeilingAndAnyOtherIsNoCap() {
        assertEquals(1, SearchLimit.of(1)?.value)
        assertEquals(50, SearchLimit.of(50)?.value)
        assertEquals(50, SearchLimit.of(SEARCH_LIMIT_MAX)?.value)
        assertNull(SearchLimit.of(0))
        assertNull(SearchLimit.of(51))
        assertNull(SearchLimit.of(-1))
        assertNull(SearchLimit.of(Int.MAX_VALUE))
        assertEquals(SearchLimit.of(5), SearchLimit.of(5))
    }
}
