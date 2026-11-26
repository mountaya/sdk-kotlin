package com.mountaya.conformance

import com.mountaya.geo.overlayInstant
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.overlay-instant`: the moment `overlayInstant` answers for the case's `pinned` instant at the clock's
 * reading, `nowMs`, written in epoch milliseconds. A pin that names no instant is handed as none. A clock that reads no
 * instant, NaN, is refused by Kotlin's types, its `Clock` answering an instant on every read, so no reading can be handed
 * the rule: the case is answered the rule's answer without one, the pin as it is, or no moment with none pinned.
 */
object GeoLayersOverlayInstantRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val pinned = TimeFixtures.instant(input.textOrNull("pinned"))
        val now = TimeFixtures.reading(input["nowMs"])
        return ConformanceOutcome.Value(TimeFixtures.written(if (now == null) pinned else overlayInstant(pinned, now)))
    }
}
