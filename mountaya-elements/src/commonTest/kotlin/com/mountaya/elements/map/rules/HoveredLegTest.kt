package com.mountaya.elements

import com.mountaya.client.GeoJsonPoint
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HoveredLegTest {
    private fun feature(vararg properties: Pair<String, String>) =
        QueriedFeature(
            properties.associate { (name, value) -> name to JsonPrimitive(value) },
            GeoJsonPoint(listOf(6.0, 45.0)),
            "itinerary-line",
        )

    /**
     * The leg is the first a feature names, topmost first, by its `segment_id`, and none where no feature names one.
     */
    @Test
    fun theLegIsTheFirstTheFeaturesUnderThePointerName() {
        val place = listOf(6.0, 45.0)

        val features = listOf(feature("waypoint_id" to "hut"), feature("segment_id" to "col"))

        assertEquals("col", hoveredLeg(MapEvent.FeatureHover(place, features)))
        assertNull(hoveredLeg(MapEvent.FeatureHover(place, emptyList())))
        assertNull(hoveredLeg(MapEvent.FeatureHover(place, listOf(feature("segment_id" to "")))))
    }
}
