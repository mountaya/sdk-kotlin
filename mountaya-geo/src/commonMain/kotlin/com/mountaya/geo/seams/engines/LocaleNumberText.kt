package com.mountaya.geo

/**
 * Returns [value] written by the platform's locale services for [languageTag]: its digits, its
 * decimal mark, its minus sign and, where [grouping] asks for them, its grouping separators, with
 * no fraction digit past [fractionDigits] and none it does not need beyond [minimumFractionDigits],
 * which a figure stated to a fixed place asks for (`1.0 km`).
 *
 * The value arrives already rounded to [fractionDigits] places, by the rule every target shares, so
 * the platform writes it and decides nothing: its own rounding never sees a value it could round
 * another way. The locale is always the one named, never the platform's default. On the jvm and
 * android this is `java.text`'s `NumberFormat`, and on iOS Foundation's `NSNumberFormatter`.
 */
internal expect fun localeNumberText(
    value: Double,
    languageTag: String,
    fractionDigits: Int,
    grouping: Boolean,
    minimumFractionDigits: Int = 0,
): String
