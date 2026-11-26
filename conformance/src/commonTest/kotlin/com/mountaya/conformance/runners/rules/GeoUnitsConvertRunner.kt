package com.mountaya.conformance

import com.mountaya.geo.AngleUnit
import com.mountaya.geo.DataSizeUnit
import com.mountaya.geo.DurationUnit
import com.mountaya.geo.LengthUnit
import com.mountaya.geo.ProportionUnit
import com.mountaya.geo.SpeedUnit
import com.mountaya.geo.TemperatureUnit
import com.mountaya.geo.convertAngle
import com.mountaya.geo.convertDataSize
import com.mountaya.geo.convertDuration
import com.mountaya.geo.convertLength
import com.mountaya.geo.convertProportion
import com.mountaya.geo.convertSpeed
import com.mountaya.geo.convertTemperature
import kotlinx.coroutines.test.TestScope

/**
 * `geo.units.convert`: the case's `value` from one unit to another of its `quantity`, each unit
 * read by its contract id, through the conversion Geo publishes for that quantity.
 *
 * The fixture's `constants` are held to the conversions themselves, each defining value read back
 * as the conversion of one unit, so a factor the rules state is the factor the fixture declares.
 */
object GeoUnitsConvertRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, declared)
        val input = case.fields
        val value = input.number("value")
        val from = input.text("from")
        val to = input.text("to")

        // The case's value from the unit spelled `from` to the one spelled `to`, both among [entries].
        fun <T> convert(
            entries: List<T>,
            idOf: (T) -> String,
            conversion: (Double, T, T) -> Double,
        ): Double = conversion(value, unit(entries, from, idOf), unit(entries, to, idOf))

        val converted =
            when (val quantity = input.text("quantity")) {
                "length" -> convert(LengthUnit.entries, LengthUnit::id, ::convertLength)
                "speed" -> convert(SpeedUnit.entries, SpeedUnit::id, ::convertSpeed)
                "duration" -> convert(DurationUnit.entries, DurationUnit::id, ::convertDuration)
                "temperature" -> convert(TemperatureUnit.entries, TemperatureUnit::id, ::convertTemperature)
                "angle" -> convert(AngleUnit.entries, AngleUnit::id, ::convertAngle)
                "data" -> convert(DataSizeUnit.entries, DataSizeUnit::id, ::convertDataSize)
                "proportion" -> convert(ProportionUnit.entries, ProportionUnit::id, ::convertProportion)
                else -> throw AssertionError("no quantity is called `$quantity`")
            }
        return ConformanceOutcome.Value(FixtureValue.Number(converted))
    }

    private val declared: Map<String, Double> by lazy {
        mapOf(
            "bitsPerByte" to convertDataSize(1.0, DataSizeUnit.Byte, DataSizeUnit.Bit),
            "bitsPerKilobit" to convertDataSize(1.0, DataSizeUnit.Kilobit, DataSizeUnit.Bit),
            "bitsPerMegabit" to convertDataSize(1.0, DataSizeUnit.Megabit, DataSizeUnit.Bit),
            "bytesPerKibibyte" to convertDataSize(1.0, DataSizeUnit.Kibibyte, DataSizeUnit.Byte),
            "degreesPerRadian" to convertAngle(1.0, AngleUnit.Radian, AngleUnit.Degree),
            "fahrenheitOffset" to convertTemperature(0.0, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit),
            "fahrenheitPerCelsius" to
                (
                    convertTemperature(100.0, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit) -
                        convertTemperature(0.0, TemperatureUnit.Celsius, TemperatureUnit.Fahrenheit)
                ) / 100.0,
            "hoursPerDay" to convertDuration(1.0, DurationUnit.Day, DurationUnit.Hour),
            "kibibytesPerMebibyte" to convertDataSize(1.0, DataSizeUnit.Mebibyte, DataSizeUnit.Kibibyte),
            "metersPerCentimeter" to convertLength(1.0, LengthUnit.Centimeter, LengthUnit.Meter),
            "metersPerFoot" to convertLength(1.0, LengthUnit.Foot, LengthUnit.Meter),
            "metersPerInch" to convertLength(1.0, LengthUnit.Inch, LengthUnit.Meter),
            "metersPerKilometer" to convertLength(1.0, LengthUnit.Kilometer, LengthUnit.Meter),
            "metersPerMile" to convertLength(1.0, LengthUnit.Mile, LengthUnit.Meter),
            "millisecondsPerSecond" to convertDuration(1.0, DurationUnit.Second, DurationUnit.Millisecond),
            "minutesPerHour" to convertDuration(1.0, DurationUnit.Hour, DurationUnit.Minute),
            "percentPerRatio" to convertProportion(1.0, ProportionUnit.Ratio, ProportionUnit.Percent),
            "secondsPerHour" to convertDuration(1.0, DurationUnit.Hour, DurationUnit.Second),
            "secondsPerMinute" to convertDuration(1.0, DurationUnit.Minute, DurationUnit.Second),
        )
    }

    private fun <T> unit(
        entries: List<T>,
        id: String,
        idOf: (T) -> String,
    ): T = entries.firstOrNull { idOf(it) == id } ?: throw AssertionError("no unit of the quantity is called `$id`")
}
