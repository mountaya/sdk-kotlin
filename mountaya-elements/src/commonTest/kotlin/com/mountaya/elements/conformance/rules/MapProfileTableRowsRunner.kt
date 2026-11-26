package com.mountaya.elements

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.ElevationPoint
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.ItinerarySegmentRow
import com.mountaya.geo.ItinerarySegmentRowMetrics
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonPrimitive

/**
 * `map.profile.table-rows`: what an itinerary's two tables write to one map and light, played step by step through
 * the rules the tables run on, over the engine. A row pointed at is written to the map as its table writes it, its
 * target (`legTarget`, `passTarget`) through the table's own writer, under the table's cursor marker id, and taken
 * down as the reader leaves the table; the map's own cursor over a leg is the hover the engine tells, its features
 * naming the leg as an itinerary's do, read as the segment table reads it (`hoveredLeg`); and a row is lit as the
 * tables light one (`isSegmentRowLit`), the app's `highlighted` row named by a `highlight` step. Each step is
 * answered with the cursor markers the map holds, the stretch its emphasis slot holds, and the rows lit.
 */
object MapProfileTableRowsRunner : ConformanceRunner {
    /** A waypoint table's row: its key, and the meter the itinerary passes its place at. */
    private class WaypointRow(
        val key: String,
        val at: Double,
    )

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        if (case.fixture.objectOrNull("constants") != null) {
            throw AssertionError("the fixture declares constants no rule here states")
        }
        val input = case.fields
        val series = series(input.objectOrNull("series") ?: throw AssertionError("the case states no `series`"))
        val segments =
            input["segments"]?.let { input.items("segments") }.orEmpty().mapIndexed { index, written ->
                val row = written as? FixtureValue.Object ?: throw AssertionError("a segment row is not an object")
                val id = row.text("id")
                val members =
                    (row["memberIds"] as? FixtureValue.Array)?.items.orEmpty().map { member ->
                        (member as? FixtureValue.Text)?.value ?: throw AssertionError("a member id is no text")
                    }
                ItinerarySegmentRow(
                    id,
                    id,
                    members,
                    id,
                    null,
                    index,
                    row.number("from"),
                    row.number("to"),
                    ItinerarySegmentRowMetrics(),
                )
            }
        val waypoints =
            input["waypoints"]?.let { input.items("waypoints") }.orEmpty().map { written ->
                val row = written as? FixtureValue.Object ?: throw AssertionError("a waypoint row is not an object")
                WaypointRow(row.text("key"), row.number("at"))
            }

        val map = MapEngine(MapRunnerFixtures.client(), geolocation = InertGeolocation())
        val segmentTable = MapTargetWriter(SEGMENT_TABLE_CURSOR_MARKER_ID)
        val waypointTable = MapTargetWriter(WAYPOINT_TABLE_CURSOR_MARKER_ID)
        var pointedSegment: String? = null
        var pointedWaypoint: String? = null
        var highlighted: String? = null
        var hovered: String? = null
        val answers = mutableListOf<FixtureValue>()
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            when (val name = step.text("step")) {
                "point" -> {
                    val pointed = step.text("row")
                    when (val table = step.text("table")) {
                        "segments" -> {
                            val row =
                                segments.firstOrNull { it.id == pointed }
                                    ?: throw AssertionError("the segment table holds no row `$pointed`")
                            pointedSegment = row.key
                            segmentTable.write(legTarget(series, row.from, row.to), map, null, null)
                        }

                        "waypoints" -> {
                            val row =
                                waypoints.firstOrNull { it.key == pointed }
                                    ?: throw AssertionError("the waypoint table holds no row `$pointed`")
                            pointedWaypoint = row.key
                            waypointTable.write(passTarget(series, row.at), map, null, null)
                        }

                        else -> {
                            throw AssertionError("no table is called `$table`")
                        }
                    }
                }

                "leave" -> {
                    when (val table = step.text("table")) {
                        "segments" -> {
                            pointedSegment = null
                            segmentTable.write(MapTarget(), map, null, null)
                        }

                        "waypoints" -> {
                            pointedWaypoint = null
                            waypointTable.write(MapTarget(), map, null, null)
                        }

                        else -> {
                            throw AssertionError("no table is called `$table`")
                        }
                    }
                }

                "hover" -> {
                    // The engine's hover over an itinerary, its features naming the leg under the cursor, or none.
                    val leg = step.textOrNull("segment")
                    val features =
                        listOfNotNull(
                            leg?.let {
                                QueriedFeature(
                                    mapOf("segment_id" to JsonPrimitive(it)),
                                    GeoJsonPoint(listOf(6.0, 45.0)),
                                    "itinerary-line",
                                )
                            },
                        )
                    hovered = hoveredLeg(MapEvent.FeatureHover(listOf(6.0, 45.0), features))
                }

                "highlight" -> {
                    highlighted = step.text("row")
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
            answers +=
                objectOf(
                    "emphasis" to emphasis(map),
                    "lit" to
                        objectOf(
                            "segments" to
                                MapRunnerFixtures.texts(
                                    segments
                                        .filter { isSegmentRowLit(it, pointedSegment, highlighted, hovered) }
                                        .map { it.id },
                                ),
                            "waypoints" to
                                MapRunnerFixtures.texts(waypoints.filter { it.key == pointedWaypoint }.map { it.key }),
                        ),
                    "markers" to
                        FixtureValue.Array(
                            map.state.value.markers.map { marker ->
                                objectOf(
                                    "id" to FixtureValue.Text(marker.id),
                                    "lngLat" to numbers(marker.position),
                                )
                            },
                        ),
                )
        }
        map.close()
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }

    /** The series a case writes, as the profile fixtures write one. */
    private fun series(written: FixtureValue.Object): ElevationSeries =
        ElevationSeries(
            written.items("points").map { item ->
                val point = item as? FixtureValue.Object ?: throw AssertionError("a sample is not an object")
                val lngLat =
                    (point["lngLat"] as? FixtureValue.Array)?.items.orEmpty().map { coordinate ->
                        (coordinate as? FixtureValue.Number)?.value ?: throw AssertionError("a coordinate is no number")
                    }
                val index = point.number("index").toInt()
                ElevationPoint(point.number("distance"), point.number("elevation"), lngLat, index)
            },
            written.number("totalDistance"),
            written.numberOrNull("minElevation"),
            written.numberOrNull("maxElevation"),
        )

    /** The coordinates of the stretch the map's emphasis slot holds, or null where it holds none. */
    private fun emphasis(map: MapEngine): FixtureValue {
        val geometry = map.geometry(GeometrySlot.Emphasis).geometry ?: return FixtureValue.Null
        val line = geometry as? GeoJsonLineString ?: throw AssertionError("the emphasis slot holds no line: $geometry")
        return FixtureValue.Array(line.coordinates.map(::numbers))
    }

    private fun numbers(values: List<Double>): FixtureValue = FixtureValue.Array(values.map { FixtureValue.Number(it) })

    private fun objectOf(vararg members: Pair<String, FixtureValue>): FixtureValue =
        FixtureValue.Object(members.toList())
}
