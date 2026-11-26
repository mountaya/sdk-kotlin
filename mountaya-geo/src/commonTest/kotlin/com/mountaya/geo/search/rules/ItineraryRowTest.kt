package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ItineraryRowTest {
    @Test
    fun anItineraryStatesItsFiguresForTheSurfaceToWord() {
        val row =
            itineraryRow(
                DataSchema.ItinerarySearchResult {
                    id = "it-1"
                    name = "Haute Route"
                    distance = 24_500.0
                    ascent = 1_450
                    activitySlug = DataSchema.ActivitySlug("SKI_TOURING")
                    start = listOf(6.1, 45.1)
                },
                "haute",
            )
        assertEquals("itinerary:it-1", row?.id)
        assertEquals(listOf(0 until 5), row?.matched)
        assertEquals(SearchRowSecondary.Metric(24_500.0, 1_450), row?.secondary)
        assertEquals(listOf(DataSchema.ActivitySlug("SKI_TOURING")), row?.activities)
        assertEquals(SearchRowPayload.Itinerary("it-1", null, listOf(6.1, 45.1)), row?.payload)
    }

    @Test
    fun anItineraryWithNoFiguresStatesNoneAndOneWithNoIdIsNoRow() {
        val bare = itineraryRow(DataSchema.ItinerarySearchResult { id = "it-2" }, "")
        assertEquals(SearchRowSecondary.Metric(null, null), bare?.secondary)
        assertEquals("", bare?.title)
        assertNull(itineraryRow(DataSchema.ItinerarySearchResult { name = "No id" }, ""))
    }

    @Test
    fun anItinerarysStartDropsTheAltitudeItsPointCarries() {
        val row =
            itineraryRow(
                DataSchema.ItinerarySearchResult {
                    id = "it-1"
                    start = listOf(6.1, 45.1, 1_035.0)
                },
                "",
            )
        assertEquals(SearchRowPayload.Itinerary("it-1", null, listOf(6.1, 45.1)), row?.payload)
    }
}
