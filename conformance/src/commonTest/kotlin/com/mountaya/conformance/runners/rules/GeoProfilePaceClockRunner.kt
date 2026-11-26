package com.mountaya.conformance

import com.mountaya.geo.formatPaceClock
import com.mountaya.geo.paceClockNumberToMinutes
import com.mountaya.geo.paceMinutesToClockNumber
import com.mountaya.geo.parsePaceClock
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.pace-clock`: one of the four readings of a pace clock, the one the case's input names as its
 * `operation`: `format`, `parse`, `toClockNumber` or `fromClockNumber`.
 */
object GeoProfilePaceClockRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val answer =
            when (val operation = input.text("operation")) {
                "format" -> FixtureValue.Text(formatPaceClock(input.numberOrNull("minutes")))
                "parse" -> numberOrNull(parsePaceClock(input.text("text")))
                "toClockNumber" -> numberOrNull(paceMinutesToClockNumber(input.numberOrNull("minutes")))
                "fromClockNumber" -> numberOrNull(paceClockNumberToMinutes(input.numberOrNull("value")))
                else -> throw AssertionError("`$operation` is no operation of a pace clock this runner knows")
            }
        return ConformanceOutcome.Value(answer)
    }
}
