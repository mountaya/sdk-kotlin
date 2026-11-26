package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class WrapLongitudeTest {
    @Test
    fun aLongitudeIsBroughtIntoTheRangeByWholeTurns() {
        assertEquals(-170.0, wrapLongitude(190.0))
        assertEquals(170.0, wrapLongitude(-190.0))
        assertEquals(-173.5, wrapLongitude(906.5))
        assertEquals(6.5, wrapLongitude(6.5))
    }

    /** The range is [-180, 180): the antimeridian is written -180 whichever side it came from. */
    @Test
    fun theAntimeridianIsWrittenMinusOneHundredAndEighty() {
        assertEquals(-180.0, wrapLongitude(180.0))
        assertEquals(-180.0, wrapLongitude(-180.0))
        assertEquals(-180.0, wrapLongitude(540.0))
    }

    /**
     * The second remainder is what keeps a hair under -180 in the range: a turn added to it rounds
     * to a whole turn, and a whole turn's remainder is zero.
     */
    @Test
    fun aLongitudeAHairUnderTheRangeLandsOnItsWestEdge() {
        assertEquals(-180.0, wrapLongitude(-180.00000000000003))
        assertEquals(179.9999999999, wrapLongitude(-180.0000000001), 1e-9)
    }

    @Test
    fun aLongitudeThatIsNotFiniteNamesNoMeridian() {
        assertEquals(Double.NaN, wrapLongitude(Double.NaN))
        assertEquals(Double.NaN, wrapLongitude(Double.POSITIVE_INFINITY))
        assertEquals(Double.NaN, wrapLongitude(Double.NEGATIVE_INFINITY))
    }
}
