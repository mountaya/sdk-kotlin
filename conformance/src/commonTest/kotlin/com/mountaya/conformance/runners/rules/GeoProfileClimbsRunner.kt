package com.mountaya.conformance

import com.mountaya.geo.CLIMB_CLOSE_GRADE_PERCENT
import com.mountaya.geo.CLIMB_CLOSE_WINDOW_KM
import com.mountaya.geo.CLIMB_OPEN_GRADE_PERCENT
import com.mountaya.geo.CLIMB_OPEN_WINDOW_KM
import com.mountaya.geo.ClimbCategory
import com.mountaya.geo.MIN_CLIMB_GRADE_PERCENT
import com.mountaya.geo.MIN_CLIMB_LENGTH_KM
import com.mountaya.geo.RouteProfilePoint
import com.mountaya.geo.climbCategoryFor
import com.mountaya.geo.detectClimbs
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.climbs`: a case with `points` answers the climbs on them, in order of their start; one
 * with a `lengthMeters` and an `avgGradePercent` answers that climb's category, or null. The windows,
 * the grades and the shortest climb are held to the published constants, and the score thresholds to
 * each category's minimum score.
 */
object GeoProfileClimbsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules =
                mapOf(
                    "closeGradePercent" to CLIMB_CLOSE_GRADE_PERCENT,
                    "closeWindowKm" to CLIMB_CLOSE_WINDOW_KM,
                    "minClimbLengthKm" to MIN_CLIMB_LENGTH_KM,
                    "minGradePercent" to MIN_CLIMB_GRADE_PERCENT,
                    "openGradePercent" to CLIMB_OPEN_GRADE_PERCENT,
                    "openWindowKm" to CLIMB_OPEN_WINDOW_KM,
                ),
            values =
                mapOf(
                    "scoreThresholds" to
                        FixtureValue.Object(
                            ClimbCategory.entries.map { it.id to FixtureValue.Number(it.minimumScore) },
                        ),
                ),
        )
        val input = case.fields
        if (input["points"] == null) {
            val category = climbCategoryFor(input.number("lengthMeters"), input.number("avgGradePercent"))
            return ConformanceOutcome.Value(textOrNull(category?.id))
        }
        val points =
            input.items("points").map { item ->
                val point = item as? FixtureValue.Object ?: throw AssertionError("a point is not an object")
                RouteProfilePoint(point.number("distanceKm"), point.number("elevation"))
            }
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                detectClimbs(points).map { climb ->
                    FixtureValue.Object(
                        listOf(
                            "avgGradePercent" to FixtureValue.Number(climb.avgGradePercent),
                            "category" to FixtureValue.Text(climb.category.id),
                            "endKm" to FixtureValue.Number(climb.endKm),
                            "peakElevation" to FixtureValue.Number(climb.peakElevation),
                            "startKm" to FixtureValue.Number(climb.startKm),
                        ),
                    )
                },
            ),
        )
    }
}
