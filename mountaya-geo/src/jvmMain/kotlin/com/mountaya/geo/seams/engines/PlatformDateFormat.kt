package com.mountaya.geo

import java.text.Format
import java.util.Locale

/**
 * The formatter the jvm writes an instant with, for [locale] in the zone the IANA name [timeZone] names, at
 * [dateLength] and [timeLength]: `java.text`'s, [javaTextDateFormat], which reads no setting of the machine it
 * runs on, so a time keeps the locale's own clock. Android declares its own in `IcuDateFormat.kt`, since the
 * file that asks for it, `LocaleDateText.kt`, is held alike in both source sets.
 */
internal fun platformDateFormat(
    locale: Locale,
    timeZone: String,
    dateLength: DateLength?,
    timeLength: TimeLength?,
): Format = javaTextDateFormat(locale, timeZone, dateLength, timeLength)
