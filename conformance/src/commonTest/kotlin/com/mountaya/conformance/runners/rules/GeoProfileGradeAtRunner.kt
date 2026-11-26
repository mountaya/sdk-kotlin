package com.mountaya.conformance

import com.mountaya.geo.GRADE_WINDOW_METERS
import com.mountaya.geo.gradeAt
import kotlinx.coroutines.test.TestScope

/** `geo.profile.grade-at`: the grade at the case's `distance` along its `series`, or null. The grade window is held to the published constant. */
object GeoProfileGradeAtRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("gradeWindowMeters" to GRADE_WINDOW_METERS))
        val input = case.fields
        return ConformanceOutcome.Value(
            numberOrNull(gradeAt(fixtureSeries(input["series"]).points, input.number("distance"))),
        )
    }
}
