package com.mountaya.geo

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/** How many formatters a thread keeps, the one it used least recently dropped past it. */
private const val CACHE_LIMIT = 64

/**
 * The number formatters each thread writes with, one per locale and shape, built once and kept in the
 * order the thread last used them. A `java.text` formatter is not thread-safe, so a thread formats with
 * its own alone, and none is shared or copied; past [CACHE_LIMIT] the one the thread used least recently
 * is dropped, and a locale still in use stays.
 */
private val formatters =
    object : ThreadLocal<LinkedHashMap<String, NumberFormat>>() {
        override fun initialValue(): LinkedHashMap<String, NumberFormat> =
            object : LinkedHashMap<String, NumberFormat>(CACHE_LIMIT, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, NumberFormat>?): Boolean =
                    size > CACHE_LIMIT
            }
    }

/**
 * `java.text`'s `NumberFormat` for the locale the tag names. Both the jvm and android carry it, and
 * on android it is the platform's own ICU, so the digits are the ones the device's locale data
 * holds. The rounding mode is half away from zero, though the value it is handed is already rounded.
 */
internal actual fun localeNumberText(
    value: Double,
    languageTag: String,
    fractionDigits: Int,
    grouping: Boolean,
    minimumFractionDigits: Int,
): String = numberFormatOf(languageTag, fractionDigits, grouping, minimumFractionDigits).format(value)

/**
 * The formatter the calling thread writes [languageTag]'s numbers with in this shape: the one it built
 * before, which is then the one it used most recently, or one built now and kept.
 */
internal fun numberFormatOf(
    languageTag: String,
    fractionDigits: Int,
    grouping: Boolean,
    minimumFractionDigits: Int,
): NumberFormat {
    // Every thread holds a map from its first read on, which android annotates and the jvm does not.
    val held = checkNotNull(formatters.get()) { "A thread holds its formatters from its first read on." }
    val key = "$languageTag|$fractionDigits|$grouping|$minimumFractionDigits"
    return held[key] ?: NumberFormat.getNumberInstance(Locale.forLanguageTag(languageTag)).also { built ->
        built.minimumFractionDigits = minimumFractionDigits.coerceIn(0, fractionDigits)
        built.maximumFractionDigits = fractionDigits
        built.isGroupingUsed = grouping
        built.roundingMode = RoundingMode.HALF_UP
        held[key] = built
    }
}
