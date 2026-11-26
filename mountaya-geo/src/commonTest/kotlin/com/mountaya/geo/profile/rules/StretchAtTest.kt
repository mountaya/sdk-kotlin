package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StretchAtTest {
    private val gap =
        listOf(
            ProfileClass(0.0, 500.0, "#2f9e44", "Path"),
            ProfileClass(800.0, 1_200.0, "#e8590c", "Trail"),
        )

    @Test
    fun aDistanceInsideOrAtTheStartOfAStretchReadsIt() {
        assertEquals("Path", stretchAt(gap, 250.0)?.label)
        assertEquals("Trail", stretchAt(gap, 800.0)?.label)
    }

    @Test
    fun theLastMeterBeforeAGapAndOfTheItineraryKeepsItsStretchAGapReadsNone() {
        assertEquals("Path", stretchAt(gap, 500.0)?.label)
        assertEquals("Trail", stretchAt(gap, 1_200.0)?.label)
        assertNull(stretchAt(gap, 600.0))
        assertNull(stretchAt(gap, -10.0))
        assertNull(stretchAt(gap, 1_300.0))
        assertNull(stretchAt(gap, Double.NaN))
    }

    @Test
    fun aBoundaryTwoStretchesShareReadsTheLaterWhateverTheOrderGiven() {
        val touching =
            listOf(
                ProfileRange(500.0, 1_000.0),
                ProfileRange(0.0, 500.0),
                ProfileRange(500.0, 500.0),
            )

        assertEquals(ProfileRange(500.0, 1_000.0), stretchAt(touching, 500.0))
    }

    @Test
    fun theEarlierOfTwoOverlappingStretchesHoldsTheSharedGround() {
        val overlapping =
            listOf(
                ProfileClass(0.0, 600.0, "#f00", "Path"),
                ProfileClass(400.0, 1_000.0, "#00f", "Trail"),
            )

        assertEquals("Path", stretchAt(overlapping, 500.0)?.label)
        assertEquals("Trail", stretchAt(overlapping, 600.0)?.label)
        assertEquals(600.0, stretchAt(overlapping, 600.0)?.from)
        assertNull(stretchAt(emptyList<ProfileRange>(), 0.0))
    }
}
