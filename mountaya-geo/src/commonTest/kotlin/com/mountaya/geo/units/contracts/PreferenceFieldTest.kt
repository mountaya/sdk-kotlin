package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class PreferenceFieldTest {
    @Test
    fun theFivePreferencesInTheOrderAPickerOffersThemSpelledAsTheContractDoes() {
        assertEquals(
            listOf("length", "pace", "slope", "temperature", "language"),
            PreferenceField.entries.map { it.id },
        )
    }
}
