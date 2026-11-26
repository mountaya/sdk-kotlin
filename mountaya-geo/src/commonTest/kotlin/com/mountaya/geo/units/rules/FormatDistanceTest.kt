package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatDistanceTest {
    private val metric = UnitPreferences("en")
    private val imperial = UnitPreferences("en", length = RestSchema.UnitLength.Imperial)

    @Test
    fun aDistanceIsKilometersOrMilesToOneDecimal() {
        assertEquals("0.9 km", formatDistance(850.0, metric))
        assertEquals("24.3 km", formatDistance(24_300.0, metric))
        assertEquals("0.5 mi", formatDistance(850.0, imperial))
        assertEquals("15.1 mi", formatDistance(24_300.0, imperial))
        assertEquals("1,234.6 km", formatDistance(1_234_567.0, metric))
    }

    @Test
    fun theReadersLanguageWritesTheNumberAndTheUnit() {
        assertEquals("24,3 km", formatDistance(24_300.0, UnitPreferences("fr-FR")))
        assertEquals("24,3 km", formatDistance(24_300.0, UnitPreferences("de")))
        assertEquals("24.3公里", formatDistance(24_300.0, UnitPreferences("zh")))
    }

    @Test
    fun anUnknownSystemIsMetricAndAMissingDistanceIsNothing() {
        assertEquals("0.9 km", formatDistance(850.0, UnitPreferences("en", length = RestSchema.UnitLength("nautical"))))
        assertEquals("", formatDistance(null, metric))
        assertEquals("", formatDistance(Double.NaN, metric))
        assertEquals("0 km", formatDistance(-20.0, metric))
    }
}
