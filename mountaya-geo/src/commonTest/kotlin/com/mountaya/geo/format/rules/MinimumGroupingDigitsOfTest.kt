package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class MinimumGroupingDigitsOfTest {
    @Test
    fun mostLocalesGroupFromAThousandAndADozenLanguagesFromTenThousand() {
        assertEquals(1, minimumGroupingDigitsOf("en-US"))
        assertEquals(1, minimumGroupingDigitsOf("de"))
        assertEquals(1, minimumGroupingDigitsOf("fr-CA"))
        assertEquals(2, minimumGroupingDigitsOf("es"))
        assertEquals(2, minimumGroupingDigitsOf("pl-PL"))
        assertEquals(2, minimumGroupingDigitsOf("it_CH"))
        assertEquals(3, minimumGroupingDigitsOf("ee"))
    }

    /** A region CLDR gives a minimum of its own is read before its language. */
    @Test
    fun aRegionWithAMinimumOfItsOwnIsReadBeforeItsLanguage() {
        assertEquals(1, minimumGroupingDigitsOf("es-MX"))
        assertEquals(1, minimumGroupingDigitsOf("es-419"))
        assertEquals(2, minimumGroupingDigitsOf("es-ES"))
        assertEquals(1, minimumGroupingDigitsOf("pt-BR"))
        assertEquals(2, minimumGroupingDigitsOf("pt-PT"))
        assertEquals(2, minimumGroupingDigitsOf("ru-UA"))
        assertEquals(1, minimumGroupingDigitsOf("ru"))
    }

    /** The region is read past a script and never inside an extension. */
    @Test
    fun theRegionIsReadPastAScriptAndNeverInsideAnExtension() {
        assertEquals(1, minimumGroupingDigitsOf("es-Latn-MX"))
        assertEquals(2, minimumGroupingDigitsOf("pt-Latn-PT-u-nu-latn"))
        assertEquals(2, minimumGroupingDigitsOf("es-u-rg-mxzzzz"))
        assertEquals(2, minimumGroupingDigitsOf("es-x-mx"))
        assertEquals(1, minimumGroupingDigitsOf("ES-mx"))
    }

    @Test
    fun aValueThatIsNoLanguageTagGroupsAsEnglish() {
        assertEquals(1, minimumGroupingDigitsOf(""))
        assertEquals(1, minimumGroupingDigitsOf("not a tag"))
    }
}
