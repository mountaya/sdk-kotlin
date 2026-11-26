package com.mountaya.geo

/**
 * DateLength is how much of a date a reader is shown, as the four lengths every platform's locale
 * services name a date by: `java.text.DateFormat`'s styles, ICU's and Foundation's
 * `DateFormatter.Style`. The fields, their order, their words and their digits are each the locale's
 * own, and a date written beside a time of day is joined to it as the locale's data joins them.
 */
public enum class DateLength {
    /** Digits alone, `3/14/26` in American English. */
    Short,

    /** The month abbreviated, `Mar 14, 2026` in American English. */
    Medium,

    /** The month in words, `March 14, 2026` in American English and `14 mars 2026` in French. */
    Long,

    /** The weekday and the month in words, `Saturday, March 14, 2026` in American English. */
    Full,
}
