package com.mountaya.geo

/**
 * RangePatterns is how one language writes a range of two numbers and an approximate number, for
 * `formatAltitudeRange`: `{0}` and `{1}` stand for the numbers, each written by the platform's locale services.
 *
 * Neither `java.text` nor Foundation publishes range formatting of numbers on every target, Android's
 * `NumberRangeFormatter` being API 31's alone, so the patterns are this table, one for both native SDKs, beside
 * the unit patterns, for the seven languages the contract's `Locales` is keyed by: CLDR 48's range and
 * approximately patterns, as ICU 78 carries them and as `Intl.NumberFormat`'s `formatRange` writes them, the
 * separator an en dash in English, French and German, a hyphen-minus in Spanish, Italian and Chinese and a wave
 * dash in Japanese, and the approximation a tilde, U+2243 in French, U+2248 in German and U+7D04 in Japanese.
 */
internal class RangePatterns(
    val range: String,
    val approximately: String,
)

/** The English row, which a language outside the table writes. */
private val englishRangePatterns = RangePatterns(range = "{0}\u2013{1}", approximately = "~{0}")

/** The table, by primary language subtag. */
private val rangePatternsByLanguage: Map<String, RangePatterns> =
    mapOf(
        "de" to RangePatterns(range = "{0}\u2013{1}", approximately = "\u2248{0}"),
        "en" to englishRangePatterns,
        "es" to RangePatterns(range = "{0}-{1}", approximately = "~{0}"),
        "fr" to RangePatterns(range = "{0}\u2013{1}", approximately = "\u2243{0}"),
        "it" to RangePatterns(range = "{0}-{1}", approximately = "~{0}"),
        "ja" to RangePatterns(range = "{0}\uFF5E{1}", approximately = "\u7D04{0}"),
        "zh" to RangePatterns(range = "{0}-{1}", approximately = "~{0}"),
    )

/**
 * rangePatterns is the range and approximately patterns of [language], read by its primary subtag without regard
 * to case as `unitPatterns` reads it, English for a language the table holds none for.
 */
internal fun rangePatterns(language: String): RangePatterns =
    rangePatternsByLanguage[primaryLanguageOf(language)] ?: englishRangePatterns

/** The languages the table holds patterns for. */
internal val rangePatternLanguages: Set<String> get() = rangePatternsByLanguage.keys
