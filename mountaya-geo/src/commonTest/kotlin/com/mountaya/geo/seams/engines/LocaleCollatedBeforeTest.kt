package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocaleCollatedBeforeTest {
    @Test
    fun aTextSortsByTheReadersLocaleWhateverItsCase() {
        assertTrue(localeCollatedBefore("Food", "sauna", "en"))
        assertTrue(localeCollatedBefore("sauna", "Water", "en"))
        assertFalse(localeCollatedBefore("Water", "Food", "en"))
        assertTrue(localeCollatedBefore("Eau", "Nourriture", "fr"))
        assertFalse(localeCollatedBefore("Food", "Food", "en"))
    }
}
