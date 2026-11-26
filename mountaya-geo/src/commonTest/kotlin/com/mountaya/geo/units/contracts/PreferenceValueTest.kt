package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PreferenceValueTest {
    @Test
    fun aValueNamesItsPreferenceAndItsWireSpelling() {
        assertEquals(PreferenceField.Length, PreferenceValue.Length(RestSchema.UnitLength.Imperial).field)
        assertEquals("imperial", PreferenceValue.Length(RestSchema.UnitLength.Imperial).id)
        assertEquals("per-distance", PreferenceValue.Pace(RestSchema.UnitPace.PerDistance).id)
        assertEquals(PreferenceField.Slope, PreferenceValue.Slope(RestSchema.UnitSlope.Percentage).field)
        assertEquals(
            PreferenceField.Temperature,
            PreferenceValue.Temperature(RestSchema.UnitTemperature.Fahrenheit).field,
        )
        assertEquals(PreferenceField.Language, PreferenceValue.Language(RestSchema.Language.Fr).field)
        assertEquals("fr", PreferenceValue.Language(RestSchema.Language.Fr).id)
    }

    @Test
    fun twoValuesAreEqualWhereTheyAreOfOnePreferenceAndSpellTheSameText() {
        assertEquals<PreferenceValue>(
            PreferenceValue.Language(RestSchema.Language("fr")),
            PreferenceValue.Language(RestSchema.Language.Fr),
        )
        assertNotEquals<PreferenceValue>(
            PreferenceValue.Language(RestSchema.Language("fr")),
            PreferenceValue.Language(RestSchema.Language("fr-CA")),
        )
        assertNotEquals<PreferenceValue>(
            PreferenceValue.Slope(RestSchema.UnitSlope("degrees")),
            PreferenceValue.Temperature(RestSchema.UnitTemperature("degrees")),
        )
        assertEquals(
            PreferenceValue.Pace(RestSchema.UnitPace.PerTime).hashCode(),
            PreferenceValue.Pace(RestSchema.UnitPace("per-time")).hashCode(),
        )
    }
}
