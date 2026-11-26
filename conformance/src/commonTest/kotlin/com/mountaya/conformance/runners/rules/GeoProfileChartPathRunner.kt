package com.mountaya.conformance

import com.mountaya.geo.PlotCommand
import com.mountaya.geo.profileAreaPath
import com.mountaya.geo.profilePolyline
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.chart-path`: the line the case's `series` is drawn as against its `domain`, over its `window`, as
 * points, and the area under it as commands, `["M", x, y]`, `["L", x, y]` and `["Z"]`.
 */
object GeoProfileChartPathRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val series = fixtureSeries(input["series"])
        val domain = fixtureDomain(input["domain"])
        val window = fixtureWindow(input["window"])
        val line = profilePolyline(series, domain, window).map { numbersValue(listOf(it.x, it.y)) }
        val area =
            profileAreaPath(series, domain, window).map { command ->
                when (command) {
                    is PlotCommand.Move -> {
                        FixtureValue.Array(
                            listOf(
                                FixtureValue.Text("M"),
                                FixtureValue.Number(command.point.x),
                                FixtureValue.Number(command.point.y),
                            ),
                        )
                    }

                    is PlotCommand.Line -> {
                        FixtureValue.Array(
                            listOf(
                                FixtureValue.Text("L"),
                                FixtureValue.Number(command.point.x),
                                FixtureValue.Number(command.point.y),
                            ),
                        )
                    }

                    PlotCommand.Close -> {
                        FixtureValue.Array(listOf(FixtureValue.Text("Z")))
                    }
                }
            }
        return ConformanceOutcome.Value(
            objectValue(
                "area" to FixtureValue.Array(area),
                "line" to FixtureValue.Array(line),
            ),
        )
    }
}
