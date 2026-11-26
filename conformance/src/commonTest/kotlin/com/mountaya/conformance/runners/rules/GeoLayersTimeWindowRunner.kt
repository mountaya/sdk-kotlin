package com.mountaya.conformance

import com.mountaya.geo.TIME_WINDOW_FUTURE_HOURS
import com.mountaya.geo.TIME_WINDOW_PAST_HOURS
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.time-window`: the track `timeSliderWindow` lays out around the case's `nowMs`, its start, its stop of the
 * reading and its last stop, the instant each of its `stops` stands for and the stop each of its `instants` is placed at,
 * a pin that names no instant placed as none. A clock that reads no instant, NaN, is refused by Kotlin's types: the track
 * it lays out is the track of no instant, which the rule lays out where the track would end past the range every SDK
 * holds one to. The two constants are held to the ones Geo publishes.
 */
object GeoLayersTimeWindowRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules =
                mapOf(
                    "pastHours" to TIME_WINDOW_PAST_HOURS.toDouble(),
                    "futureHours" to TIME_WINDOW_FUTURE_HOURS.toDouble(),
                ),
        )
        val input = case.fields
        val window = TimeFixtures.window(TimeFixtures.reading(input["nowMs"]))
        val stops =
            input.items("stops").map {
                (it as? FixtureValue.Number)?.value?.toInt() ?: throw AssertionError("a stop is no number")
            }
        val instants = input.items("instants").map { TimeFixtures.instant((it as? FixtureValue.Text)?.value) }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "instantsAt" to FixtureValue.Array(stops.map { TimeFixtures.written(window.dateAt(it)) }),
                    "maxValue" to FixtureValue.Number(window.maxValue.toDouble()),
                    "nowValue" to FixtureValue.Number(window.nowValue.toDouble()),
                    "startMs" to TimeFixtures.written(window.start),
                    "valuesOf" to
                        FixtureValue.Array(instants.map { FixtureValue.Number(window.valueAt(it).toDouble()) }),
                ),
            ),
        )
    }
}
