package com.mountaya.elements

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Instant

/** The fields a time slider's readout writes a moment in: the weekday, the day, the month, the hour and the minute. */
private const val MOMENT_SKELETON: String = "EEEdMMMjmm"

/**
 * momentText writes [instant] as a time slider's thumb reads it (`geo.layers.time-readout`): the weekday, the day, the
 * month, the hour and the minute, in the fields, the order, the words and the digits the platform's locale services
 * choose for [languageTag], the hour in the locale's own cycle, in [timeZone], the device's in a panel
 * (`deviceTimeZone`).
 */
internal fun momentText(
    instant: Instant,
    languageTag: String,
    timeZone: TimeZone,
): String {
    val locale = Locale.forLanguageTag(languageTag)
    return SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, MOMENT_SKELETON), locale)
        .apply { this.timeZone = timeZone }
        .format(Date(instant.toEpochMilliseconds()))
}
