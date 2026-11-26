package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ClampRangeTest {
    @Test
    fun theNarrowestWindowIsAHundredMeters() {
        assertEquals(100.0, MIN_RANGE_SPAN_METERS)
    }

    @Test
    fun aWindowIsOrderedAndHeldInsideTheItinerary() {
        assertEquals(ProfileRange(200.0, 800.0), clampRange(ProfileRange(200.0, 800.0), 1_000.0))
        assertEquals(ProfileRange(200.0, 800.0), clampRange(ProfileRange(800.0, 200.0), 1_000.0))
        assertEquals(ProfileRange(0.0, 1_000.0), clampRange(ProfileRange(-200.0, 1_500.0), 1_000.0))
        assertEquals(
            ProfileRange(0.0, 1_000.0),
            clampRange(ProfileRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), 1_000.0),
        )
    }

    @Test
    fun aNarrowWindowWidensForwardFromItsStartAndBackFromTheItinerarysEnd() {
        assertEquals(ProfileRange(0.0, 100.0), clampRange(ProfileRange(0.0, 50.0), 1_000.0))
        assertEquals(ProfileRange(500.0, 600.0), clampRange(ProfileRange(500.0, 550.0), 1_000.0))
        assertEquals(ProfileRange(900.0, 1_000.0), clampRange(ProfileRange(980.0, 1_000.0), 1_000.0))
        assertEquals(ProfileRange(9_900.0, 10_000.0), clampRange(ProfileRange(10_000.0, 10_000.0), 10_000.0))
    }

    @Test
    fun anEndThatIsNoNumberReadsAsTheItinerarysEndOnThatSide() {
        assertEquals(ProfileRange(0.0, 500.0), clampRange(ProfileRange(Double.NaN, 500.0), 1_000.0))
        assertEquals(ProfileRange(200.0, 1_000.0), clampRange(ProfileRange(200.0, Double.NaN), 1_000.0))
    }

    @Test
    fun aShortItineraryIsItsOwnWidthAndOneOfNoLengthAWindowOfNoLength() {
        assertEquals(ProfileRange(0.0, 60.0), clampRange(ProfileRange(10.0, 20.0), 60.0))
        assertEquals(ProfileRange(0.0, 0.0), clampRange(ProfileRange(10.0, 20.0), 0.0))
        assertEquals(ProfileRange(0.0, 0.0), clampRange(ProfileRange(10.0, 20.0), Double.NaN))
        assertEquals(ProfileRange(0.0, 0.0), clampRange(ProfileRange(10.0, 20.0), -5.0))
    }
}
