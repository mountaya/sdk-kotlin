package com.mountaya.conformance

import com.mountaya.geo.ItineraryLeg
import com.mountaya.geo.ItinerarySegmentRowMetrics
import com.mountaya.geo.itinerarySegmentRows
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.segment-rows`: the case's `segments` as table rows over its `series`. Each segment is a leg holding
 * its identity and geometry, and its stated `metrics` are stated for it by its id, the leg's own figures left
 * out, which is the same reading.
 */
object GeoProfileSegmentRowsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val legs = ArrayList<ItineraryLeg>()
        val stated = HashMap<String, ItinerarySegmentRowMetrics>()
        for (item in input.items("segments")) {
            val segment = item as? FixtureValue.Object ?: throw AssertionError("a segment is not an object")
            val id = segment.text("id")
            legs +=
                ItineraryLeg(
                    id = id,
                    memberIds = profileOptionalTextList(segment["memberIds"]),
                    name = segment.textOrNull("name") ?: "",
                    color = segment.textOrNull("color"),
                    geometry = segment["geojson"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry),
                )
            segment["metrics"]?.takeUnless { it is FixtureValue.Null }?.let { stated[id] = fixtureSegmentMetrics(it) }
        }
        val rows = itinerarySegmentRows(fixtureSeries(input["series"]), legs, stated)
        return ConformanceOutcome.Value(FixtureValue.Array(rows.map(::segmentRowValue)))
    }
}
