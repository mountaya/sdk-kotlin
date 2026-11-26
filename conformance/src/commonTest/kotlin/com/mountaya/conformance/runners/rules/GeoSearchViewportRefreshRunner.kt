package com.mountaya.conformance

import com.mountaya.geo.searchViewportRefreshPause
import kotlinx.coroutines.test.TestScope
import kotlin.time.DurationUnit

/**
 * `geo.search.viewport-refresh`: plays the case's script (`GeoSearchScripts`), the pause after the camera rests held to
 * `searchViewportRefreshPause`.
 */
object GeoSearchViewportRefreshRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = mapOf("viewportRefreshMs" to searchViewportRefreshPause.toDouble(DurationUnit.MILLISECONDS)),
        )
        return ConformanceOutcome.Value(GeoSearchScripts.play(scope, GeoSearchScripts.script(case.fields)).value)
    }
}
