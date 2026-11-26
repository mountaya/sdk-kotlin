package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PreferenceOptionTest {
    @Test
    fun anOptionHoldsItsValueItsLabelAndWhetherItIsChecked() {
        val metric = PreferenceValue.Length(RestSchema.UnitLength.Metric)
        val option = PreferenceOption(metric, PreferenceLabel.Word("metric"), isChecked = true)

        assertEquals<PreferenceValue>(metric, option.value)
        assertEquals<PreferenceLabel>(PreferenceLabel.Word("metric"), option.label)
        assertTrue(option.isChecked)
        assertNotEquals(option, PreferenceOption(metric, PreferenceLabel.Word("metric"), isChecked = false))
    }
}
