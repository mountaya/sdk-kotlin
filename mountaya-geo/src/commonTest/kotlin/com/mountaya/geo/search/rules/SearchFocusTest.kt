package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchFocusTest {
    @Test
    fun aCentreWithinRangeIsSentAsWrittenMinusZeroAnd180Included() {
        assertEquals(listOf(6.8652, 45.9237), searchFocus(listOf(6.8652, 45.9237)))
        assertEquals(listOf(180.0, 0.0), searchFocus(listOf(180.0, 0.0)))
        assertEquals(listOf(-180.0, 0.0), searchFocus(listOf(-180.0, 0.0)))
        assertEquals((-0.0).toBits(), searchFocus(listOf(-0.0, 0.0))?.get(0)?.toBits())
    }

    @Test
    fun aCentreCountedPastTheAntimeridianIsBroughtBackByWholeTurns() {
        assertEquals(listOf(-162.5, 21.3), searchFocus(listOf(197.5, 21.3)))
        assertEquals(listOf(170.0, 10.0), searchFocus(listOf(-190.0, 10.0)))
        assertEquals(listOf(6.5, 45.0), searchFocus(listOf(366.5, 45.0)))
    }

    @Test
    fun aLatitudePastAPoleIsHeldAtThePole() {
        assertEquals(listOf(6.5, 90.0), searchFocus(listOf(6.5, 95.0)))
        assertEquals(listOf(6.5, -90.0), searchFocus(listOf(6.5, -100.0)))
    }

    @Test
    fun aCentreThatNamesNoPointRanksAroundNothingAnAltitudeLeftOut() {
        assertNull(searchFocus(listOf(Double.NaN, 45.9237)))
        assertNull(searchFocus(listOf(6.5, Double.POSITIVE_INFINITY)))
        assertNull(searchFocus(listOf(6.5)))
        assertNull(searchFocus(emptyList()))
        assertEquals(listOf(6.5, 45.0), searchFocus(listOf(6.5, 45.0, 1200.0)))
    }
}
