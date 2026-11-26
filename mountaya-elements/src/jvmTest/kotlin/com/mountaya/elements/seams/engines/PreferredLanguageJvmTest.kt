package com.mountaya.elements

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferredLanguageJvmTest {
    @Test
    fun theEnvironmentsLanguageIsTheRuntimesDefaultLocaleAsATag() {
        val before = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("de-CH"))
            assertEquals("de-CH", preferredLanguage())
            Locale.setDefault(Locale.ROOT)
            assertEquals(null, preferredLanguage())
        } finally {
            Locale.setDefault(before)
        }
    }
}
