package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.cursor-readout`: what a profile states about the sample at the case's `index` of its `series`, in
 * the order its `stats` ask, among its classified `stretches`. The default figures, the noise threshold and the
 * grade window are held to the published ones.
 */
object GeoProfileCursorReadoutRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(
            case,
            mapOf(
                "defaultStats" to profileTexts(defaultReadoutStats.map { it.id }),
                "elevationNoiseThresholdMeters" to FixtureValue.Number(ELEVATION_NOISE_THRESHOLD_METERS),
                "gradeWindowMeters" to FixtureValue.Number(GRADE_WINDOW_METERS),
            ),
        )
        val input = case.fields
        val series = profileSeries(input["series"])
        val index = input.number("index").toInt()
        val point = series.points.getOrNull(index) ?: throw AssertionError("the series holds no sample $index")
        val stretches =
            (input["stretches"] as? FixtureValue.Array)?.items.orEmpty().map { item ->
                val entry = item as? FixtureValue.Object ?: throw AssertionError("a stretch is not an object")
                ProfileClass(entry.number("from"), entry.number("to"), entry.text("color"), entry.text("name"))
            }
        val stats = profileFigures(input, "stats", ReadoutStat.entries) { it.id } ?: defaultReadoutStats
        val readout = profileReadout(series, point, stats, stretches)
        return ConformanceOutcome.Value(
            profileObject(
                "cells" to
                    FixtureValue.Array(
                        readout.cells.map {
                            profileObject(
                                "key" to FixtureValue.Text(it.stat.id),
                                "value" to FixtureValue.Number(it.value),
                            )
                        },
                    ),
                "grade" to profileNumber(readout.grade),
                "stretch" to
                    (
                        readout.stretch?.let {
                            profileObject("color" to FixtureValue.Text(it.color), "name" to FixtureValue.Text(it.label))
                        } ?: FixtureValue.Null
                    ),
            ),
        )
    }
}
