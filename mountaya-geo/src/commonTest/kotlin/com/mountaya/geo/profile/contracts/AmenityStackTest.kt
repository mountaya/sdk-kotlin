package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AmenityStackTest {
    private val water = RestSchema.WaypointAmenity("water")
    private val food = RestSchema.WaypointAmenity("food")

    @Test
    fun twoStacksAreEqualWhereThePinAndTheServicesAre() {
        val pin = CurvePin("col#0", ProfilePlace(ProfileWaypoint.AtDistance("col", 0.0)), 0.0, 0.0, 0.0, 0.0)
        val stack = AmenityStack(pin, listOf(water), 1, listOf(water, food))

        assertEquals(AmenityStack(pin, listOf(water), 1, listOf(water, food)), stack)
        assertNotEquals(AmenityStack(pin, listOf(water), 2, listOf(water, food)), stack)
        assertEquals(AmenityStack(pin, listOf(water), 1, listOf(water, food)).hashCode(), stack.hashCode())
    }
}
