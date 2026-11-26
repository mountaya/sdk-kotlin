package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConvertSpeedTest {
    @Test
    fun aRateConvertsToARateByTheRatioOfItsParts() {
        assertEquals(10.0, convertSpeed(36.0, SpeedUnit.KilometerPerHour, SpeedUnit.MeterPerSecond))
        assertEquals(1.609344, convertSpeed(1.0, SpeedUnit.MilePerHour, SpeedUnit.KilometerPerHour))
    }

    /** A pace is the reciprocal of a rate, in decimal minutes. */
    @Test
    fun aRateConvertsToAPaceAsItsReciprocalInDecimalMinutes() {
        assertEquals(13.333333333333334, convertSpeed(4.5, SpeedUnit.KilometerPerHour, SpeedUnit.MinutePerKilometer))
        assertEquals(21.45792, convertSpeed(4.5, SpeedUnit.KilometerPerHour, SpeedUnit.MinutePerMile), 1e-12)
        assertEquals(4.5, convertSpeed(13.333333333333334, SpeedUnit.MinutePerKilometer, SpeedUnit.KilometerPerHour))
    }

    @Test
    fun aPaceConvertsToAPaceByTheRatioOfItsParts() {
        assertEquals(1.609344, convertSpeed(1.0, SpeedUnit.MinutePerKilometer, SpeedUnit.MinutePerMile))
    }

    /** The division answers what it answers: no rate is refused. */
    @Test
    fun aRateOfZeroIsAPaceOfInfinityAndAPaceOfZeroARateOfInfinity() {
        assertEquals(
            Double.POSITIVE_INFINITY,
            convertSpeed(0.0, SpeedUnit.KilometerPerHour, SpeedUnit.MinutePerKilometer),
        )
        assertEquals(Double.POSITIVE_INFINITY, convertSpeed(0.0, SpeedUnit.MinutePerMile, SpeedUnit.MilePerHour))
        assertEquals(
            0.0,
            convertSpeed(Double.POSITIVE_INFINITY, SpeedUnit.KilometerPerHour, SpeedUnit.MinutePerKilometer),
        )
    }

    @Test
    fun aSpeedInItsOwnUnitIsReturnedAsItIs() {
        for (unit in SpeedUnit.entries) assertEquals(0.1, convertSpeed(0.1, unit, unit))
    }

    @Test
    fun theTwoMinuteUnitsAreThePaces() {
        assertTrue(SpeedUnit.MinutePerKilometer.isPace)
        assertTrue(SpeedUnit.MinutePerMile.isPace)
        assertFalse(SpeedUnit.KilometerPerHour.isPace)
        assertFalse(SpeedUnit.MeterPerSecond.isPace)
        assertFalse(SpeedUnit.MilePerHour.isPace)
    }
}
