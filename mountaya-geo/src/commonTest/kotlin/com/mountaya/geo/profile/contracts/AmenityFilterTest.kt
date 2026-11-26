package com.mountaya.geo.profile.contracts

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.AmenityFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AmenityFilterTest {
    private val food = RestSchema.WaypointAmenity("food")
    private val water = RestSchema.WaypointAmenity("water")

    @Test
    fun twoFiltersAreEqualWhereTheyOfferTheSameServicesOverTheSameRows() {
        assertEquals(AmenityFilter(listOf(food, water), emptyList()), AmenityFilter(listOf(food, water), emptyList()))
        assertNotEquals(
            AmenityFilter(listOf(water, food), emptyList()),
            AmenityFilter(listOf(food, water), emptyList()),
        )
    }
}
