package com.mountaya.conformance

import com.mountaya.geo.AngleUnit
import com.mountaya.geo.METERS_PER_DEGREE_MEAN_SPHERE
import com.mountaya.geo.TerrainNeighbours
import com.mountaya.geo.convertAngle
import com.mountaya.geo.slopeAspectFromNeighbours
import kotlinx.coroutines.test.TestScope
import kotlin.math.PI

/**
 * `geo.terrain.slope-aspect`: the slope and the aspect at the case's `latitude` from its four
 * `neighbours` one `spacingDegrees` away, the aspect null on flat ground. The fixture's mean-sphere
 * degree is held to `METERS_PER_DEGREE_MEAN_SPHERE`, and its turn to the angle conversion of two pi.
 */
object GeoTerrainSlopeAspectRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "degreesPerTurn" to convertAngle(2.0 * PI, AngleUnit.Radian, AngleUnit.Degree),
                "metersPerDegreeMeanSphere" to METERS_PER_DEGREE_MEAN_SPHERE,
            ),
        )
        val input = case.fields
        val sides = input.objectOrNull("neighbours") ?: throw AssertionError("`neighbours` is absent")
        val neighbours =
            TerrainNeighbours(
                north = sides.number("north"),
                south = sides.number("south"),
                east = sides.number("east"),
                west = sides.number("west"),
            )
        val answer = slopeAspectFromNeighbours(neighbours, input.number("latitude"), input.number("spacingDegrees"))
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "aspect" to (answer.aspect?.let { FixtureValue.Number(it) } ?: FixtureValue.Null),
                    "slope" to FixtureValue.Number(answer.slope),
                ),
            ),
        )
    }
}
