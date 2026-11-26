package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatSnowfallTest {
    private val imperial = RestSchema.UnitLength.Imperial

    @Test
    fun theMetricSystemWritesWholeCentimeters() {
        assertEquals("35 cm", formatSnowfall(35.0, UnitPreferences("en")))
        assertEquals("35厘米", formatSnowfall(35.0, UnitPreferences("zh")))
    }

    @Test
    fun theImperialSystemWritesInchesToOneDecimalATrailingZeroDropped() {
        assertEquals("13.8 in", formatSnowfall(35.0, UnitPreferences("en", length = imperial)))
        assertEquals("13,8 po", formatSnowfall(35.0, UnitPreferences("fr", length = imperial)))
        assertEquals("10 in", formatSnowfall(25.4, UnitPreferences("en", length = imperial)))
    }

    @Test
    fun noNumberIsNothing() {
        assertEquals("", formatSnowfall(null, UnitPreferences("en")))
        assertEquals("", formatSnowfall(Double.POSITIVE_INFINITY, UnitPreferences("en")))
    }
}
