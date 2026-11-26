package com.mountaya.conformance

import com.mountaya.geo.timeSliderReadout
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.time-readout`: what `timeSliderReadout` reads at each of the case's `values`, the stops of the track its
 * `nowMs` lays out: the instant in epoch milliseconds, or null, and the offset's word by its path with its count. A
 * clock that reads no instant, NaN, is refused by Kotlin's types, and lays out the track of no instant, as
 * `geo.layers.time-window`'s runner reads it.
 */
object GeoLayersTimeReadoutRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val window = TimeFixtures.window(TimeFixtures.reading(input["nowMs"]))
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                input.items("values").map { item ->
                    val value =
                        (item as? FixtureValue.Number)?.value?.toInt() ?: throw AssertionError("a stop is no number")
                    val readout = timeSliderReadout(window, value)
                    FixtureValue.Object(
                        listOf(
                            "instant" to TimeFixtures.written(readout.instant),
                            "offset" to
                                FixtureValue.Object(
                                    listOf(
                                        "count" to
                                            (
                                                readout.count?.let { FixtureValue.Number(it.toDouble()) }
                                                    ?: FixtureValue.Null
                                            ),
                                        "word" to FixtureValue.Text(readout.word),
                                    ),
                                ),
                        ),
                    )
                },
            ),
        )
    }
}
