package com.mountaya.conformance

import com.mountaya.geo.TimeSliderTick
import com.mountaya.geo.timeSliderRelease
import com.mountaya.geo.timeSliderTicks
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.time-slider`: what a slider draws and commits over the track the case's `nowMs` lays out, as `LayersPanel`
 * draws it: the thumb at the stop `valueAt` places the `pinned` instant at, the fill from the stop of the reading, the
 * ticks of `timeSliderTicks`, the slider held under `layers.timeUnavailable` on a track of no instant, and what
 * `timeSliderRelease` commits for a release on the stop `release`. A clock that reads no instant, NaN, is refused by
 * Kotlin's types, and lays out the track of no instant, as `geo.layers.time-window`'s runner reads it. Geo keeps the
 * two constants to itself, so each is read back through what the rule answers: the stops of a day as the distance
 * between two ticks, and each labelled day as the days from the stop of the reading to a tick carrying a word.
 */
object GeoLayersTimeSliderRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val window = TimeFixtures.window(TimeFixtures.reading(input["nowMs"]))
        val ticks = timeSliderTicks(window)
        val dayStops = ticks[1].value - ticks[0].value
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "dayStops" to FixtureValue.Number(dayStops.toDouble()),
                    "labelledDays" to
                        FixtureValue.Array(
                            ticks.mapNotNull { tick ->
                                val word = tick.word ?: return@mapNotNull null
                                val days = (tick.value - window.nowValue) / dayStops
                                FixtureValue.Object(
                                    listOf(
                                        "days" to FixtureValue.Number(days.toDouble()),
                                        "word" to FixtureValue.Text(word),
                                    ),
                                )
                            },
                        ),
                ),
        )
        val release = input.number("release").toInt()
        val released = timeSliderRelease(window, release)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "fillFrom" to FixtureValue.Number(window.nowValue.toDouble()),
                    "held" to
                        if (window.start == null) FixtureValue.Text("layers.timeUnavailable") else FixtureValue.Null,
                    "released" to
                        (
                            released?.let { FixtureValue.Object(listOf("pin" to TimeFixtures.written(it.pin))) }
                                ?: FixtureValue.Null
                        ),
                    "ticks" to FixtureValue.Array(ticks.map(::written)),
                    "value" to
                        FixtureValue.Number(
                            window.valueAt(TimeFixtures.instant(input.textOrNull("pinned"))).toDouble(),
                        ),
                ),
            ),
        )
    }

    /** A tick as the fixture writes one. */
    private fun written(tick: TimeSliderTick): FixtureValue =
        FixtureValue.Object(
            listOf(
                "value" to FixtureValue.Number(tick.value.toDouble()),
                "word" to (tick.word?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
            ),
        )
}
