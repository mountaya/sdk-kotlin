package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageTagOfTest {
    @Test
    fun aLanguageTagIsHandedOnAsItIsWrittenAnUnderscoreReadAsAHyphen() {
        assertEquals("fr-FR", languageTagOf("fr-FR"))
        assertEquals("ar-EG", languageTagOf("ar_EG"))
        assertEquals("zh-Hant-TW", languageTagOf("zh_Hant_TW"))
        assertEquals("en-US-u-nu-arab", languageTagOf("en-US-u-nu-arab"))
    }

    @Test
    fun aValueThatIsNoLanguageTagReadsAsEnglish() {
        for (locale in listOf("", " fr", "fr-", "e", "fr--FR", "français", "123")) {
            assertEquals("en", languageTagOf(locale), "`$locale`")
        }
    }

    @Test
    fun thePrimarySubtagIsInLowerCase() {
        assertEquals("fr", primaryLanguageOf("FR_ca"))
        assertEquals("frr", primaryLanguageOf("frr"))
        assertEquals("en", primaryLanguageOf("not a tag"))
    }
}
