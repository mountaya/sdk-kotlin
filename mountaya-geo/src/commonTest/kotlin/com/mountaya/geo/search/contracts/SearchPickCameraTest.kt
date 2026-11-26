package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchPickCameraTest {
    @Test
    fun twoMovesAreEqualWhereTheyAreOfOneKindHoldingTheSameNumbers() {
        assertEquals(
            SearchPickCamera.Fit(listOf(6.0, 45.0, 7.0, Double.NaN)),
            SearchPickCamera.Fit(listOf(6.0, 45.0, 7.0, Double.NaN)),
        )
        assertNotEquals(
            SearchPickCamera.Fit(listOf(0.0, 45.0, 7.0, 46.0)),
            SearchPickCamera.Fit(listOf(-0.0, 45.0, 7.0, 46.0)),
        )
        assertEquals(SearchPickCamera.Fly(listOf(6.0, 45.0), 13.0), SearchPickCamera.Fly(listOf(6.0, 45.0), 13.0))
        assertNotEquals(SearchPickCamera.Fly(listOf(6.0, 45.0), 13.0), SearchPickCamera.Fly(listOf(6.0, 45.0), 12.0))
        assertNotEquals<SearchPickCamera>(
            SearchPickCamera.Fly(listOf(6.0, 45.0, 7.0, 46.0), 13.0),
            SearchPickCamera.Fit(listOf(6.0, 45.0, 7.0, 46.0)),
        )
        assertEquals(
            SearchPickCamera.Fly(listOf(6.0, 45.0), Double.NaN),
            SearchPickCamera.Fly(listOf(6.0, 45.0), Double.NaN),
        )
        assertEquals(
            SearchPickCamera.Fly(listOf(6.0, 45.0), Double.NaN).hashCode(),
            SearchPickCamera.Fly(listOf(6.0, 45.0), Double.NaN).hashCode(),
        )
        assertNotEquals(SearchPickCamera.Fly(listOf(6.0, 45.0), 0.0), SearchPickCamera.Fly(listOf(6.0, 45.0), -0.0))
    }
}
