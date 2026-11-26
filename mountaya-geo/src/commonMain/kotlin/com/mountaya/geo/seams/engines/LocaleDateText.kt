package com.mountaya.geo

/**
 * Returns the instant [epochMilliseconds] after the epoch, in the time zone the IANA name [timeZone]
 * names, written by the platform's locale services for [languageTag]: its date at [dateLength], its
 * time of day at [timeLength], or both, joined as the platform's data for the locale joins them, and
 * its date at [DateLength.Long] where neither is named. Each is the locale's own style of that length,
 * its fields, their order, its words, its digits and its hour cycle, whatever clock the device is set
 * to, in the Gregorian calendar. Null where the platform knows no zone of that name.
 *
 * The locale and the zone are always the ones named, never the platform's defaults. On the jvm this is
 * `java.text`'s `DateFormat`, on android `android.icu.text.DateFormat`, and on iOS Foundation's
 * `NSDateFormatter`.
 */
internal expect fun localeDateText(
    epochMilliseconds: Long,
    languageTag: String,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): String?
