package com.mountaya.conformance

import com.mountaya.client.GeoJsonLineString
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.MAX_PROFILE_POINTS
import com.mountaya.geo.buildElevationSeries
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.elevation-series`: the series the case's `geometry` projects onto, thinned to its
 * `maxPoints` where it names one and to the declared ceiling where it names none. The ceiling is held
 * to the published constant and read through the rule as well: a line one vertex over it comes back at
 * exactly the ceiling, both its ends kept.
 */
object GeoProfileElevationSeriesRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("maxPoints" to MAX_PROFILE_POINTS.toDouble()))
        val ceiling = case.root("constants").number("maxPoints").toInt()
        // A line that climbs a meter a vertex from zero, so an elevation names the vertex it was read at.
        val overTheCeiling =
            buildElevationSeries(
                GeoJsonLineString(
                    (0..ceiling).map {
                        listOf(it / 1_000.0, 45.0, it.toDouble())
                    },
                ),
            ).points
        val ends = listOf(overTheCeiling.first().elevation, overTheCeiling.last().elevation)
        if (overTheCeiling.size != ceiling || ends != listOf(0.0, ceiling.toDouble())) {
            throw AssertionError("a line one vertex over the ceiling of $ceiling is not thinned to it, both ends kept")
        }
        val input = case.fields
        val geometry = input["geometry"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry)
        val maxPoints = input.numberOrNull("maxPoints")
        val series = maxPoints?.let { buildElevationSeries(geometry, it.toInt()) } ?: buildElevationSeries(geometry)
        return ConformanceOutcome.Value(seriesValue(series))
    }
}

/** [series] in the shape the profile fixtures write a series in. */
internal fun seriesValue(series: ElevationSeries): FixtureValue =
    FixtureValue.Object(
        listOf(
            "hasElevation" to FixtureValue.Bool(series.hasElevation),
            "maxElevation" to numberOrNull(series.maxElevation),
            "minElevation" to numberOrNull(series.minElevation),
            "points" to
                FixtureValue.Array(
                    series.points.map { point ->
                        FixtureValue.Object(
                            listOf(
                                "distance" to FixtureValue.Number(point.distance),
                                "elevation" to FixtureValue.Number(point.elevation),
                                "index" to FixtureValue.Number(point.index.toDouble()),
                                "lngLat" to FixtureValue.Array(point.lngLat.map { FixtureValue.Number(it) }),
                            ),
                        )
                    },
                ),
            "totalDistance" to FixtureValue.Number(series.totalDistance),
        ),
    )

/** [value] as a fixture writes it, null as null. */
internal fun numberOrNull(value: Double?): FixtureValue = value?.let { FixtureValue.Number(it) } ?: FixtureValue.Null

/** [value] as a fixture writes it, null as null. */
internal fun textOrNull(value: String?): FixtureValue = value?.let { FixtureValue.Text(it) } ?: FixtureValue.Null
