package com.mountaya.geo

import java.text.DateFormat
import java.text.Format
import java.util.Calendar
import java.util.Date
import java.util.IllformedLocaleException
import java.util.Locale
import java.util.TimeZone

/** How many formatters a thread keeps, the one it used least recently dropped past it. */
private const val CACHE_LIMIT = 64

/**
 * The formatters each thread writes an instant with, one per locale, pair of lengths and zone, built once and
 * kept in the order the thread last used them. Neither a `java.text` formatter nor an ICU one is thread-safe,
 * so a thread formats with its own alone, and none is shared or copied; past [CACHE_LIMIT] the one the thread
 * used least recently is dropped, and a locale still in use stays.
 */
private val formatters =
    object : ThreadLocal<LinkedHashMap<String, Format>>() {
        override fun initialValue(): LinkedHashMap<String, Format> =
            object : LinkedHashMap<String, Format>(CACHE_LIMIT, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Format>?): Boolean =
                    size > CACHE_LIMIT
            }
    }

/**
 * The zones the runtime knows. `TimeZone.getTimeZone` answers GMT for a name it does not know,
 * so a name is looked up here first and an unknown one answers no date.
 */
private val zones: Set<String> by lazy { TimeZone.getAvailableIDs().toSet() }

/**
 * The instant written by the formatter this source set's `platformDateFormat` builds for the locale the tag
 * names, at the lengths named, in the zone named. The jvm and android hold this file twice, alike, and each
 * declares its own `platformDateFormat`: the jvm's, in `PlatformDateFormat.kt`, is `java.text`'s, and android's,
 * in `IcuDateFormat.kt`, `android.icu`'s, since android's `java.text` follows the device's 12- or
 * 24-hour setting and joins a date and a time with one space. The calendar is always the Gregorian one,
 * whatever calendar the locale keeps or its tag names: the locale is read with its `ca` keyword set to
 * `gregory`, since the pattern a locale's style writes is chosen by the locale's calendar, and a Gregorian
 * `Calendar` set on a Buddhist pattern would still write Thai's year without its era. So a Thai date carries
 * the year 2026 and the era that names it.
 */
internal actual fun localeDateText(
    epochMilliseconds: Long,
    languageTag: String,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): String? {
    if (timeZone !in zones) return null
    return dateFormatOf(languageTag, timeZone, dateLength, timeLength).format(Date(epochMilliseconds))
}

/**
 * The formatter the calling thread writes [languageTag]'s instants with at [dateLength] and [timeLength] in
 * [timeZone], a zone the runtime knows: the one it built before, which is then the one it used most recently,
 * or one built now and kept.
 */
internal fun dateFormatOf(
    languageTag: String,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): Format {
    // Every thread holds a map from its first read on, which android annotates and the jvm does not.
    val held = checkNotNull(formatters.get()) { "A thread holds its formatters from its first read on." }
    val key = "$languageTag|$dateLength|$timeLength|$timeZone"
    return held[key]
        ?: platformDateFormat(gregorian(Locale.forLanguageTag(languageTag)), timeZone, dateLength, timeLength)
            .also { built -> held[key] = built }
}

/**
 * `java.text`'s `DateFormat` for [locale] in the zone the IANA name [timeZone] names: the date at [dateLength],
 * the time of day at [timeLength], or both, joined as `getDateTimeInstance` joins them, and the date at
 * [DateLength.Long] where neither is named. Both the jvm and android carry it, and it writes the locale's own
 * digits.
 */
internal fun javaTextDateFormat(
    locale: Locale,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): DateFormat {
    val zone = TimeZone.getTimeZone(timeZone)
    val built =
        when {
            timeLength == null -> DateFormat.getDateInstance(styleOf(dateLength ?: DateLength.Long), locale)
            dateLength == null -> DateFormat.getTimeInstance(styleOf(timeLength), locale)
            else -> DateFormat.getDateTimeInstance(styleOf(dateLength), styleOf(timeLength), locale)
        }
    built.calendar = Calendar.getInstance(zone, locale)
    built.timeZone = zone
    return built
}

/**
 * [locale] with its calendar keyword set to the Gregorian calendar, `ca=gregory`, in place of any it
 * names, or [locale] itself where the builder refuses it, which no locale a language tag reads to is.
 */
internal fun gregorian(locale: Locale): Locale =
    try {
        Locale
            .Builder()
            .setLocale(locale)
            .setUnicodeLocaleKeyword("ca", "gregory")
            .build()
    } catch (refused: IllformedLocaleException) {
        locale
    }

/** The `java.text` style of [length]. */
private fun styleOf(length: DateLength): Int =
    when (length) {
        DateLength.Short -> DateFormat.SHORT
        DateLength.Medium -> DateFormat.MEDIUM
        DateLength.Long -> DateFormat.LONG
        DateLength.Full -> DateFormat.FULL
    }

/** The `java.text` style of [length]. */
private fun styleOf(length: TimeLength): Int =
    when (length) {
        TimeLength.Short -> DateFormat.SHORT
        TimeLength.Medium -> DateFormat.MEDIUM
    }
