package com.mountaya.conformance

import kotlinx.coroutines.test.TestScope

/** `geo.search.broaden`: plays the case's script (`GeoSearchScripts`). */
object GeoSearchBroadenRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        return ConformanceOutcome.Value(GeoSearchScripts.play(scope, GeoSearchScripts.script(case.fields)).value)
    }
}
