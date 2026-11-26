package com.mountaya.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Reads a `Retry-After` header as the wait it asks for, in whole seconds, or returns null where it
 * is neither of its two spellings.
 *
 * The first spelling is a count of seconds, digits and nothing else. The second is an HTTP date,
 * read in the three formats RFC 9110 has a recipient accept, and the wait is how far it lies
 * after [now], rounded up to the whole second, so a repeat scheduled from it never comes back
 * before the date; a date already past asks for no wait, rather than for a negative one a backoff
 * would read as an error. The transport waits what it answers and an error names what it answers,
 * so the two are one wait. The API sends it on every 429 and on a 409 whose `Idempotency-Key` is
 * still in use, and a proxy in front of it may write one on another answer: a client honours it
 * wherever it comes from and never waits for one that does not come.
 */
internal fun parseRetryAfter(
    header: String,
    now: Instant,
): Duration? {
    val value = header.trim(' ', '\t')
    if (value.isNotEmpty() && value.all { it in '0'..'9' }) return value.toLongOrNull()?.seconds
    val date = parseHttpDate(value, now) ?: return null
    return (date - now).coerceAtLeast(Duration.ZERO).toComponents { seconds, nanoseconds ->
        (if (nanoseconds > 0) seconds + 1 else seconds).seconds
    }
}

private const val SHORT_DAYS = "Mon|Tue|Wed|Thu|Fri|Sat|Sun"
private const val LONG_DAYS = "Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday"
private const val MONTH_NAMES = "Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec"
private const val TIME_OF_DAY = "([0-9]{2}):([0-9]{2}):([0-9]{2})"

private val MONTHS = MONTH_NAMES.split('|')

/** `Sun, 06 Nov 1994 08:49:37 GMT`, the one format a sender writes today. */
private val IMF_FIXDATE = Regex("($SHORT_DAYS), ([0-9]{2}) ($MONTH_NAMES) ([0-9]{4}) $TIME_OF_DAY GMT")

/** `Sunday, 06-Nov-94 08:49:37 GMT`, obsolete, with a two-digit year. */
private val RFC_850_DATE = Regex("($LONG_DAYS), ([0-9]{2})-($MONTH_NAMES)-([0-9]{2}) $TIME_OF_DAY GMT")

/** `Sun Nov  6 08:49:37 1994`, obsolete, the day padded with a space and the zone implied. */
private val ASCTIME_DATE = Regex("($SHORT_DAYS) ($MONTH_NAMES) ([0-9]{2}| [0-9]) $TIME_OF_DAY ([0-9]{4})")

private fun parseHttpDate(
    value: String,
    now: Instant,
): Instant? {
    IMF_FIXDATE.matchEntire(value)?.destructured?.let { (_, day, month, year, hour, minute, second) ->
        return instantOf(year.toInt(), month, day, hour, minute, second)
    }
    RFC_850_DATE.matchEntire(value)?.destructured?.let { (_, day, month, year, hour, minute, second) ->
        return instantOf(fullYear(year.toInt(), now), month, day, hour, minute, second)
    }
    ASCTIME_DATE.matchEntire(value)?.destructured?.let { (_, month, day, hour, minute, second, year) ->
        return instantOf(year.toInt(), month, day.trim(), hour, minute, second)
    }
    return null
}

/**
 * The year a two-digit year names: the one with those last two digits that lies no more than 50
 * years after [now], as RFC 9110 has a recipient read it, and no more than 50 years before it.
 */
private fun fullYear(
    twoDigits: Int,
    now: Instant,
): Int {
    val current = yearOf(now)
    val year = current - current % 100 + twoDigits
    return when {
        year > current + 50 -> year - 100
        year <= current - 50 -> year + 100
        else -> year
    }
}

private const val SECONDS_PER_DAY = 86_400L

/** The instant a date names in UTC, or null where a component is out of range. */
private fun instantOf(
    year: Int,
    monthName: String,
    dayText: String,
    hourText: String,
    minuteText: String,
    secondText: String,
): Instant? {
    val month = MONTHS.indexOf(monthName) + 1
    val day = dayText.toInt()
    val hour = hourText.toInt()
    val minute = minuteText.toInt()
    // 60 is the leap second the Internet Message Format allows, and it reads as the next minute.
    val second = secondText.toInt()
    if (day !in 1..daysIn(year, month) || hour > 23 || minute > 59 || second > 60) return null
    val seconds = daysFromEpoch(year, month, day) * SECONDS_PER_DAY + hour * 3_600L + minute * 60L + second
    return Instant.fromEpochSeconds(seconds)
}

private fun daysIn(
    year: Int,
    month: Int,
): Int =
    when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

/** Days from 1970-01-01 to a date of the proleptic Gregorian calendar, counted in 400-year eras. */
private fun daysFromEpoch(
    year: Int,
    month: Int,
    day: Int,
): Long {
    val shifted = if (month <= 2) year - 1 else year
    val era = (if (shifted >= 0) shifted else shifted - 399) / 400
    val yearOfEra = shifted - era * 400
    val dayOfYear = (153 * (if (month > 2) month - 3 else month + 9) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097L + dayOfEra - 719_468L
}

/** The UTC year [instant] falls in, the inverse of [daysFromEpoch] read as far as the year. */
private fun yearOf(instant: Instant): Int {
    val days = instant.epochSeconds.floorDiv(SECONDS_PER_DAY) + 719_468L
    val era = (if (days >= 0) days else days - 146_096L) / 146_097L
    val dayOfEra = days - era * 146_097L
    val yearOfEra = (dayOfEra - dayOfEra / 1_460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val shiftedMonth = (5 * dayOfYear + 2) / 153
    val year = yearOfEra + era * 400
    return (if (shiftedMonth >= 10) year + 1 else year).toInt()
}
