package com.mountaya.conformance

import com.mountaya.geo.DurationUnit
import com.mountaya.geo.convertDuration
import com.mountaya.geo.splitDuration
import kotlinx.coroutines.test.TestScope

/** `geo.profile.split-duration`: the hours and minutes the case's `seconds` split into, or null. */
object GeoProfileSplitDurationRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "minutesPerHour" to convertDuration(1.0, DurationUnit.Hour, DurationUnit.Minute),
                "secondsPerMinute" to convertDuration(1.0, DurationUnit.Minute, DurationUnit.Second),
            ),
        )
        val split =
            splitDuration(case.fields.numberOrNull("seconds")) ?: return ConformanceOutcome.Value(FixtureValue.Null)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "hours" to FixtureValue.Number(split.hours.toDouble()),
                    "minutes" to FixtureValue.Number(split.minutes.toDouble()),
                ),
            ),
        )
    }
}
