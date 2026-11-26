package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/** What stands for the number in a unit pattern. */
internal const val NUMBER_PLACEHOLDER: String = "{0}"

/**
 * Returns [value] in the reader's [language], rounded to [fractionDigits] places half away from
 * zero on its decimal value, written by the platform's locale services, and set in [pattern] where
 * it holds [NUMBER_PLACEHOLDER].
 *
 * [minimumFractionDigits] places are always written, so a figure stated to one place reads `1.0`.
 * The rounding is done before the platform sees the value, so the figure is the same on every
 * target, and a value that rounds to zero is +0, stated with no sign. The number is grouped as
 * [patterns] says the language groups it and never where the locale's own minimum grouping digits
 * say it is not (`isGroupedAt`): a language whose minimum grouping digits are 2 writes no separator
 * under 10,000, so a Spanish and a Polish 1531 are both `1531`.
 */
internal fun formatMeasurement(
    value: Double,
    fractionDigits: Int,
    pattern: String,
    language: String,
    patterns: UnitPatterns = unitPatterns(language),
    minimumFractionDigits: Int = 0,
): String {
    val rounded = roundHalfAwayFromZero(value, fractionDigits)
    val grouping = isGroupedAt(rounded, maxOf(patterns.minimumGroupingDigits, minimumGroupingDigitsOf(language)))
    return pattern.replace(
        NUMBER_PLACEHOLDER,
        localeNumberText(rounded, languageTagOf(language), fractionDigits, grouping, minimumFractionDigits),
    )
}

/** Whether [length] asks for the imperial system; any other system reads as metric. */
internal fun isImperial(length: RestSchema.UnitLength): Boolean = length == RestSchema.UnitLength.Imperial
