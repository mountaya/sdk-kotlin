package com.mountaya.geo

/**
 * SpeedUnit is a unit a travel rate is stated in: a rate, a distance per time, or a pace, a time
 * per distance and the reciprocal of a rate. Each is spelled by its contract unit id, the
 * ECMA-402 identifier, and the wire carries every rate in [KilometerPerHour].
 */
public enum class SpeedUnit(
    /** The contract's unit id, as `geo/units-convert.json` spells it. */
    public val id: String,
) {
    /** A rate in kilometers per hour. */
    KilometerPerHour("kilometer-per-hour"),

    /** A rate in meters per second. */
    MeterPerSecond("meter-per-second"),

    /** A rate in miles per hour. */
    MilePerHour("mile-per-hour"),

    /** A pace in decimal minutes per kilometer. */
    MinutePerKilometer("minute-per-kilometer"),

    /** A pace in decimal minutes per mile. */
    MinutePerMile("minute-per-mile"),
}
