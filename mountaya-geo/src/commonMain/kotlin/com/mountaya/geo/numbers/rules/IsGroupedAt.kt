package com.mountaya.geo

import kotlin.math.abs

/** The digits a group holds, the first grouping separator standing after this many past the least. */
private const val GROUP_DIGITS = 3

/**
 * Returns whether [value], already rounded to the places it is written with, is written with grouping
 * separators where a locale's minimum grouping digits are [minimumGroupingDigits]: a separator stands only
 * where at least that many digits stand in front of it, so with 1 every number from 1,000 is grouped, and
 * with 2 none under 10,000, `1234` and `12.345` in Spanish. The one rule every number Geo writes is grouped
 * by, a measurement and a plain number alike, whatever grouping the platform's own data holds.
 */
internal fun isGroupedAt(
    value: Double,
    minimumGroupingDigits: Int,
): Boolean = abs(value) >= powerOfTen(GROUP_DIGITS + minimumGroupingDigits - 1)
