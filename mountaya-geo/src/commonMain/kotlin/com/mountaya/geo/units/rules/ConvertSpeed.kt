package com.mountaya.geo

/**
 * convertSpeed converts [value] from one speed unit to another. A rate is its length over its
 * time and a pace its time over its length, each through the exact values of its parts, so a
 * conversion between two of one form scales by the ratio of the parts and one across the forms
 * takes the reciprocal: 4.5 km/h is 13.333 minutes per kilometer.
 *
 * A rate of zero is a pace of infinity and a pace of zero a rate of infinity, as the division
 * answers; nothing is clamped.
 */
public fun convertSpeed(
    value: Double,
    from: SpeedUnit,
    to: SpeedUnit,
): Double {
    val sourceLength = from.length.tenthsOfMillimeter
    val sourceTime = from.time.milliseconds
    val targetLength = to.length.tenthsOfMillimeter
    val targetTime = to.time.milliseconds
    return when {
        from.isPace && to.isPace -> scaleByRatio(value, sourceTime * targetLength, sourceLength * targetTime)
        !from.isPace && !to.isPace -> scaleByRatio(value, sourceLength * targetTime, sourceTime * targetLength)
        from.isPace -> invertByRatio(value, sourceLength * targetTime, sourceTime * targetLength)
        else -> invertByRatio(value, sourceTime * targetLength, sourceLength * targetTime)
    }
}

/** Whether the unit is a pace, a time per distance, rather than a rate. */
internal val SpeedUnit.isPace: Boolean
    get() = this == SpeedUnit.MinutePerKilometer || this == SpeedUnit.MinutePerMile

/** The length the unit is stated over. */
private val SpeedUnit.length: LengthUnit
    get() =
        when (this) {
            SpeedUnit.KilometerPerHour, SpeedUnit.MinutePerKilometer -> LengthUnit.Kilometer
            SpeedUnit.MeterPerSecond -> LengthUnit.Meter
            SpeedUnit.MilePerHour, SpeedUnit.MinutePerMile -> LengthUnit.Mile
        }

/** The time the unit is stated over. */
private val SpeedUnit.time: DurationUnit
    get() =
        when (this) {
            SpeedUnit.KilometerPerHour, SpeedUnit.MilePerHour -> DurationUnit.Hour
            SpeedUnit.MeterPerSecond -> DurationUnit.Second
            SpeedUnit.MinutePerKilometer, SpeedUnit.MinutePerMile -> DurationUnit.Minute
        }
