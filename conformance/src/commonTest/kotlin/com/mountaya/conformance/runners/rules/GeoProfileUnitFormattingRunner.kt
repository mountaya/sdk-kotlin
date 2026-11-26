package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.LengthUnit
import com.mountaya.geo.SlopeUnit
import com.mountaya.geo.SpeedUnit
import com.mountaya.geo.TemperatureUnit
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.convertLength
import com.mountaya.geo.convertSlope
import com.mountaya.geo.convertSpeed
import com.mountaya.geo.convertTemperature
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatDuration
import com.mountaya.geo.formatElevation
import com.mountaya.geo.formatHumidity
import com.mountaya.geo.formatSlope
import com.mountaya.geo.formatSnowfall
import com.mountaya.geo.formatSpeed
import com.mountaya.geo.formatTemperature
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.unit-formatting`: the case's `value` as a reader of its `units` is shown it, by the
 * formatter of the `measure` it names, `distance`, `elevation`, `slope`, `speed`, `temperature`,
 * `duration`, `humidity` or `snowfall`. The lengths a unit is defined by are held to the conversions, and each preference's
 * vocabulary to the values the wire's preference names.
 *
 * The table of unit patterns Geo writes from is `internal`, so each declared pattern is read back
 * through what the published formatters write, language by language: a measurement of 5 of the unit, a
 * snowfall of 5 cm and of 5 in among them,
 * a duration of 2 hours and one of 5 minutes, and a pace of 5 minutes, each number written in place of
 * `{0}`; the minimum grouping digits are read off the elevations 1531 and 15,310, the widths off the
 * units Geo writes narrow, and the rounding off 2.5°, -2.5° and 850 m.
 */
object GeoProfileUnitFormattingRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val declared = case.root("constants")
        val languages =
            declared.objectOrNull("unitPatterns")?.keys
                ?: throw AssertionError("the fixture declares no `unitPatterns`")
        val written = languages.associateWith(::writtenPatterns)
        holdDeclaredConstants(
            case,
            rules =
                mapOf(
                    "centimetersPerInch" to convertLength(1.0, LengthUnit.Inch, LengthUnit.Centimeter),
                    "kilometersPerMile" to convertLength(1.0, LengthUnit.Mile, LengthUnit.Kilometer),
                    "metersPerFoot" to convertLength(1.0, LengthUnit.Foot, LengthUnit.Meter),
                    "metersPerMile" to convertLength(1.0, LengthUnit.Mile, LengthUnit.Meter),
                ),
            values =
                mapOf(
                    "unitPatterns" to
                        FixtureValue.Object(
                            written.map { (language, patterns) ->
                                language to
                                    FixtureValue.Object(
                                        patterns.map { (unit, pattern) ->
                                            unit to
                                                FixtureValue.Text(pattern)
                                        },
                                    )
                            },
                        ),
                    "unitPatternWidths" to
                        FixtureValue.Object(
                            written.values.first().keys.map { unit ->
                                unit to
                                    FixtureValue.Text(if (unit in narrowUnits) "narrow" else "short")
                            },
                        ),
                    "minimumGroupingDigits" to
                        FixtureValue.Object(
                            written.map { (language, patterns) ->
                                language to
                                    FixtureValue.Number(groupingDigits(language, patterns.getValue("meter")).toDouble())
                            },
                        ),
                    "roundingMode" to FixtureValue.Text(roundingMode()),
                    "unitLengths" to
                        words(
                            RestSchema.UnitLength.Metric.value,
                            RestSchema.UnitLength.Imperial.value,
                        ),
                    "unitPaces" to
                        words(RestSchema.UnitPace.PerTime.value, RestSchema.UnitPace.PerDistance.value),
                    "unitSlopes" to
                        words(
                            RestSchema.UnitSlope.Degrees.value,
                            RestSchema.UnitSlope.Percentage.value,
                        ),
                    "unitTemperatures" to
                        words(
                            RestSchema.UnitTemperature.Celsius.value,
                            RestSchema.UnitTemperature.Fahrenheit.value,
                        ),
                ),
        )
        val input = case.fields
        val units = fixtureUnits(input.objectOrNull("units"))
        val value = input.numberOrNull("value")
        val text =
            when (val measure = input.text("measure")) {
                "distance" -> formatDistance(value, units)
                "elevation" -> formatElevation(value, units)
                "slope" -> formatSlope(value, units)
                "speed" -> formatSpeed(value, units)
                "temperature" -> formatTemperature(value, units)
                "duration" -> formatDuration(value, units)
                "humidity" -> formatHumidity(value, units)
                "snowfall" -> formatSnowfall(value, units)
                else -> throw AssertionError("`$measure` is no measure a formatter states")
            }
        return ConformanceOutcome.Value(FixtureValue.Text(text))
    }

    private fun words(vararg words: String) = FixtureValue.Array(words.map { FixtureValue.Text(it) })

    /** The units Geo writes at CLDR's narrow width: the degree sign, and a duration's hours and minutes. */
    private val narrowUnits = setOf("degree", "hour", "minute")

    /**
     * The pattern each unit is written in for a reader of [language], by its ECMA-402 unit id, read off
     * what the published formatters write, each number written in place of `{0}`.
     */
    private fun writtenPatterns(language: String): Map<String, String> {
        fun units(
            length: RestSchema.UnitLength = RestSchema.UnitLength.Metric,
            pace: RestSchema.UnitPace = RestSchema.UnitPace.PerTime,
            slope: RestSchema.UnitSlope = RestSchema.UnitSlope.Degrees,
            temperature: RestSchema.UnitTemperature = RestSchema.UnitTemperature.Celsius,
        ) = UnitPreferences(language, length, pace, slope, temperature)

        fun pattern(
            written: String,
            number: String,
        ): String {
            val at = written.indexOf(number)
            if (at < 0 || written.indexOf(number, at + 1) >= 0) {
                throw AssertionError("`$written` does not hold `$number` once, in `$language`")
            }
            return written.replaceFirst(number, "{0}")
        }
        val imperial = RestSchema.UnitLength.Imperial
        val metersPerFoot = convertLength(1.0, LengthUnit.Foot, LengthUnit.Meter)
        val metersPerMile = convertLength(1.0, LengthUnit.Mile, LengthUnit.Meter)
        val kmhPerMph = convertSpeed(1.0, SpeedUnit.MilePerHour, SpeedUnit.KilometerPerHour)
        val fiveDegreesOfGrade = convertSlope(5.0, SlopeUnit.Percentage, SlopeUnit.Degrees)
        val perDistance = RestSchema.UnitPace.PerDistance
        return mapOf(
            "celsius" to pattern(formatTemperature(5.0, units()), "5"),
            "centimeter" to pattern(formatSnowfall(5.0, units()), "5"),
            "degree" to pattern(formatSlope(5.0, units()), "5"),
            "fahrenheit" to
                pattern(
                    formatTemperature(
                        convertTemperature(5.0, TemperatureUnit.Fahrenheit, TemperatureUnit.Celsius),
                        units(temperature = RestSchema.UnitTemperature.Fahrenheit),
                    ),
                    "5",
                ),
            "foot" to pattern(formatElevation(5 * metersPerFoot, units(length = imperial)), "5"),
            "hour" to pattern(formatDuration(7200.0, units()), "2"),
            "inch" to
                pattern(
                    formatSnowfall(
                        convertLength(5.0, LengthUnit.Inch, LengthUnit.Centimeter),
                        units(length = imperial),
                    ),
                    "5",
                ),
            "kilometer" to pattern(formatDistance(5000.0, units()), "5"),
            "kilometer-per-hour" to pattern(formatSpeed(5.0, units()), "5"),
            "meter" to pattern(formatElevation(5.0, units()), "5"),
            "mile" to pattern(formatDistance(5 * metersPerMile, units(length = imperial)), "5"),
            "mile-per-hour" to pattern(formatSpeed(5 * kmhPerMph, units(length = imperial)), "5"),
            "minute" to pattern(formatDuration(300.0, units()), "5"),
            "minute-per-kilometer" to pattern(formatSpeed(12.0, units(pace = perDistance)), "5:00"),
            "minute-per-mile" to
                pattern(formatSpeed(12 * kmhPerMph, units(length = imperial, pace = perDistance)), "5:00"),
            "percent" to pattern(formatSlope(fiveDegreesOfGrade, units(slope = RestSchema.UnitSlope.Percentage)), "5"),
        )
    }

    /**
     * The fewest digits the first group of a number [language] groups holds, as the elevations it writes
     * in its [meter] pattern show: 1 where it groups 1531, 2 where it groups 15,310 and not 1531.
     */
    private fun groupingDigits(
        language: String,
        meter: String,
    ): Int {
        val units = UnitPreferences(language)

        fun grouped(meters: Int): Boolean =
            formatElevation(meters.toDouble(), units) != meter.replace("{0}", meters.toString())
        if (grouped(1531)) return 1
        if (grouped(15310)) return 2
        throw AssertionError("`$language` groups neither 1531 nor 15,310")
    }

    /**
     * The rounding the formatters apply, named as the contract names it: a half rounded away from zero
     * both ways, on the decimal value a reader writes, so 2.5° is `3°`, -2.5° is `-3°` and 850 m is
     * `0.9 km`, though 0.85 is held in binary as a little less.
     */
    private fun roundingMode(): String {
        val english = UnitPreferences("en")
        val halves = listOf(formatSlope(2.5, english), formatSlope(-2.5, english), formatDistance(850.0, english))
        return if (halves ==
            listOf("3°", "-3°", "0.9 km")
        ) {
            "half-away-from-zero"
        } else {
            "none the contract names: $halves"
        }
    }
}

/** The reader [value] writes, `{language, length, pace, slope, temperature}`, each as the wire spells it. */
internal fun fixtureUnits(value: FixtureValue.Object?): UnitPreferences {
    val units = value ?: throw AssertionError("the reader's `units` are absent")
    return UnitPreferences(
        language = units.text("language"),
        length = RestSchema.UnitLength(units.text("length")),
        pace = RestSchema.UnitPace(units.text("pace")),
        slope = RestSchema.UnitSlope(units.text("slope")),
        temperature = RestSchema.UnitTemperature(units.text("temperature")),
    )
}
