package com.mountaya.conformance

import com.mountaya.geo.formatLngLat
import kotlinx.coroutines.test.TestScope

/** `geo.format.lnglat`: the case's `position`, written for a reader, or null where it names no place. */
object GeoFormatLnglatRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val text = formatLngLat(fixturePosition(case.fields["position"], "position"))
        return ConformanceOutcome.Value(text?.let { FixtureValue.Text(it) } ?: FixtureValue.Null)
    }
}
