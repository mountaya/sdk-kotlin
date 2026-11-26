package com.mountaya.conformance

import com.mountaya.geo.AngleUnit
import com.mountaya.geo.SlopeUnit
import com.mountaya.geo.convertAngle
import com.mountaya.geo.convertSlope
import kotlinx.coroutines.test.TestScope

/**
 * `geo.units.slope`: the case's `value` from one slope unit to another, each read by its contract
 * id, through `convertSlope`. The fixture's `constants` are held to the conversions that state them.
 */
object GeoUnitsSlopeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "degreesPerRadian" to convertAngle(1.0, AngleUnit.Radian, AngleUnit.Degree),
                "percentPerRatio" to convertSlope(1.0, SlopeUnit.Ratio, SlopeUnit.Percentage),
            ),
        )
        val input = case.fields
        val converted = convertSlope(input.number("value"), unit(input.text("from")), unit(input.text("to")))
        return ConformanceOutcome.Value(FixtureValue.Number(converted))
    }

    private fun unit(id: String): SlopeUnit =
        SlopeUnit.entries.firstOrNull { it.id == id } ?: throw AssertionError("no slope unit is called `$id`")
}
