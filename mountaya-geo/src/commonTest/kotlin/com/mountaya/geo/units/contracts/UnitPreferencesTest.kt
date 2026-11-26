package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UnitPreferencesTest {
    @Test
    fun aReaderWhoSaysNothingButTheLanguageReadsMetricPerTimeDegreesAndCelsius() {
        val preferences = UnitPreferences("en")

        assertEquals(RestSchema.UnitLength.Metric, preferences.length)
        assertEquals(RestSchema.UnitPace.PerTime, preferences.pace)
        assertEquals(RestSchema.UnitSlope.Degrees, preferences.slope)
        assertEquals(RestSchema.UnitTemperature.Celsius, preferences.temperature)
        assertEquals(
            "UnitPreferences(language=en, length=metric, pace=per-time, slope=degrees, temperature=celsius)",
            preferences.toString(),
        )
    }

    @Test
    fun twoPreferencesAreEqualWhenEveryFieldIs() {
        val imperial = UnitPreferences("fr", length = RestSchema.UnitLength.Imperial)

        assertEquals(imperial, UnitPreferences("fr", length = RestSchema.UnitLength("imperial")))
        assertEquals(imperial.hashCode(), UnitPreferences("fr", length = RestSchema.UnitLength("imperial")).hashCode())
        assertNotEquals(imperial, UnitPreferences("fr"))
        assertNotEquals(imperial, UnitPreferences("en", length = RestSchema.UnitLength.Imperial))
    }
}
