package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QueryLayerIdsTest {
    private val placed =
        listOf(
            PlacedLayer("itinerary-line", 50),
            PlacedLayer("itinerary-point", 90),
            PlacedLayer("zone-fill", 40),
            PlacedLayer("hut", 80),
        )

    @Test
    fun eachQueryAsksAboutItsBandsTheLiveStyleHoldsInTheOrderTheMapPlacedThem() {
        val live = setOf("background", "road-label", "itinerary-line", "zone-fill", "hut")

        assertEquals(
            listOf("itinerary-line", "zone-fill", "hut"),
            queryLayerIds(PointQuery.Features, placed) {
                it in
                    live
            },
        )
        assertEquals(listOf("hut"), queryLayerIds(PointQuery.Pois, placed) { it in live })
        assertEquals(listOf("zone-fill"), queryLayerIds(PointQuery.Zones, placed) { it in live })
        assertEquals(
            listOf("itinerary-line", "itinerary-point"),
            queryLayerIds(PointQuery.Itineraries, placed) { true },
        )
    }

    @Test
    fun aRankBetweenTwoBandsIsInNeitherAndAQueryLeftWithNoLayerAsksNothing() {
        val between = listOf(PlacedLayer("itinerary-line", 50), PlacedLayer("mine", 52))

        assertEquals(listOf("itinerary-line"), queryLayerIds(PointQuery.Itineraries, between) { true })
        assertEquals(listOf("itinerary-line", "mine"), queryLayerIds(PointQuery.Features, between) { true })
        assertNull(queryLayerIds(PointQuery.Pois, between) { true })
        assertNull(queryLayerIds(PointQuery.Features, placed) { false })
        assertNull(queryLayerIds(PointQuery.Features, emptyList()) { true })
    }
}
