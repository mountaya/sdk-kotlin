package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatSpeedTest {
    private val perDistance = RestSchema.UnitPace.PerDistance
    private val imperial = RestSchema.UnitLength.Imperial

    @Test
    fun perTimeARateIsStatedToOneDecimal() {
        assertEquals("4.5 km/h", formatSpeed(4.5, UnitPreferences("en")))
        assertEquals("2.8 mph", formatSpeed(4.5, UnitPreferences("en", length = imperial)))
        assertEquals("2,8 mi/h", formatSpeed(4.5, UnitPreferences("fr", length = imperial)))
        assertEquals("0 km/h", formatSpeed(-0.04, UnitPreferences("en")))
    }

    @Test
    fun perDistanceARateIsThePaceClockOfItsReciprocal() {
        assertEquals("13:20 min/km", formatSpeed(4.5, UnitPreferences("en", pace = perDistance)))
        assertEquals("21:27 min/mi", formatSpeed(4.5, UnitPreferences("en", length = imperial, pace = perDistance)))
        assertEquals("13:20 min/km", formatSpeed(4.5, UnitPreferences("fr", pace = perDistance)))
        assertEquals("60:00 min/km", formatSpeed(1.0, UnitPreferences("en", pace = perDistance)))
    }

    @Test
    fun aRateWithNoReciprocalOrNoNumberIsNothing() {
        assertEquals("", formatSpeed(0.0, UnitPreferences("en", pace = perDistance)))
        assertEquals("", formatSpeed(-3.0, UnitPreferences("en", pace = perDistance)))
        assertEquals("", formatSpeed(1e-300, UnitPreferences("en", pace = perDistance)))
        assertEquals("", formatSpeed(null, UnitPreferences("en")))
        assertEquals("4.5 km/h", formatSpeed(4.5, UnitPreferences("en", pace = RestSchema.UnitPace("per-lap"))))
    }
}
