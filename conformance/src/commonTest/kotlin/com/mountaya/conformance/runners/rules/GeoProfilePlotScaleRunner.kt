package com.mountaya.conformance

import com.mountaya.geo.PROFILE_BASELINE
import com.mountaya.geo.PROFILE_BOTTOM_INSET
import com.mountaya.geo.PROFILE_TOP_INSET
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.profileX
import com.mountaya.geo.profileY
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.plot-scale`: where the case's `distance` and `elevation` are drawn in the profile's square, over
 * its `window` where it states one. The square, its insets and its baseline are held to the published
 * constants.
 */
object GeoProfilePlotScaleRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "baseline" to PROFILE_BASELINE,
                "bottomInset" to PROFILE_BOTTOM_INSET,
                "topInset" to PROFILE_TOP_INSET,
                "viewBox" to PROFILE_VIEW_BOX,
            ),
        )
        val input = case.fields
        val x = profileX(input.number("distance"), input.number("totalDistance"), fixtureWindow(input["window"]))
        val y = profileY(input.number("elevation"), fixtureDomain(input["domain"]))
        return ConformanceOutcome.Value(objectValue("x" to FixtureValue.Number(x), "y" to FixtureValue.Number(y)))
    }
}
