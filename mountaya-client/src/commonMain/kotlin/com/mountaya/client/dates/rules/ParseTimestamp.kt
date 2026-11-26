package com.mountaya.client

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The contract's anchor (`sdk/SPEC.md` section 4): a whole value that is an RFC 3339 timestamp carrying a zone.
 *
 * A date, a `T`, a time to the second with an optional fraction, then `Z` or an offset with its
 * colon, and nothing before or after. The letters are uppercase, and every field but the day is
 * held to its range here; the day is held to its month by the parse that follows.
 */
private val ANCHOR =
    Regex(
        "([0-9]{4}-(?:0[1-9]|1[0-2])-[0-9]{2}T(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])" +
            "(?:\\.([0-9]+))?(?:Z|([+-])([01][0-9]|2[0-3]):([0-5][0-9]))",
    )

/** The digits of a fraction an [Instant] holds, which is to the nanosecond. */
private const val NANOSECOND_DIGITS = 9

/**
 * parseTimestamp answers the instant [value] names where it is a full timestamp with a zone, and null
 * where it is anything else.
 *
 * The anchor is checked before `Instant.parse` sees the value, because what a parser accepts is
 * the library's decision and can move with a release, where the anchor is the contract's. So the
 * parser is handed only what it reads alike on every platform: the date and time in UTC, with a
 * fraction cut to the nanosecond, and the offset is applied here, since `Instant.parse` refuses
 * an offset past 18 hours that the grammar writes. A value that passes the anchor and still
 * names no instant, such as the thirtieth of February, is not one either. A date alone and a
 * sentence that merely contains a timestamp stay strings: an unanchored match would turn a title
 * a user typed into a date.
 *
 * It is the anchor every timestamp the client decodes is read by (`transport.dates.reviver`), published so a
 * rule that holds a timestamp as text, as Geo holds a place's `date` field and an app a value it stored, reads it
 * as the wire is read, as `sdk-swift` and `sdk-dart` publish theirs.
 */
public fun parseTimestamp(value: String): Instant? {
    val (dateTime, fraction, sign, offsetHours, offsetMinutes) =
        ANCHOR.matchEntire(value)?.destructured ?: return null
    val written = if (fraction.isEmpty()) dateTime else "$dateTime.${fraction.take(NANOSECOND_DIGITS)}"
    val utc =
        try {
            Instant.parse("${written}Z")
        } catch (noSuchDay: IllegalArgumentException) {
            return null
        }
    if (sign.isEmpty()) return utc
    val offset = offsetHours.toInt().hours + offsetMinutes.toInt().minutes
    return if (sign == "+") utc - offset else utc + offset
}
