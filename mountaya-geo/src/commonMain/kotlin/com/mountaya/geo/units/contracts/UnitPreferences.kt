package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * UnitPreferences is how a reader wants measurements stated: the language they read, and four
 * preferences the wire carries, each independent of the others. Every formatter reads its own:
 * an imperial length beside a Celsius scale renders its temperatures in Celsius, since temperature
 * is never derived from length.
 *
 * A preference the contract adds after this build, or one stored before it, reads as the default
 * its formatter names: an unknown system is metric, an unknown pace per time, an unknown slope
 * unit degrees and an unknown scale Celsius, so a formatter states it rather than failing.
 */
public class UnitPreferences(
    /**
     * The reader's language, a BCP 47 language tag such as `fr-CA`: it chooses the digits, the
     * decimal mark and the words around a number. An underscore parts subtags as a hyphen does, and
     * a value that is no language tag reads as English. Always the caller's, never a platform's
     * default.
     */
    public val language: String,
    /** The measurement system lengths are stated in: kilometers and meters, or miles and feet. */
    public val length: RestSchema.UnitLength = RestSchema.UnitLength.Metric,
    /** Whether a travel rate is stated per time, `km/h`, or per distance, `min/km`. */
    public val pace: RestSchema.UnitPace = RestSchema.UnitPace.PerTime,
    /** Whether a slope is stated in degrees or as a percentage grade. */
    public val slope: RestSchema.UnitSlope = RestSchema.UnitSlope.Degrees,
    /** The scale a temperature is stated in. */
    public val temperature: RestSchema.UnitTemperature = RestSchema.UnitTemperature.Celsius,
) {
    override fun equals(other: Any?): Boolean =
        other is UnitPreferences &&
            other.language == language &&
            other.length == length &&
            other.pace == pace &&
            other.slope == slope &&
            other.temperature == temperature

    override fun hashCode(): Int =
        (((language.hashCode() * 31 + length.hashCode()) * 31 + pace.hashCode()) * 31 + slope.hashCode()) * 31 +
            temperature.hashCode()

    override fun toString(): String =
        "UnitPreferences(language=$language, length=$length, pace=$pace, slope=$slope, temperature=$temperature)"
}
