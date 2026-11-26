package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class ToggleAmenityTest {
    private val water = RestSchema.WaypointAmenity("water")

    private fun amenities(vararg values: String): List<RestSchema.WaypointAmenity> =
        values.map { RestSchema.WaypointAmenity(it) }

    @Test
    fun aPressAddsItsServiceAtTheEndOrTakesAPickedOneOut() {
        assertEquals(amenities("food", "water"), toggleAmenity(amenities("food"), water))
        assertEquals(amenities("food"), toggleAmenity(amenities("water", "food"), water))
        assertEquals(amenities("food"), toggleAmenity(amenities("water", "food", "water"), water))
        assertEquals(amenities("water"), toggleAmenity(emptyList(), water))
    }
}
