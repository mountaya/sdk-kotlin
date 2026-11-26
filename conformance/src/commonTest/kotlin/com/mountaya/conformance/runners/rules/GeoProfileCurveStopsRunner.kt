package com.mountaya.conformance

import com.mountaya.geo.CurveColor
import com.mountaya.geo.curveStops
import kotlinx.coroutines.test.TestScope

/** `geo.profile.curve-stops`: the gradient the case's coloured `stretches` paint the curve with, over its `window` where it states one. */
object GeoProfileCurveStopsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val stretches =
            input.items("stretches").map { item ->
                val stretch = item as? FixtureValue.Object ?: throw AssertionError("a stretch is not an object")
                CurveColor(stretch.number("from"), stretch.number("to"), stretch.text("color"))
            }
        val stops = curveStops(stretches, input.number("totalDistance"), fixtureWindow(input["window"]))
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                stops.map { objectValue("color" to textOrNull(it.color), "offset" to FixtureValue.Number(it.offset)) },
            ),
        )
    }
}
