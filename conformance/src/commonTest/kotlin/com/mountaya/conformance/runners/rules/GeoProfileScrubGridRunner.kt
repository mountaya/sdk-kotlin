package com.mountaya.conformance

import com.mountaya.geo.PROFILE_SCRUB_COLUMNS
import com.mountaya.geo.scrubDistance
import com.mountaya.geo.scrubGrid
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.scrub-grid`: the columns a scrub over the case's `window` reads as runs of one sample, folded
 * here from the grid's columns, and the distance each column of its `centres` stands for. The column count is
 * held to the published constant.
 */
object GeoProfileScrubGridRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("columns" to PROFILE_SCRUB_COLUMNS.toDouble()))
        val input = case.fields
        val window = fixtureWindow(input["window"]) ?: throw AssertionError("the case states no `window`")
        val grid = scrubGrid(fixtureSeries(input["series"]).points, window)
        val runs = ArrayList<Triple<Int, Int, Int?>>()
        for ((column, sample) in grid.withIndex()) {
            val open = runs.lastOrNull()
            if (open != null && open.third == sample) {
                runs[runs.size - 1] = Triple(open.first, column, sample)
            } else {
                runs += Triple(column, column, sample)
            }
        }
        val centres =
            input.items("centres").map { item ->
                val number =
                    (item as? FixtureValue.Number)?.value ?: throw AssertionError("a column of `centres` is no number")
                val column = number.toInt()
                if (column.toDouble() != number) throw AssertionError("a column of `centres` is no whole number")
                scrubDistance(column, window)
            }
        return ConformanceOutcome.Value(
            objectValue(
                "centres" to numbersValue(centres),
                "runs" to
                    FixtureValue.Array(
                        runs.map { (first, last, sample) ->
                            objectValue(
                                "first" to FixtureValue.Number(first.toDouble()),
                                "last" to FixtureValue.Number(last.toDouble()),
                                "sample" to numberOrNull(sample?.toDouble()),
                            )
                        },
                    ),
            ),
        )
    }
}
