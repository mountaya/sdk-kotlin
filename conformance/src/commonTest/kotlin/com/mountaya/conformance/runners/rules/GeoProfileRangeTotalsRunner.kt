package com.mountaya.conformance

import com.mountaya.geo.ELEVATION_NOISE_THRESHOLD_METERS
import com.mountaya.geo.GRADE_WINDOW_METERS
import com.mountaya.geo.rangeTotals
import kotlinx.coroutines.test.TestScope

/** `geo.profile.range-totals`: the totals of the stretch the case's `range` names on its `series`. */
object GeoProfileRangeTotalsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "elevationNoiseThresholdMeters" to ELEVATION_NOISE_THRESHOLD_METERS,
                "gradeWindowMeters" to GRADE_WINDOW_METERS,
            ),
        )
        val range = case.fields.objectOrNull("range") ?: throw AssertionError("the case has no `range`")
        val totals = rangeTotals(fixtureSeries(case.fields["series"]), range.number("from"), range.number("to"))
        return ConformanceOutcome.Value(totalsValue(totals))
    }
}
