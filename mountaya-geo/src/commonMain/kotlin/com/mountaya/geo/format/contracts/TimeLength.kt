package com.mountaya.geo

/**
 * TimeLength is how much of a time of day a reader is shown, as the two shortest lengths every
 * platform's locale services name a time by: `java.text.DateFormat`'s styles, ICU's and Foundation's
 * `DateFormatter.Style`. The fields, their order, their words, their digits and the hour cycle, 12 or
 * 24 hours, are each the locale's own, whatever clock the device is set to. A time of day names no
 * zone, so there is no long or full length: those styles name the zone, in words each platform
 * chooses.
 */
public enum class TimeLength {
    /** The hour and the minute, `13:00` in German and `오후 1:00` in Korean. */
    Short,

    /** The second too, `13:00:00` in German and `9:00:00` in Japanese. */
    Medium,
}
