package com.mountaya.conformance

import com.mountaya.geo.ProfileClass
import com.mountaya.geo.stretchAt
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.stretch-at`: the name of the stretch the case's `distance` reads among its named `stretches`, or
 * null. A stretch is a classified one whose word is its name.
 */
object GeoProfileStretchAtRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val stretches =
            input.items("stretches").map { item ->
                val stretch = item as? FixtureValue.Object ?: throw AssertionError("a stretch is not an object")
                ProfileClass(stretch.number("from"), stretch.number("to"), "", stretch.text("name"))
            }
        return ConformanceOutcome.Value(textOrNull(stretchAt(stretches, input.number("distance"))?.label))
    }
}
