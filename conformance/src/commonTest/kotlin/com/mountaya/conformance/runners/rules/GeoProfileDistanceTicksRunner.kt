package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.DEFAULT_DISTANCE_TICK_COUNT
import com.mountaya.geo.distanceTicks
import kotlinx.coroutines.test.TestScope

/** `geo.profile.distance-ticks`: the stops a distance axis over the case's `range` is labelled at. */
object GeoProfileDistanceTicksRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("defaultTarget" to DEFAULT_DISTANCE_TICK_COUNT.toDouble()))
        val input = case.fields
        val range = input.objectOrNull("range") ?: throw AssertionError("the case has no `range`")
        val length = RestSchema.UnitLength(input.text("length"))
        val target = input.numberOrNull("target")
        val ticks =
            if (target == null) {
                distanceTicks(range.number("from"), range.number("to"), length)
            } else {
                distanceTicks(range.number("from"), range.number("to"), length, target.toInt())
            }
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                ticks.map { tick ->
                    FixtureValue.Object(
                        listOf(
                            "fractionDigits" to FixtureValue.Number(tick.fractionDigits.toDouble()),
                            "meters" to FixtureValue.Number(tick.meters),
                            "value" to FixtureValue.Number(tick.value),
                        ),
                    )
                },
            ),
        )
    }
}
