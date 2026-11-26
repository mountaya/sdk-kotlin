package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatHumidityTest {
    @Test
    fun aHumidityIsWrittenWholeInThePercentPatternWhateverThePreferences() {
        assertEquals("55%", formatHumidity(54.6, UnitPreferences("en")))
        assertEquals("55 %", formatHumidity(54.6, UnitPreferences("fr", length = RestSchema.UnitLength.Imperial)))
    }

    @Test
    fun oneThatRoundsToZeroHasNoSignAndNoNumberIsNothing() {
        assertEquals("0 %", formatHumidity(-0.4, UnitPreferences("de")))
        assertEquals("", formatHumidity(null, UnitPreferences("en")))
        assertEquals("", formatHumidity(Double.NaN, UnitPreferences("en")))
    }
}
