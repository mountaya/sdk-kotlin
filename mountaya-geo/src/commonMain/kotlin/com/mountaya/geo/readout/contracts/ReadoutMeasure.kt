package com.mountaya.geo

/**
 * ReadoutMeasure is what a readout's band is written in (`geo.readout.rows`), each by the formatter of
 * `geo.profile.unit-formatting` that writes it: a temperature in degrees Celsius, a humidity in percent, a snowfall or
 * a snow depth in centimeters, a visibility as a distance in meters, and a wind as a speed in kilometers per hour.
 */
public enum class ReadoutMeasure(
    /** The measure as the contract spells it. */
    public val id: String,
) {
    /** A temperature, written by `formatTemperature`. */
    Temperature("temperature"),

    /** A relative humidity, written by `formatHumidity`. */
    Humidity("humidity"),

    /** A depth of snow, written by `formatSnowfall`. */
    Snowfall("snowfall"),

    /** A distance, written by `formatDistance`. */
    Distance("distance"),

    /** A speed, written by `formatSpeed`. */
    Speed("speed"),
}
