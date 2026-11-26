package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelectBoundaryTest {
    private fun boundary(
        name: String,
        vararg bbox: Double,
    ) = RestSchema.Boundaries(bbox = bbox.toList(), name = name)

    private val pyrenees = boundary("pyrenees", -1.0, 42.0, 3.0, 43.0)
    private val alps = boundary("alps", 5.0, 44.0, 8.0, 47.0)
    private val hawaii = boundary("hawaii", -161.0, 18.0, -154.0, 23.0)

    @Test
    fun theFirstBoundaryHoldingTheCenterIsTheLockElseTheFirst() {
        assertEquals(alps, selectBoundary(listOf(pyrenees, alps), listOf(6.88, 45.85)))
        assertEquals(pyrenees, selectBoundary(listOf(pyrenees, alps), listOf(0.0, 0.0)))
        assertEquals(alps, selectBoundary(listOf(alps, boundary("wider", 0.0, 40.0, 10.0, 50.0)), listOf(6.88, 45.85)))
    }

    @Test
    fun noBoundaryIsNoLock() {
        assertNull(selectBoundary(emptyList(), listOf(6.88, 45.85)))
        assertNull(selectBoundary(emptyList(), listOf(Double.NaN, 45.0)))
    }

    @Test
    fun aCenterIsReadAsAnAngleHoweverManyTurnsItIsWrittenAway() {
        assertEquals(hawaii, selectBoundary(listOf(alps, hawaii), listOf(202.5, 21.3)))
        assertEquals(alps, selectBoundary(listOf(hawaii, alps), listOf(-353.12, 45.85)))
        val alpsATurnOut = boundary("alps", 365.0, 44.0, 368.0, 47.0)
        assertEquals(alpsATurnOut, selectBoundary(listOf(hawaii, alpsATurnOut), listOf(6.88, 45.85)))
    }

    @Test
    fun aBoxAcrossTheAntimeridianHoldsACenterWrittenOnEitherSide() {
        val fiji = boundary("fiji", 177.0, -19.0, 182.0, -16.0)
        val fijiFromTheWest = boundary("fiji", -185.0, -19.0, -175.0, -16.0)

        assertEquals(fiji, selectBoundary(listOf(alps, fiji), listOf(-179.5, -17.0)))
        assertEquals(fiji, selectBoundary(listOf(alps, fiji), listOf(180.5, -17.0)))
        assertEquals(fijiFromTheWest, selectBoundary(listOf(alps, fijiFromTheWest), listOf(182.0, -17.0)))
        assertEquals(alps, selectBoundary(listOf(alps, fijiFromTheWest), listOf(170.0, -17.0)))
    }

    @Test
    fun aCenterOnAnEdgeIsInsideAndOneOutsideTheLatitudesIsNot() {
        val alpsToTheEdge = boundary("alps", 5.0, 44.0, 7.1, 47.0)
        val origin = boundary("origin", 0.0, 0.0, 1.0, 1.0)

        assertEquals(alpsToTheEdge, selectBoundary(listOf(pyrenees, alpsToTheEdge), listOf(7.1, 47.0)))
        assertEquals(alpsToTheEdge, selectBoundary(listOf(pyrenees, alpsToTheEdge), listOf(5.0, 44.0)))
        assertEquals(
            origin,
            selectBoundary(listOf(origin, boundary("fiji", 175.0, -19.0, 185.0, -16.0)), listOf(178.0, 40.0)),
        )
    }

    /** A box a turn or more wide holds every meridian between its latitudes. */
    @Test
    fun aBoxATurnWideHoldsEveryMeridian() {
        val band = boundary("band", -180.0, -10.0, 180.0, 10.0)

        assertEquals(band, selectBoundary(listOf(alps, band), listOf(-179.0, 0.0)))
        assertEquals(band, selectBoundary(listOf(alps, band), listOf(900.0, 0.0)))
    }

    @Test
    fun aCenterThatNamesNoPlaceIsHeldByNoBox() {
        assertEquals(hawaii, selectBoundary(listOf(hawaii, alps), listOf(Double.NaN, 45.0)))
        assertEquals(hawaii, selectBoundary(listOf(hawaii, alps), listOf(Double.POSITIVE_INFINITY, 45.0)))
        assertEquals(hawaii, selectBoundary(listOf(hawaii, alps), listOf(6.88)))
    }

    /** Such a box is not one the wire writes, and nothing is guessed about it. */
    @Test
    fun aBoxThatIsNotFourNumbersOrIsDrawnBackwardsHoldsNothing() {
        val short = boundary("short", 5.0, 44.0, 8.0)
        val backwards = boundary("backwards", 8.0, 44.0, 5.0, 47.0)
        val upsideDown = boundary("upside-down", 5.0, 47.0, 8.0, 44.0)

        assertEquals(alps, selectBoundary(listOf(short, alps), listOf(6.88, 45.85)))
        assertEquals(alps, selectBoundary(listOf(backwards, alps), listOf(6.88, 45.85)))
        assertEquals(alps, selectBoundary(listOf(upsideDown, alps), listOf(6.88, 45.85)))
        assertEquals(short, selectBoundary(listOf(short, backwards, upsideDown), listOf(6.88, 45.85)))
    }
}
