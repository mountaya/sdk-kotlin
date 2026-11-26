package com.mountaya.geo

/**
 * formatNumber writes [value] for a reader of [locale], a BCP 47 language tag such as `ar-EG`,
 * through the platform's locale services: the locale's own digits, decimal mark, minus sign and
 * grouping, never digits assembled by hand, which are Latin digits by construction. 1531 is
 * `1,531` in American English, `١٬٥٣١` in Egyptian Arabic and `১,৫৩১` in Bengali.
 *
 * The value is rounded to [maximumFractionDigits] places, half away from zero on its decimal value,
 * before the platform sees it, and written with no fraction digit it does not need: 24.3 to one
 * place is `24,3` in French. The default of three places is every locale's own for a plain number.
 * The places are read within 0 and 20. It is grouped where the locale's CLDR minimum grouping digits
 * say, on every platform, as ICU groups it: 1234 is `1234` and 12,345 `12.345` in Spanish, and a
 * Polish 1531 is `1531`. An underscore in [locale] parts subtags as a hyphen does, and a value that is
 * no language tag reads as English. A value that is no finite number is the empty string.
 */
public fun formatNumber(
    value: Double,
    locale: String,
    maximumFractionDigits: Int = 3,
): String {
    if (!value.isFinite()) return ""
    val digits = maximumFractionDigits.coerceIn(0, MAX_FRACTION_DIGITS)
    val rounded = roundHalfAwayFromZero(value, digits)
    val grouping = isGroupedAt(rounded, minimumGroupingDigitsOf(locale))
    return localeNumberText(rounded, languageTagOf(locale), digits, grouping)
}
