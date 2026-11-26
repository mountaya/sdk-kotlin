package com.mountaya.conformance

import com.mountaya.geo.itineraryWaypointRows
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.waypoint-rows`: the case's `waypoints` as waypoint rows over its `geometry` and `series`, each
 * placed at its `distance` or its `lngLat`, with its stated `metrics`.
 */
object GeoProfileWaypointRowsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val places = input.items("waypoints").map(::fixturePlace)
        val geometry = input["geometry"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry)
        val rows = itineraryWaypointRows(fixtureSeries(input["series"]), geometry, places)
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                rows.map { row ->
                    val entries =
                        mutableListOf(
                            "from" to FixtureValue.Number(row.from),
                            "id" to FixtureValue.Text(row.place.id),
                            "index" to FixtureValue.Number(row.index.toDouble()),
                            "key" to FixtureValue.Text(row.key),
                            "metrics" to waypointMetricsValue(row.metrics),
                            "to" to FixtureValue.Number(row.to),
                        )
                    row.place.name?.let { entries += "name" to FixtureValue.Text(it) }
                    row.place.glyph?.let { entries += "glyph" to FixtureValue.Text(it.value) }
                    FixtureValue.Object(entries)
                },
            ),
        )
    }
}
