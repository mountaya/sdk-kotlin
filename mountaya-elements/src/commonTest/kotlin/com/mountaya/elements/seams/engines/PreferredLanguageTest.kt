package com.mountaya.elements

import com.mountaya.geo.labelLanguage
import com.mountaya.geo.labelLanguages
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PreferredLanguageTest {
    @Test
    fun theEnvironmentsLanguageIsATagALabelLanguageIsReadFrom() {
        val tag = preferredLanguage()

        assertNotEquals("", tag)
        assertTrue(labelLanguage(tag) in labelLanguages)
    }
}
