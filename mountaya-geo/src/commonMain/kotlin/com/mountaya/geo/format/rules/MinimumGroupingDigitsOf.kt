package com.mountaya.geo

/**
 * CLDR 48's minimum grouping digits by language, as ICU 78 carries them, where they are not 1: a language
 * of 2 writes no grouping separator under 10,000, and one of 3 none under 100,000.
 */
private val minimumGroupingDigitsByLanguage: Map<String, Int> =
    mapOf(
        "be" to 2,
        "bg" to 2,
        "ee" to 3,
        "es" to 2,
        "et" to 2,
        "hu" to 2,
        "hy" to 2,
        "ia" to 2,
        "ie" to 2,
        "it" to 2,
        "ka" to 2,
        "lv" to 2,
        "pl" to 2,
        "sl" to 2,
        "sq" to 2,
    )

/**
 * The regions whose locale writes another minimum than its language's, by language and region: Latin
 * American Spanish groups 1234 where Spanish does not, Portugal's Portuguese and the locales that follow
 * it group none under 10,000 where Brazil's does, and so does Ukraine's Russian.
 */
private val minimumGroupingDigitsByRegion: Map<String, Int> =
    "419 AR BO BR BZ CL CO CR CU DO EC GT HN MX NI PA PE PR PY SV US UY VE".split(' ').associate { "es-$it" to 1 } +
        "CH CV GQ GW LU MO MZ PT ST TL".split(' ').associate { "pt-$it" to 2 } +
        mapOf("ru-UA" to 2)

/**
 * minimumGroupingDigitsOf answers how many digits a number written for a reader of [locale], a BCP 47
 * language tag, holds in front of its first grouping separator: CLDR's minimum grouping digits, as ICU
 * writes a number with them on every platform that carries it. Most locales group from 1,000, so 1234 is
 * `1,234` in English; Spanish, Italian, Polish and a dozen more group from 10,000, so 1234 is `1234` and
 * 12,345 `12.345` in Spanish; Ewe groups from 100,000.
 *
 * The locale is read by its language and its region, its script and extensions aside, the region first
 * where CLDR gives it a minimum of its own, `es-MX` grouping from 1,000 where `es` groups from 10,000. A
 * locale CLDR names neither way, and a value that is no language tag, groups from 1,000.
 */
internal fun minimumGroupingDigitsOf(locale: String): Int {
    val subtags = languageTagOf(locale).split('-')
    val language = subtags[0].lowercase()
    // The region is two letters or three digits, after any script of four letters and before the first
    // singleton, which opens an extension (`-u-nu-arab`) or a private use whose subtags name no region.
    val region =
        subtags
            .drop(1)
            .takeWhile { it.length > 1 }
            .firstOrNull { (it.length == 2 && it.all(Char::isLetter)) || (it.length == 3 && it.all(Char::isDigit)) }
            ?.uppercase()
    if (region != null) minimumGroupingDigitsByRegion["$language-$region"]?.let { return it }
    return minimumGroupingDigitsByLanguage[language] ?: 1
}
