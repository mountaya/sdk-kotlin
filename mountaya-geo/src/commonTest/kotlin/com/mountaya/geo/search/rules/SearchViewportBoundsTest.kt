package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchViewportBoundsTest {
    private fun assertBox(
        expected: List<Double>,
        actual: List<Double>,
    ) {
        assertEquals(4, actual.size)
        expected.zip(actual).forEach { (e, a) -> assertEquals(e, a, 1e-9) }
    }

    @Test
    fun theBoxIsTheCentreWidenedByTheZoomsSpan() {
        assertBox(listOf(-0.703125, -0.703125, 0.703125, 0.703125), searchViewportBounds(listOf(0.0, 0.0), 8.0))
        assertBox(
            listOf(6.821254687499959, 45.893130951161474, 6.909145312499959, 45.95426904883852),
            searchViewportBounds(listOf(6.8652, 45.9237), 12.0),
        )
    }

    @Test
    fun eachSpanIsCappedAtNinetyDegrees() {
        assertBox(
            listOf(-38.13480000000004, 14.620993989349468, 51.86519999999996, 77.22640601065052),
            searchViewportBounds(listOf(6.8652, 45.9237), 1.0),
        )
        // Below zoom 0 the span would pass a turn; it is still ninety degrees.
        assertBox(listOf(-45.0, -45.0, 45.0, 45.0), searchViewportBounds(listOf(0.0, 0.0), -3.0))
    }

    @Test
    fun theBoxNeverCrossesTheAntimeridianOrAPole() {
        assertBox(
            listOf(-162.67578125, 44.87570388611955, -162.32421875, 45.12429611388045),
            searchViewportBounds(listOf(197.5, 45.0), 10.0),
        )
        assertBox(
            listOf(177.08749999999998, -19.689607126146036, 180.0, -14.310392873853964),
            searchViewportBounds(listOf(179.9, -17.0), 6.0),
        )
        assertBox(listOf(17.1875, 89.759375, 22.8125, 90.0), searchViewportBounds(listOf(20.0, 89.9), 6.0))
        // A latitude past the pole is held at it first.
        assertBox(listOf(17.1875, 89.859375, 22.8125, 90.0), searchViewportBounds(listOf(20.0, 95.0), 6.0))
    }

    /** The altitude is no part of the place; a centre that names none, and a zoom that is no number, answer NaN. */
    @Test
    fun aCentreThatNamesNoPlaceAndAZoomThatIsNoNumberAnswerNaN() {
        assertBox(listOf(-0.703125, -0.703125, 0.703125, 0.703125), searchViewportBounds(listOf(0.0, 0.0, 1200.0), 8.0))
        assertEquals(List(4) { Double.NaN }, searchViewportBounds(listOf(6.0), 8.0))
        assertEquals(List(4) { Double.NaN }, searchViewportBounds(listOf(Double.NaN, 45.0), 8.0))
        assertEquals(List(4) { Double.NaN }, searchViewportBounds(listOf(6.0, 45.0), Double.NaN))
    }
}
