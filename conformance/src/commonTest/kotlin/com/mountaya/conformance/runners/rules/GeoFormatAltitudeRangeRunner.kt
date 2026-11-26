package com.mountaya.conformance

import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.formatAltitudeRange
import com.mountaya.geo.formatElevation
import kotlinx.coroutines.test.TestScope

/**
 * `geo.format.altitude-range`: how a band from the case's `min` to its `max` is written for its `units`.
 *
 * The fixture's `rangePatterns` are held to the ones the rule writes, language by language, value for value,
 * each read through the public surface: a language's elevation of 1 m gives the words around a number, and its
 * band from 1 m to 2 m and its band of 1 m alone, those words taken off, give its range and its approximately
 * patterns, `{0}` and `{1}` standing where the numbers were.
 */
object GeoFormatAltitudeRangeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val declared =
            case.root("constants").objectOrNull("rangePatterns")
                ?: throw AssertionError("the fixture declares no `rangePatterns`")
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values = mapOf("rangePatterns" to writtenPatterns(declared.keys)),
        )
        val input = case.fields
        val text =
            formatAltitudeRange(input.numberOrNull("min"), input.numberOrNull("max"), readerUnits(input["units"]))
        return ConformanceOutcome.Value(FixtureValue.Text(text))
    }

    /** The range and approximately patterns the rule writes for each of [languages], as the fixture states them. */
    private fun writtenPatterns(languages: List<String>): FixtureValue =
        FixtureValue.Object(
            languages.map { language ->
                val units = UnitPreferences(language)
                val single = formatElevation(1.0, units)
                val at = single.indexOf('1')
                if (at < 0 || single.indexOf('1', at + 1) >= 0) {
                    throw AssertionError(
                        "an elevation of 1 m in `$language` is written `$single`, which holds no one 1",
                    )
                }
                val before = single.substring(0, at)
                val after = single.substring(at + 1)

                fun pattern(written: String): String {
                    if (!written.startsWith(before) || !written.endsWith(after)) {
                        throw AssertionError(
                            "a band in `$language` is written `$written`, not in a measurement's words",
                        )
                    }
                    val inner = written.substring(before.length, written.length - after.length)
                    return inner.replaceFirst("1", "{0}").replaceFirst("2", "{1}")
                }
                language to
                    FixtureValue.Object(
                        listOf(
                            "approximately" to FixtureValue.Text(pattern(formatAltitudeRange(1.0, 1.0, units))),
                            "range" to FixtureValue.Text(pattern(formatAltitudeRange(1.0, 2.0, units))),
                        ),
                    )
            },
        )
}
