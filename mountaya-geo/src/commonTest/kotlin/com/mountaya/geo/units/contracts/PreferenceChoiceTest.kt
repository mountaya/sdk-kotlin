package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PreferenceChoiceTest {
    @Test
    fun aChoiceHoldsItsFieldAndItsOptionsInOrder() {
        val options =
            listOf(
                PreferenceOption(
                    PreferenceValue.Slope(RestSchema.UnitSlope.Degrees),
                    PreferenceLabel.Word("degrees"),
                    true,
                ),
                PreferenceOption(
                    PreferenceValue.Slope(RestSchema.UnitSlope.Percentage),
                    PreferenceLabel.Word("percentage"),
                    false,
                ),
            )
        val choice = PreferenceChoice(PreferenceField.Slope, options)

        assertEquals(PreferenceField.Slope, choice.field)
        assertEquals(options, choice.options)
        assertNotEquals(choice, PreferenceChoice(PreferenceField.Slope, options.reversed()))
    }
}
