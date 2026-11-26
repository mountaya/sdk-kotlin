package com.mountaya.geo

import platform.Foundation.NSCache
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterLongStyle
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSDateFormatterStyle
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneWithName

/** How many formatters the cache holds; it evicts past that on its own. */
private const val CACHE_LIMIT = 64UL

/** The milliseconds in a second, which Foundation counts an instant in. */
private const val MILLISECONDS_PER_SECOND = 1_000.0

/**
 * One formatter per locale, pair of lengths and zone, built once. Foundation's formatters are safe to
 * share across threads, and so is the cache.
 */
private val formatters = NSCache().apply { countLimit = CACHE_LIMIT }

/**
 * Foundation's `NSDateFormatter` for the locale the tag names, as [platformLocale] reads it: its date
 * style the one of [dateLength], its time style the one of [timeLength], each none where its length is
 * not named, and the date's long style where neither is. The locale is built from the tag's components
 * and never from the user's current locale, so a time keeps the locale's own clock and not the device's
 * 12- or 24-hour setting. The calendar is always the Gregorian one, whatever calendar the locale keeps or
 * a `-u-ca-` extension of the tag names, set after the locale, which would set its own over one set first:
 * so a Thai date carries the year 2026 and a Persian one the Gregorian date in Persian's words and digits.
 */
internal actual fun localeDateText(
    epochMilliseconds: Long,
    languageTag: String,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): String? {
    val key = "$languageTag|$dateLength|$timeLength|$timeZone"
    val formatter =
        formatters.objectForKey(key) as? NSDateFormatter ?: run {
            val zone = NSTimeZone.timeZoneWithName(timeZone) ?: return null
            NSDateFormatter().also { built ->
                built.locale = platformLocale(languageTag)
                built.calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian)
                built.timeZone = zone
                val unnamed = if (timeLength == null) NSDateFormatterLongStyle else NSDateFormatterNoStyle
                built.dateStyle = dateLength?.let { styleOf(it) } ?: unnamed
                built.timeStyle = timeLength?.let { styleOf(it) } ?: NSDateFormatterNoStyle
                formatters.setObject(built, forKey = key)
            }
        }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMilliseconds / MILLISECONDS_PER_SECOND))
}

/** Foundation's style of [length]. */
private fun styleOf(length: DateLength): NSDateFormatterStyle =
    when (length) {
        DateLength.Short -> NSDateFormatterShortStyle
        DateLength.Medium -> NSDateFormatterMediumStyle
        DateLength.Long -> NSDateFormatterLongStyle
        DateLength.Full -> NSDateFormatterFullStyle
    }

/** Foundation's style of [length]. */
private fun styleOf(length: TimeLength): NSDateFormatterStyle =
    when (length) {
        TimeLength.Short -> NSDateFormatterShortStyle
        TimeLength.Medium -> NSDateFormatterMediumStyle
    }
