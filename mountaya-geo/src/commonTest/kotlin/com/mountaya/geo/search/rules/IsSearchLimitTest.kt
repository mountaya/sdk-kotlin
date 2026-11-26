package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsSearchLimitTest {
    @Test
    fun aCapIsAWholeNumberFromOneToTheCeiling() {
        assertTrue(isSearchLimit(1))
        assertTrue(isSearchLimit(SEARCH_LIMIT_MAX))
        assertFalse(isSearchLimit(0))
        assertFalse(isSearchLimit(51))
        assertFalse(isSearchLimit(-1))
    }
}
