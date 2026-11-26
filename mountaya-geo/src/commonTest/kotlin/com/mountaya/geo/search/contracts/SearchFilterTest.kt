package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchFilterTest {
    @Test
    fun everyTargetIsUnderAllAndUnderExactlyOneNarrowingFilter() {
        val narrowing = SearchFilter.entries - SearchFilter.All
        for (target in SearchFilter.All.targets) {
            assertEquals(1, narrowing.count { target in it.targets }, target.value)
        }
        assertEquals(listOf("all", "itineraries", "places", "zones"), SearchFilter.entries.map { it.id })
    }
}
