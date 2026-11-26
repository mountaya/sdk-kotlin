package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/** `geo.profile.row-match`: the id of the row of the case's `rows` that answers to its `id`, or that its `distance` stands in, or null. */
object GeoProfileRowMatchRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val rows =
            input.items("rows").mapIndexed { index, item ->
                val row = item as? FixtureValue.Object ?: throw AssertionError("a row is not an object")
                val id = row.text("id")
                ItinerarySegmentRow(
                    key = id,
                    id = id,
                    memberIds = profileOptionalTexts(row["memberIds"]),
                    name = "",
                    index = index,
                    from = row.number("from"),
                    to = row.number("to"),
                    metrics = ItinerarySegmentRowMetrics(),
                )
            }
        if (input["distance"] !=
            null
        ) {
            return ConformanceOutcome.Value(profileText(rowAt(rows, input.number("distance"))?.id))
        }
        val id = input.textOrNull("id")
        return ConformanceOutcome.Value(profileText(rows.firstOrNull { rowStandsFor(it, id) }?.id))
    }
}
