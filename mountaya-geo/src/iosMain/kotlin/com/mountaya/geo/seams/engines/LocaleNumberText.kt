package com.mountaya.geo

import platform.Foundation.NSCache
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumberFormatterRoundHalfUp

/** How many formatters the cache holds; it evicts past that on its own. */
private const val CACHE_LIMIT = 64UL

/**
 * One formatter per locale and shape, built once. Foundation's formatters are safe to share
 * across threads, and so is the cache.
 */
private val formatters = NSCache().apply { countLimit = CACHE_LIMIT }

/**
 * Foundation's `NSNumberFormatter`, in its decimal style, for the locale the tag names, as
 * [platformLocale] reads it. The rounding mode is half away from zero, though the value it is
 * handed is already rounded.
 */
internal actual fun localeNumberText(
    value: Double,
    languageTag: String,
    fractionDigits: Int,
    grouping: Boolean,
    minimumFractionDigits: Int,
): String {
    val key = "$languageTag|$fractionDigits|$grouping|$minimumFractionDigits"
    val formatter =
        formatters.objectForKey(key) as? NSNumberFormatter ?: NSNumberFormatter().also { built ->
            built.locale = platformLocale(languageTag)
            built.numberStyle = NSNumberFormatterDecimalStyle
            built.minimumFractionDigits = minimumFractionDigits.coerceIn(0, fractionDigits).toULong()
            built.maximumFractionDigits = fractionDigits.toULong()
            built.usesGroupingSeparator = grouping
            built.roundingMode = NSNumberFormatterRoundHalfUp
            formatters.setObject(built, forKey = key)
        }
    return formatter.stringFromNumber(NSNumber(double = value)).orEmpty()
}
