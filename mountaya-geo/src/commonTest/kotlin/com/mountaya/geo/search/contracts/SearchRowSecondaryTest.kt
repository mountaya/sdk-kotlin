package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchRowSecondaryTest {
    @Test
    fun twoSecondLinesAreEqualWhereTheyHoldTheSameValues() {
        assertEquals(SearchRowSecondary.Metric(Double.NaN, null), SearchRowSecondary.Metric(Double.NaN, null))
        assertNotEquals(SearchRowSecondary.Metric(0.0, 1), SearchRowSecondary.Metric(-0.0, 1))
        assertEquals(SearchRowSecondary.Count(7), SearchRowSecondary.Count(7))
        assertNotEquals<SearchRowSecondary>(SearchRowSecondary.Text("7"), SearchRowSecondary.Count(7))
    }
}
