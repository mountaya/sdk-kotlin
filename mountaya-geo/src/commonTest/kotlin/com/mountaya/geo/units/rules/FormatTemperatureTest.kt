package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTemperatureTest {
    @Test
    fun aTemperatureIsWholeDegreesInTheScaleAsked() {
        assertEquals("4°C", formatTemperature(4.0, UnitPreferences("en")))
        assertEquals(
            "39°F",
            formatTemperature(
                4.0,
                UnitPreferences("en", temperature = RestSchema.UnitTemperature.Fahrenheit),
            ),
        )
        assertEquals("-12°C", formatTemperature(-12.4, UnitPreferences("en")))
    }

    /** Temperature is never derived from length. */
    @Test
    fun anImperialReaderWhoStoredCelsiusIsShownCelsius() {
        val reader = UnitPreferences("fr", length = RestSchema.UnitLength.Imperial)

        assertEquals("4 °C", formatTemperature(4.0, reader))
    }

    @Test
    fun aTemperatureThatRoundsToZeroHasNoSignAndAnUnknownScaleIsCelsius() {
        assertEquals("0°C", formatTemperature(-0.4, UnitPreferences("en")))
        assertEquals(
            "4°C",
            formatTemperature(4.0, UnitPreferences("en", temperature = RestSchema.UnitTemperature("kelvin"))),
        )
        assertEquals("", formatTemperature(Double.NaN, UnitPreferences("en")))
    }
}
