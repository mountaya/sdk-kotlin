package com.mountaya.geo

/**
 * convertTemperature converts [value] from one temperature scale to the other: a Fahrenheit
 * reading is nine fifths of the Celsius one plus 32, computed as a product by nine and a quotient
 * by five rather than through a rounded 1.8. A value on its own scale is returned as it is.
 */
public fun convertTemperature(
    value: Double,
    from: TemperatureUnit,
    to: TemperatureUnit,
): Double =
    when {
        from == to -> value
        to == TemperatureUnit.Fahrenheit -> value * 9.0 / 5.0 + FAHRENHEIT_OFFSET
        else -> (value - FAHRENHEIT_OFFSET) * 5.0 / 9.0
    }

/** The Fahrenheit reading of the Celsius zero. */
private const val FAHRENHEIT_OFFSET = 32.0
