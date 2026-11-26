package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WaypointDeclaresNothingTest {
    @Test
    fun aPlaceWithNoFactDeclaresNothingWhateverItsDescription() {
        assertTrue(waypointDeclaresNothing(restWaypoint()))
        assertTrue(waypointDeclaresNothing(restWaypoint(description = "The old refuge above the glacier.")))
        assertTrue(waypointDeclaresNothing(restWaypoint(media = RestSchema.WaypointMedia { photos = emptyList() })))
    }

    @Test
    fun anyServiceTimingContactMediaOrFieldIsAFact() {
        assertFalse(waypointDeclaresNothing(restWaypoint(amenities = listOf("water"))))
        assertFalse(waypointDeclaresNothing(restWaypoint(timing = RestSchema.WaypointTiming { stop_duration = 60 })))
        assertFalse(
            waypointDeclaresNothing(restWaypoint(contact = RestSchema.WaypointContact { phone = "+33450123456" })),
        )
        assertFalse(
            waypointDeclaresNothing(
                restWaypoint(properties = listOf(RestSchema.WaypointProperty("beds", "Beds", "40"))),
            ),
        )
    }
}
