package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.stat-strip`: what the stat strip states for the case's `characteristics` and `cursor`, in the order
 * its `stats` ask. The default figures are held to the published ones.
 */
object GeoProfileStatStripRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(case, mapOf("defaultStats" to profileTexts(defaultStatStripStats.map { it.id })))
        val input = case.fields
        val figures =
            input.objectOrNull("characteristics")
                ?: throw AssertionError("the case's `characteristics` is not an object")
        val characteristics =
            ResolvedCharacteristics(
                distance = figures.numberOrNull("distance"),
                duration = figures.numberOrNull("duration"),
                ascent = figures.numberOrNull("ascent"),
                descent = figures.numberOrNull("descent"),
                altitudeMin = figures.numberOrNull("altitudeMin"),
                altitudeMax = figures.numberOrNull("altitudeMax"),
                slopeMin = figures.numberOrNull("slopeMin"),
                slopeMax = figures.numberOrNull("slopeMax"),
                trailDifficultyMin =
                    figures
                        .textOrNull(
                            "trailDifficultyMin",
                        )?.let { RestSchema.TrailDifficultyType(it) },
                trailDifficultyMax =
                    figures
                        .textOrNull(
                            "trailDifficultyMax",
                        )?.let { RestSchema.TrailDifficultyType(it) },
            )
        val cursor =
            input.objectOrNull("cursor")?.let { stands ->
                StatStripCursor(stands.number("distance"), stands.number("elevation"), stands.numberOrNull("grade"))
            }
        val stats = profileFigures(input, "stats", StatStripStat.entries) { it.id } ?: defaultStatStripStats
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                statStripCells(characteristics, cursor, stats).map { cell ->
                    val value =
                        if (cell is StatStripCell.Difficulty) {
                            FixtureValue.Text(
                                cell.difficulty.value,
                            )
                        } else {
                            profileNumber(cell.number)
                        }
                    profileObject("key" to FixtureValue.Text(cell.stat.id), "value" to value)
                },
            ),
        )
    }
}
