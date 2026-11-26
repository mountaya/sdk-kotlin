package com.mountaya.geo

import platform.Foundation.NSLocale
import platform.Foundation.calendarIdentifier
import platform.Foundation.componentsFromLocaleIdentifier
import platform.Foundation.countryCode
import platform.Foundation.languageCode
import platform.Foundation.localeIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The locale a tag names on iOS, read from the components Foundation parses out of the tag, since
 * its canonical reading of the tag drops a numbering system named as the language's script.
 */
class PlatformLocaleTest {
    /** The numbering system [locale]'s identifier names, `beng` for `bn_BD@numbers=beng`. */
    private fun numberingSystemOf(locale: NSLocale): Any? =
        NSLocale.componentsFromLocaleIdentifier(locale.localeIdentifier)["numbers"]

    @Test
    fun aTagsLanguageRegionCalendarAndNumberingSystemAreTheLocales() {
        val locale = platformLocale("th-TH-u-ca-gregory")
        assertEquals("th", locale.languageCode)
        assertEquals("TH", locale.countryCode)
        assertEquals("gregorian", locale.calendarIdentifier)
        assertEquals("FR", platformLocale("fr_FR").countryCode)
        val both = platformLocale("bn-BD-u-ca-gregory-nu-beng")
        assertEquals("gregorian", both.calendarIdentifier)
        assertEquals("beng", numberingSystemOf(both))
    }

    /**
     * `canonicalLocaleIdentifierFromString` reads `bn-BD-u-nu-beng` as `bn-BD-u-nu` on iOS 27, and
     * `th-TH-u-nu-thai` as `th-TH-u-nu`, though Thai's own digits are Latin; `arab` is no script of
     * English, so `en-US-u-nu-arab` keeps it there.
     */
    @Test
    fun aNumberingSystemNamedAsTheLanguagesScriptIsKept() {
        assertEquals("beng", numberingSystemOf(platformLocale("bn-BD-u-nu-beng")))
        assertEquals("arab", numberingSystemOf(platformLocale("ar-EG-u-nu-arab")))
        assertEquals("deva", numberingSystemOf(platformLocale("hi-IN-u-nu-deva")))
        assertEquals("thai", numberingSystemOf(platformLocale("th-TH-u-nu-thai")))
        assertEquals("arab", numberingSystemOf(platformLocale("en-US-u-nu-arab")))
    }

    @Test
    fun aNumberingSystemThePlatformDoesNotCarryIsDropped() {
        assertNull(numberingSystemOf(platformLocale("en-US-u-nu-zzzz")))
        assertEquals("en_US", platformLocale("en-US-u-nu-zzzz").localeIdentifier)
        assertEquals("japanese", platformLocale("en-US-u-nu-zzzz-ca-japanese").calendarIdentifier)
    }
}
