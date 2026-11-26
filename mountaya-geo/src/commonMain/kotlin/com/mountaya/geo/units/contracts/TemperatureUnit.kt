package com.mountaya.geo

/**
 * TemperatureUnit is a scale a temperature is read on, each spelled by its contract unit id, the
 * ECMA-402 identifier and the value of the reader's temperature preference.
 */
public enum class TemperatureUnit(
    /** The contract's unit id, as `geo/units-convert.json` spells it. */
    public val id: String,
) {
    /** A reading in degrees Celsius. */
    Celsius("celsius"),

    /** A reading in degrees Fahrenheit: nine fifths of the Celsius reading, plus 32. */
    Fahrenheit("fahrenheit"),
}
