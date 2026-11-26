package com.mountaya.conformance

import com.mountaya.geo.AXIS_LABEL_GUTTER
import com.mountaya.geo.AxisLabelBox
import com.mountaya.geo.cullCrowdedLabels
import kotlinx.coroutines.test.TestScope

/** `geo.profile.axis-label-culling`: the keys of the case's laid-out `boxes` left undrawn. The gutter is held to the published constant. */
object GeoProfileAxisLabelCullingRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, mapOf("gutterPixels" to AXIS_LABEL_GUTTER))
        val boxes =
            case.fields.items("boxes").map { item ->
                val box = item as? FixtureValue.Object ?: throw AssertionError("a box is not an object")
                AxisLabelBox(box.text("key"), box.number("left"), box.number("right"))
            }
        return ConformanceOutcome.Value(textArray(cullCrowdedLabels(boxes)))
    }
}
