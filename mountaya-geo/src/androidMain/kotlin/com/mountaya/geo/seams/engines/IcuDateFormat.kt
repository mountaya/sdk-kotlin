package com.mountaya.geo

import android.icu.text.DateFormat
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import java.text.Format
import java.util.Locale

/**
 * The formatter android writes an instant with, for [locale] in the zone the IANA name [timeZone] names, at
 * [dateLength] and [timeLength]: ICU's, [icuDateFormat], which every release from this SDK's floor carries.
 * Android's `java.text` writes a short and a medium time on the device's 12- or 24-hour clock where the person
 * set one, and joins a date and a time with one space whatever the locale writes, where its ICU keeps the
 * locale's own clock and joins them as the locale's data joins a date of that length. The jvm declares its own
 * in `PlatformDateFormat.kt`, since the file that asks for it, `LocaleDateText.kt`, is held alike in both source
 * sets.
 */
internal fun platformDateFormat(
    locale: Locale,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): Format = icuDateFormat(locale, timeZone, dateLength, timeLength)

/**
 * `android.icu`'s `DateFormat` for [locale] over a Gregorian calendar of the zone the IANA name [timeZone]
 * names: the date at [dateLength], the time of day at [timeLength], or both, joined as the locale's data joins a
 * date of that length and a time, and the date at [DateLength.Long] where neither is named. A time keeps the
 * locale's own clock whatever clock the device is set to, and every field is written in the locale's own
 * digits.
 */
internal fun icuDateFormat(
    locale: Locale,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): DateFormat {
    val read = ULocale.forLocale(locale)
    val calendar = GregorianCalendar(TimeZone.getTimeZone(timeZone), read)
    val dateStyle = dateLength?.let { icuStyleOf(it) } ?: if (timeLength == null) DateFormat.LONG else DateFormat.NONE
    val timeStyle = timeLength?.let { icuStyleOf(it) } ?: DateFormat.NONE
    return DateFormat.getDateTimeInstance(calendar, dateStyle, timeStyle, read)
}

/** ICU's style of [length]. */
private fun icuStyleOf(length: DateLength): Int =
    when (length) {
        DateLength.Short -> DateFormat.SHORT
        DateLength.Medium -> DateFormat.MEDIUM
        DateLength.Long -> DateFormat.LONG
        DateLength.Full -> DateFormat.FULL
    }

/** ICU's style of [length]. */
private fun icuStyleOf(length: TimeLength): Int =
    when (length) {
        TimeLength.Short -> DateFormat.SHORT
        TimeLength.Medium -> DateFormat.MEDIUM
    }
