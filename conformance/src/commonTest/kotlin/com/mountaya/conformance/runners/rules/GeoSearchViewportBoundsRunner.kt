package com.mountaya.conformance

import com.mountaya.geo.SEARCH_BOUNDS_COSINE_FLOOR
import com.mountaya.geo.SEARCH_BOUNDS_MAX_SPAN_DEGREES
import com.mountaya.geo.searchViewportBounds
import kotlinx.coroutines.test.TestScope

/**
 * `geo.search.viewport-bounds`: the box a viewport-restricted search sends for a camera at the case's
 * `center` and `zoom`. The cosine floor and the span's ceiling are held to the ones Geo publishes.
 */
object GeoSearchViewportBoundsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules =
                mapOf(
                    "cosineFloor" to SEARCH_BOUNDS_COSINE_FLOOR,
                    "maxSpanDegrees" to SEARCH_BOUNDS_MAX_SPAN_DEGREES,
                ),
        )
        val box = searchViewportBounds(fixturePosition(case.fields["center"], "center"), case.fields.number("zoom"))
        return ConformanceOutcome.Value(FixtureValue.Array(box.map { FixtureValue.Number(it) }))
    }
}
