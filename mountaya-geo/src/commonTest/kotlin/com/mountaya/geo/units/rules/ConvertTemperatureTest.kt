package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertTemperatureTest {
    @Test
    fun aFahrenheitReadingIsNineFifthsOfTheCelsiusOnePlusThirtyTwo() {
        assertEquals(32.0, convertTemperature(0.0, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit))
        assertEquals(212.0, convertTemperature(100.0, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit))
        assertEquals(-40.0, convertTemperature(-40.0, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit))
        assertEquals(-40.0, convertTemperature(-40.0, TemperatureUnit.Fahrenheit, TemperatureUnit.Celsius))
        assertEquals(37.0, convertTemperature(98.6, TemperatureUnit.Fahrenheit, TemperatureUnit.Celsius), 1e-12)
    }

    /** Nine and five rather than a rounded 1.8, so a whole Celsius reading lands on its exact figure. */
    @Test
    fun aWholeCelsiusReadingConvertsExactly() {
        assertEquals(98.6, convertTemperature(37.0, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit))
        assertEquals(37.0, convertTemperature(98.6, TemperatureUnit.Fahrenheit, TemperatureUnit.Celsius))
    }

    @Test
    fun aReadingOnItsOwnScaleIsReturnedAsItIs() {
        for (unit in TemperatureUnit.entries) assertEquals(0.1, convertTemperature(0.1, unit, unit))
    }
}
