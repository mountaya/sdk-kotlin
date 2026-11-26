package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SlopeAspectFromNeighboursTest {
    private val spacing = 0.0001

    @Test
    fun flatGroundHasNoSlopeAndFacesNowhere() {
        assertEquals(
            SlopeAspect(0.0, null),
            slopeAspectFromNeighbours(TerrainNeighbours(1200.0, 1200.0, 1200.0, 1200.0), 45.9, spacing),
        )
    }

    @Test
    fun theAspectIsTheCompassBearingOfTheDescent() {
        fun aspect(neighbours: TerrainNeighbours) = slopeAspectFromNeighbours(neighbours, 45.9, spacing).aspect

        assertEquals(0.0, aspect(TerrainNeighbours(1199.0, 1201.0, 1200.0, 1200.0)))
        assertEquals(90.0, aspect(TerrainNeighbours(1200.0, 1200.0, 1199.0, 1201.0)))
        assertEquals(180.0, aspect(TerrainNeighbours(1201.0, 1199.0, 1200.0, 1200.0)))
        assertEquals(270.0, aspect(TerrainNeighbours(1200.0, 1200.0, 1201.0, 1199.0)))
    }

    /** A bearing of zero is +0.0, a north-facing slope, never -0.0 and never 360. */
    @Test
    fun aNorthFacingSlopeReadsZeroAsAPositiveZero() {
        val aspect = slopeAspectFromNeighbours(TerrainNeighbours(1199.0, 1201.0, 1200.0, 1200.0), 45.9, spacing).aspect

        assertNotNull(aspect, "a slope states its aspect")
        assertEquals(1.0 / Double.POSITIVE_INFINITY, aspect)
        assertTrue(1.0 / aspect > 0.0)
    }

    @Test
    fun aBearingJustWestOfNorthStaysUnderATurn() {
        val just = slopeAspectFromNeighbours(TerrainNeighbours(1150.0, 1250.0, 1200.01, 1199.99), 45.9, spacing)

        assertEquals(359.9835336328873, assertNotNull(just.aspect, "a slope states its aspect"), 1e-9)
        assertEquals(77.462040543526, just.slope, 1e-9)
    }

    /**
     * The east-west span shrinks by the cosine of the latitude, so the same fall eastward is steeper
     * nearer a pole.
     */
    @Test
    fun theEastWestSpanShrinksWithTheLatitude() {
        val fallsEast = TerrainNeighbours(1200.0, 1200.0, 1199.0, 1201.0)

        assertEquals(7.36347066874295, slopeAspectFromNeighbours(fallsEast, 45.9, spacing).slope, 1e-9)
        assertEquals(10.196443687446688, slopeAspectFromNeighbours(fallsEast, 60.0, spacing).slope, 1e-9)
    }

    @Test
    fun theSlopeIsTheGradientsMagnitudeAsAnAngle() {
        assertEquals(
            5.1389088537818175,
            slopeAspectFromNeighbours(TerrainNeighbours(1199.0, 1201.0, 1200.0, 1200.0), 45.9, spacing).slope,
            1e-9,
        )
        assertEquals(
            86.81777613564847,
            slopeAspectFromNeighbours(TerrainNeighbours(1000.0, 1400.0, 1200.0, 1200.0), 45.9, spacing).slope,
            1e-9,
        )
    }

    @Test
    fun aNeighbourThatIsNotANumberLeavesBothNotANumber() {
        val unknown = slopeAspectFromNeighbours(TerrainNeighbours(Double.NaN, 1200.0, 1200.0, 1200.0), 45.9, spacing)

        assertEquals(Double.NaN, unknown.slope)
        assertEquals(Double.NaN, unknown.aspect)
    }

    @Test
    fun aSpacingOfZeroOnFlatGroundIsNotANumberRatherThanFlat() {
        val none = slopeAspectFromNeighbours(TerrainNeighbours(1200.0, 1200.0, 1200.0, 1200.0), 45.9, 0.0)

        assertEquals(Double.NaN, none.slope)
        assertEquals(Double.NaN, none.aspect)
        assertNull(slopeAspectFromNeighbours(TerrainNeighbours(1.0, 1.0, 1.0, 1.0), 89.0, spacing).aspect)
    }
}
