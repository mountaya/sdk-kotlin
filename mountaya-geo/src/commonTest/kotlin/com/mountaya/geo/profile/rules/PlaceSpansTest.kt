package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceSpansTest {
    @Test
    fun aStretchTheWindowDoesNotReachOrOnlyTouchesIsLeftOut() {
        val window = ProfileRange(5_000.0, 10_000.0)
        val placed =
            placeSpans(listOf(ProfileRange(0.0, 4_000.0), ProfileRange(4_000.0, 10_000.0)), 10_000.0, window)

        assertEquals(listOf(1), placed.map { it.index })
        assertEquals(0.0, placed.firstOrNull()?.start)
        assertEquals(100.0, placed.firstOrNull()?.end)
        assertEquals(4_000.0, placed.firstOrNull()?.from)
        assertEquals(10_000.0, placed.firstOrNull()?.to)

        val touching =
            placeSpans(listOf(ProfileRange(0.0, 5_000.0), ProfileRange(6_000.0, 8_000.0)), 10_000.0, window)

        assertEquals(listOf(1), touching.map { it.index })
        assertEquals(20.0, touching.firstOrNull()?.start)
        assertEquals(60.0, touching.firstOrNull()?.end)
    }

    @Test
    fun aStretchOfNoLengthIsLeftOutOneGivenBackwardsReadInOrderTheOrderKept() {
        val placed =
            placeSpans(
                listOf(
                    ProfileRange(3_000.0, 3_000.0),
                    ProfileRange(7_500.0, 2_500.0),
                    ProfileRange(1_000.0, 2_000.0),
                ),
                10_000.0,
            )

        assertEquals(listOf(1, 2), placed.map { it.index })
        assertEquals(2_500.0, placed.firstOrNull()?.from)
        assertEquals(7_500.0, placed.firstOrNull()?.to)
        assertEquals(25.0, placed.firstOrNull()?.start)
        assertEquals(75.0, placed.firstOrNull()?.end)
    }

    @Test
    fun withNoWindowTheChartIsTheWholeItineraryAStretchPastItLeftOut() {
        val placed = placeSpans(listOf(ProfileRange(0.0, 2_500.0), ProfileRange(10_000.0, 12_000.0)), 10_000.0)

        assertEquals(listOf(0), placed.map { it.index })
    }

    @Test
    fun aWindowGivenBackwardsIsReadInOrderAndAStretchKeepsItsOwnMembers() {
        val placed =
            placeSpans(
                listOf(ProfileClass(4_000.0, 6_000.0, "#f00", "Path")),
                10_000.0,
                ProfileRange(8_000.0, 2_000.0),
            )

        assertEquals(33.333, placed.firstOrNull()?.start)
        assertEquals(66.667, placed.firstOrNull()?.end)
        assertEquals("Path", placed.firstOrNull()?.span?.label)
    }
}
