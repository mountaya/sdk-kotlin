package com.mountaya.geo

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.componentsFromLocaleIdentifier
import platform.Foundation.localeIdentifierFromComponents

/** The component a locale identifier names its numbering system by, `bn_BD@numbers=beng`. */
private const val NUMBERS = "numbers"

/**
 * Returns the `NSLocale` [languageTag] names, built from the components Foundation parses out of
 * the tag and never from the platform's default: its language, script, region, calendar, numbering
 * system and the rest of its `-u-` keywords.
 *
 * It is built from `componentsFromLocaleIdentifier`, never from the tag handed to
 * `canonicalLocaleIdentifierFromString` or to `NSLocale(localeIdentifier)`, because on iOS 27 both
 * read a numbering system whose name is also the language's script, `bn-BD-u-nu-beng`,
 * `ar-EG-u-nu-arab`, `hi-IN-u-nu-deva`, as that script and drop it, leaving `bn-BD-u-nu`, and the
 * formatters then write nothing. A numbering system the platform does not carry, `en-US-u-nu-zzzz`,
 * is dropped, so the locale writes its own digits, as `java.text` reads such a tag: Foundation's
 * formatters write nothing for one.
 */
internal fun platformLocale(languageTag: String): NSLocale {
    val components = NSLocale.componentsFromLocaleIdentifier(languageTag).toMutableMap()
    val named = NSLocale(localeIdentifier = NSLocale.localeIdentifierFromComponents(components))
    if (components[NUMBERS] == null || writesNumbers(named)) return named
    components.remove(NUMBERS)
    return NSLocale(localeIdentifier = NSLocale.localeIdentifierFromComponents(components))
}

/** Whether Foundation writes a number in [locale], which it does in every numbering system it carries. */
private fun writesNumbers(locale: NSLocale): Boolean =
    NSNumberFormatter().also { it.locale = locale }.stringFromNumber(NSNumber(int = 0)) != null
