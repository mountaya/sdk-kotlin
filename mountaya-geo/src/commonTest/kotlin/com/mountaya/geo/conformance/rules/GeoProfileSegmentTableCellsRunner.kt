package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.segment-table-cells`: what the case's `columns` state about its `row`. The default columns are held
 * to the published ones.
 */
object GeoProfileSegmentTableCellsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(case, mapOf("defaultColumns" to profileTexts(defaultSegmentTableColumns.map { it.id })))
        val input = case.fields
        val row = profileSegmentRow(input["row"])
        val columns =
            profileFigures(input, "columns", SegmentTableColumn.entries) { it.id } ?: defaultSegmentTableColumns
        return ConformanceOutcome.Value(FixtureValue.Array(columns.map { written(segmentRowCell(row, it)) }))
    }

    /** [cell] as the fixture writes it, its column first. */
    private fun written(cell: SegmentRowCell): FixtureValue {
        val column = "column" to FixtureValue.Text(cell.column.id)
        return when (cell) {
            is SegmentRowCell.Segment -> {
                profileObject(column, "name" to FixtureValue.Text(cell.name))
            }

            is SegmentRowCell.Profile -> {
                profileObject(
                    column,
                    "color" to profileText(cell.color),
                    "lit" to
                        profileObject(
                            "from" to FixtureValue.Number(cell.lit.from),
                            "to" to FixtureValue.Number(cell.lit.to),
                        ),
                )
            }

            is SegmentRowCell.Distance -> {
                profileObject(column, "leg" to profileNumber(cell.leg), "total" to profileNumber(cell.total))
            }

            is SegmentRowCell.Elevation -> {
                profileObject(
                    column,
                    "ascent" to profileNumber(cell.ascent),
                    "descent" to profileNumber(cell.descent),
                    "totalAscent" to profileNumber(cell.totalAscent),
                )
            }

            is SegmentRowCell.Altitude -> {
                profileObject(
                    column,
                    "band" to
                        (
                            cell.band?.let {
                                profileObject(
                                    "max" to FixtureValue.Number(it.max),
                                    "min" to FixtureValue.Number(it.min),
                                )
                            } ?: FixtureValue.Null
                        ),
                    "single" to profileNumber(cell.single),
                )
            }

            is SegmentRowCell.Duration -> {
                profileObject(column, "seconds" to profileNumber(cell.seconds))
            }

            is SegmentRowCell.Difficulty -> {
                profileObject(column, "text" to profileText(cell.text?.value))
            }
        }
    }
}
