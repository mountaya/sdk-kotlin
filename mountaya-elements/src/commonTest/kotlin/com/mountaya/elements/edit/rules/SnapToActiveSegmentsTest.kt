package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class SnapToActiveSegmentsTest {
    /** Reads a longitude as an x and a latitude as a y, one degree one pixel. */
    private val identity = ScreenProjection(point = { it }, position = { it })

    /** Draws a degree as a hundred pixels, as a map zoomed in does. */
    private val hundredfold =
        ScreenProjection(point = { p ->
            p.map { it * 100 }
        }, position = { p -> p.map { it / 100 } })

    private val line = listOf(listOf(0.0, 10.0), listOf(100.0, 10.0))

    @Test
    fun aPointerWithinTheMagnetsRadiusIsPlacedOnTheNearestPointOfTheLine() {
        val snap = snapToActiveSegments(listOf(50.0, 40.0), listOf(line), identity)

        assertEquals(listOf(50.0, 10.0), snap.position)
        assertTrue(snap.isSnapped)
    }

    @Test
    fun theRadiusItselfIsWithinTheMagnetAndTheNextPixelOutIsNot() {
        assertTrue(snapToActiveSegments(listOf(50.0, 50.0), listOf(line), identity).isSnapped)
        val past = snapToActiveSegments(listOf(50.0, 51.0), listOf(line), identity)

        assertFalse(past.isSnapped)
        assertEquals(listOf(50.0, 51.0), past.position)
        assertEquals(40.0, SNAP_THRESHOLD_PX)
    }

    @Test
    fun beyondAnEdgesEndTheEndIsNearest() {
        val snap = snapToActiveSegments(listOf(120.0, 10.0), listOf(line), identity)

        assertEquals(listOf(100.0, 10.0), snap.position)
        assertTrue(snap.isSnapped)
    }

    @Test
    fun theNearestOfTwoLinesTakesThePointAndOfTwoAtOneDistanceTheFirst() {
        val lower = listOf(listOf(0.0, 10.0), listOf(100.0, 10.0))
        val upper = listOf(listOf(0.0, 30.0), listOf(100.0, 30.0))

        assertEquals(
            listOf(50.0, 30.0),
            snapToActiveSegments(listOf(50.0, 25.0), listOf(lower, upper), identity).position,
        )
        assertEquals(
            listOf(50.0, 30.0),
            snapToActiveSegments(listOf(50.0, 20.0), listOf(upper, lower), identity).position,
        )
        assertEquals(
            listOf(50.0, 10.0),
            snapToActiveSegments(listOf(50.0, 20.0), listOf(lower, upper), identity).position,
        )
    }

    @Test
    fun everyEdgeOfALineIsReadNotItsFirstAlone() {
        val bent = listOf(listOf(0.0, 0.0), listOf(10.0, 0.0), listOf(10.0, 100.0))

        assertEquals(listOf(10.0, 60.0), snapToActiveSegments(listOf(15.0, 60.0), listOf(bent), identity).position)
    }

    @Test
    fun aLineOfOnePositionHasNoEdgeAndNoLineLeavesThePointerUnread() {
        val alone = snapToActiveSegments(listOf(0.0, 15.0), listOf(listOf(listOf(0.0, 10.0))), identity)
        assertEquals(listOf(0.0, 15.0), alone.position)
        assertFalse(alone.isSnapped)

        val untouched = ScreenProjection(point = { fail("the projection was read for no line") }, position = { it })
        assertEquals(listOf(50.0, 12.0), snapToActiveSegments(listOf(50.0, 12.0), emptyList(), untouched).position)
    }

    @Test
    fun anEdgeOfNoLengthIsItsStart() {
        val snap = snapToActiveSegments(listOf(3.0, 4.0), listOf(listOf(listOf(0.0, 0.0), listOf(0.0, 0.0))), identity)

        assertEquals(listOf(0.0, 0.0), snap.position)
        assertTrue(snap.isSnapped)
    }

    @Test
    fun distancesArePixelsAHundredfoldProjectionNarrowsTheMagnetOnTheGround() {
        val short = listOf(listOf(10.0, 10.0), listOf(11.0, 10.0))

        assertTrue(snapToActiveSegments(listOf(10.5, 10.3), listOf(short), hundredfold).isSnapped)
        assertFalse(snapToActiveSegments(listOf(10.5, 10.5), listOf(short), hundredfold).isSnapped)
    }

    @Test
    fun aPointTheMagnetLeavesIsThePointerAsGivenOneItMovesIsReadBack() {
        assertEquals(
            listOf(50.0, 90.0, 1200.0),
            snapToActiveSegments(listOf(50.0, 90.0, 1200.0), listOf(line), identity).position,
        )
        assertEquals(
            listOf(50.0, 10.0),
            snapToActiveSegments(listOf(50.0, 20.0, 1200.0), listOf(line), identity).position,
        )
    }

    @Test
    fun aPointerMissingACoordinateReadsItAsZeroAndOneThatNamesNoPlaceIsLeft() {
        assertEquals(listOf(50.0, 10.0), snapToActiveSegments(listOf(50.0), listOf(line), identity).position)
        val nowhere = snapToActiveSegments(listOf(Double.NaN, 10.0), listOf(line), identity)

        assertFalse(nowhere.isSnapped)
        assertEquals(2, nowhere.position.size)
    }

    @Test
    fun aRadiusOfTheCallersOwnTakesTheMagnetsPlace() {
        assertFalse(snapToActiveSegments(listOf(50.0, 20.0), listOf(line), identity, thresholdPx = 5.0).isSnapped)
    }
}
