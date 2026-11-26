package com.mountaya.conformance

import com.mountaya.geo.ELEVATION_NOISE_THRESHOLD_METERS
import com.mountaya.geo.GRADE_WINDOW_METERS
import com.mountaya.geo.MAX_PROFILE_POINTS
import com.mountaya.geo.profileTotals
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.totals`: the totals of the case's `series`. The noise threshold, the grade window and the
 * series' ceiling are held to the published constants. The narrowest window a chart is drawn over is
 * `geo.profile.window-clamp`'s, which holds it as its `minSpanMeters`.
 */
object GeoProfileTotalsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "elevationNoiseThresholdMeters" to ELEVATION_NOISE_THRESHOLD_METERS,
                "gradeWindowMeters" to GRADE_WINDOW_METERS,
                "maxProfilePoints" to MAX_PROFILE_POINTS.toDouble(),
            ),
        )
        return ConformanceOutcome.Value(totalsValue(profileTotals(fixtureSeries(case.fields["series"]))))
    }
}
