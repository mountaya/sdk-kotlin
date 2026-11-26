package com.mountaya.conformance

import com.mountaya.geo.searchTypingPause
import kotlinx.coroutines.test.TestScope
import kotlin.time.DurationUnit

/**
 * `geo.search.typing-pause`: plays the case's script (`GeoSearchScripts`), the typing pause held to `searchTypingPause`,
 * a search given a pause below zero answering `{refused: true}` alone, as it is refused as it is built.
 */
object GeoSearchTypingPauseRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules =
                mapOf("typingPauseMs" to searchTypingPause.toDouble(DurationUnit.MILLISECONDS)),
        )
        return ConformanceOutcome.Value(GeoSearchScripts.played(scope, GeoSearchScripts.script(case.fields)))
    }
}
