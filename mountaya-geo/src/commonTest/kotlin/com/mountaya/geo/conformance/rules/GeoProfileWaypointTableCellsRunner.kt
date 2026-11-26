package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.waypoint-table-cells`: what the case's `columns` state about its `row`, over a set named for the case
 * holding the glyphs it names `held`, each a placeholder no rule reads, for its `units`, a cut-off written as its
 * `cutoffMoment` where it states one. The default columns and the fallback glyph are held to the published ones.
 */
object GeoProfileWaypointTableCellsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(
            case,
            mapOf(
                "defaultColumns" to profileTexts(defaultWaypointTableColumns.map { it.id }),
                "fallbackGlyph" to FixtureValue.Text(WAYPOINT_FALLBACK_GLYPH),
            ),
        )
        val input = case.fields
        val row = profileWaypointRow(input["row"])
        val markup = profileOptionalTexts(input["held"]).associateWith { "<path d=\"M0 0h1\"/>" }
        val glyphs =
            GlyphSet.of(case.name, GlyphSet.Paint.Stroke, markup = markup)
                ?: throw AssertionError("the case's set cannot be built")
        val units = profileUnits(input["units"])
        val formatter = profileFormatter(input, "cutoffMoment")
        val columns =
            profileFigures(input, "columns", WaypointTableColumn.entries) { it.id } ?: defaultWaypointTableColumns
        return ConformanceOutcome.Value(
            FixtureValue.Array(columns.map { written(waypointRowCell(row, it, glyphs, units, formatter)) }),
        )
    }

    /** [cell] as the fixture writes it, its column first. */
    private fun written(cell: WaypointRowCell): FixtureValue {
        val column = "column" to FixtureValue.Text(cell.column.id)
        return when (cell) {
            is WaypointRowCell.Waypoint -> {
                val label =
                    cell.name?.let { profileObject("name" to FixtureValue.Text(it)) }
                        ?: profileObject("word" to FixtureValue.Text("profile.unlabelledWaypoint"))
                profileObject(column, "glyph" to FixtureValue.Text(cell.glyph), "label" to label)
            }

            is WaypointRowCell.Profile -> {
                profileObject(
                    column,
                    "lit" to
                        profileObject(
                            "from" to FixtureValue.Number(cell.lit.from),
                            "to" to FixtureValue.Number(cell.lit.to),
                        ),
                )
            }

            is WaypointRowCell.Distance -> {
                profileObject(column, "total" to FixtureValue.Number(cell.total))
            }

            is WaypointRowCell.Elevation -> {
                profileObject(column, "ascent" to profileNumber(cell.ascent), "descent" to profileNumber(cell.descent))
            }

            is WaypointRowCell.Altitude -> {
                profileObject(column, "altitude" to profileNumber(cell.altitude))
            }

            is WaypointRowCell.Amenities -> {
                profileObject(column, "services" to profileTexts(cell.amenities.map { it.value }))
            }

            is WaypointRowCell.Cutoff -> {
                profileObject(column, "moment" to profileText(cell.moment))
            }

            is WaypointRowCell.Properties -> {
                profileObject(
                    column,
                    "fields" to
                        FixtureValue.Array(
                            cell.fields.map {
                                profileObject(
                                    "key" to FixtureValue.Text(it.key),
                                    "label" to FixtureValue.Text(it.label),
                                    "value" to FixtureValue.Text(it.value),
                                )
                            },
                        ),
                )
            }
        }
    }
}
