package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PreferenceLabelTest {
    @Test
    fun twoLabelsAreEqualWhereTheyAreOfOneKindAndSpellTheSameText() {
        assertEquals<PreferenceLabel>(PreferenceLabel.Word("metric"), PreferenceLabel.Word("metric"))
        assertNotEquals<PreferenceLabel>(PreferenceLabel.Word("metric"), PreferenceLabel.Name("metric"))
        assertNotEquals<PreferenceLabel>(PreferenceLabel.Name("Français"), PreferenceLabel.Name("Français"))
        assertEquals(PreferenceLabel.Word("metric").hashCode(), PreferenceLabel.Word("metric").hashCode())
    }
}
