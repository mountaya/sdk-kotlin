package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/** The places a rate is stated to. */
private const val RATE_FRACTION_DIGITS = 1

/**
 * formatSpeed states a travel rate of [kmh], kilometers per hour as the wire carries every rate,
 * for the reader [units] describes: its `length` picks the system and its `pace` the form. Per
 * time, it is a rate to one decimal, `4.5 km/h`, or `2,8 mi/h` for a French reader of miles. Per
 * distance, it is the reciprocal as the pace clock [formatPaceClock] writes it, the seconds rounded
 * before the split, `13:20 min/km` or `21:27 min/mi`.
 *
 * A rate that rounds to zero is stated with no sign, `0 km/h`. The empty string for a rate that is
 * missing or no finite number, and per distance for one of zero or less, which has no reciprocal a
 * reader can read. A pace form other than per distance reads as per time.
 */
public fun formatSpeed(
    kmh: Double?,
    units: UnitPreferences,
): String {
    if (kmh == null || !kmh.isFinite()) return ""
    val patterns = unitPatterns(units.language)
    val imperial = isImperial(units.length)
    if (units.pace == RestSchema.UnitPace.PerDistance) {
        if (kmh <= 0.0) return ""
        val unit = if (imperial) SpeedUnit.MinutePerMile else SpeedUnit.MinutePerKilometer
        val clock = formatPaceClock(convertSpeed(kmh, SpeedUnit.KilometerPerHour, unit))
        if (clock.isEmpty()) return ""
        return (if (imperial) patterns.minutePerMile else patterns.minutePerKilometer).replace(
            NUMBER_PLACEHOLDER,
            clock,
        )
    }
    return if (imperial) {
        formatMeasurement(
            convertSpeed(kmh, SpeedUnit.KilometerPerHour, SpeedUnit.MilePerHour),
            RATE_FRACTION_DIGITS,
            patterns.milePerHour,
            units.language,
            patterns,
        )
    } else {
        formatMeasurement(kmh, RATE_FRACTION_DIGITS, patterns.kilometerPerHour, units.language, patterns)
    }
}
