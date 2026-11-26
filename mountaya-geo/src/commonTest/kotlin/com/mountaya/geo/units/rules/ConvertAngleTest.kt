package com.mountaya.geo

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertAngleTest {
    @Test
    fun anAngleConvertsThroughPiOverOneHundredAndEighty() {
        assertEquals(PI, convertAngle(180.0, AngleUnit.Degree, AngleUnit.Radian))
        assertEquals(57.29577951308232, convertAngle(1.0, AngleUnit.Radian, AngleUnit.Degree))
        assertEquals(0.017453292519943295, convertAngle(1.0, AngleUnit.Degree, AngleUnit.Radian))
    }

    /** An angle is not reduced to one turn: 720 degrees is four pi. */
    @Test
    fun nothingIsReducedToOneTurn() {
        assertEquals(4.0 * PI, convertAngle(720.0, AngleUnit.Degree, AngleUnit.Radian), 1e-15)
        assertEquals(-90.0, convertAngle(-PI / 2.0, AngleUnit.Radian, AngleUnit.Degree))
    }

    @Test
    fun theDeclaredConstantsAreTheFixturesOwn() {
        assertEquals(0.017453292519943295, RADIANS_PER_DEGREE)
        assertEquals(57.29577951308232, DEGREES_PER_RADIAN)
        assertEquals(360.0, DEGREES_PER_TURN)
    }

    @Test
    fun anAngleInItsOwnUnitIsReturnedAsItIs() {
        for (unit in AngleUnit.entries) assertEquals(0.1, convertAngle(0.1, unit, unit))
    }
}
