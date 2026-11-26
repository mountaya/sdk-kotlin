package com.mountaya.conformance

import com.mountaya.geo.GRADE_WINDOW_METERS
import com.mountaya.geo.slopeBuckets
import com.mountaya.geo.slopeStretches
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.slope-stretches`: the stretches the case's `series` is painted in by its own steepness, each its
 * ends, its bucket's key and its colour. The buckets and the grade window are held to the published ones.
 */
object GeoProfileSlopeStretchesRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf("gradeWindowMeters" to GRADE_WINDOW_METERS),
            values =
                mapOf(
                    "buckets" to
                        FixtureValue.Array(
                            slopeBuckets.map {
                                objectValue(
                                    "color" to FixtureValue.Text(it.color),
                                    "from" to FixtureValue.Number(it.from),
                                    "key" to FixtureValue.Text(it.key),
                                )
                            },
                        ),
                ),
        )
        val series = fixtureSeries(case.fields["series"])
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                slopeStretches(series.points).map {
                    objectValue(
                        "color" to FixtureValue.Text(it.color),
                        "from" to FixtureValue.Number(it.from),
                        "key" to FixtureValue.Text(it.label),
                        "to" to FixtureValue.Number(it.to),
                    )
                },
            ),
        )
    }
}
