package com.mountaya.conformance

import com.mountaya.geo.waypointPropertyLink
import com.mountaya.geo.waypointPropertyValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.waypoint-property-value`: how the case's `property` reads for its `units`, an instant written as its
 * `instantMoment` where it states one, and the address it links to.
 */
object GeoProfileWaypointPropertyValueRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val property = fixtureProperty(input["property"])
        val text =
            waypointPropertyValue(property, readerUnits(input["units"]), fixtureFormatter(input, "instantMoment"))
        return ConformanceOutcome.Value(
            objectValue("href" to textOrNull(waypointPropertyLink(property)), "text" to FixtureValue.Text(text)),
        )
    }
}
