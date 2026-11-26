package com.mountaya.geo

import kotlin.time.Instant

/**
 * formatDateTime writes [instant] for a reader of [locale], a BCP 47 language tag, in [timeZone], an
 * IANA zone name such as `Europe/Paris`, through the platform's locale services: its date at
 * [dateLength], its time of day at [timeLength], or both, joined as the locale's data joins a date
 * and a time, and its date at [DateLength.Long] where neither length is named. Each is the locale's
 * own style of that length, its fields, their order, its words, its digits and its hour cycle,
 * whatever clock the device is set to. 2026-03-14T00:00:00Z is `March 14, 2026` in American English,
 * `14 mars 2026` in French and `١٤ مارس ٢٠٢٦` in Egyptian Arabic; its time of day in Auckland is
 * `13:00` in German and `오후 1:00` in Korean, and in Kathmandu `०५:४५` in Nepali.
 *
 * The zone and the locale are arguments, never the platform's defaults, and the instant is placed at
 * the offset the zone keeps at that instant, so the date in New York is the 13th. The calendar is
 * always the Gregorian one, whatever calendar the locale keeps or its tag's `-u-ca-` extension names
 * (`geo.format.locale-services`): Thai (`th-TH`) writes 2026 and the era that names it,
 * `14 มีนาคม ค.ศ. 2026`, and never the Buddhist year 2569, on every target. An underscore in [locale]
 * parts subtags as a hyphen does, and a value that is no language tag reads as English. Null where the
 * platform knows no zone of that name.
 */
public fun formatDateTime(
    instant: Instant,
    locale: String,
    timeZone: String,
    dateLength: DateLength? = null,
    timeLength: TimeLength? = null,
): String? = localeDateText(instant.toEpochMilliseconds(), languageTagOf(locale), timeZone, dateLength, timeLength)
