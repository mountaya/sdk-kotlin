package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AmenityStacksTest {
    private val six =
        listOf("water", "hot-meal", "toilets", "first-aid", "showers", "wifi").map { RestSchema.WaypointAmenity(it) }

    private fun pin(
        key: String,
        amenities: List<RestSchema.WaypointAmenity>?,
    ): CurvePin =
        CurvePin(
            key,
            ProfilePlace(
                ProfileWaypoint.AtDistance(key, 0.0),
                details = amenities?.let { WaypointDetails(amenities = it) },
            ),
            0.0,
            0.0,
            0.0,
            0.0,
        )

    /** The one stack of the place offering the six services, under the maximum given. */
    private fun stackOfSix(max: Double): AmenityStack =
        assertNotNull(amenityStacks(listOf(pin("col#0", six)), max).firstOrNull(), "the place is stacked")

    @Test
    fun sixServicesShowFourAndCountTwoAllNamed() {
        assertEquals(4.0, DEFAULT_AMENITY_STACK_MAX)
        val stack = assertNotNull(amenityStacks(listOf(pin("col#0", six))).firstOrNull(), "the place is stacked")

        assertEquals(4, stack.shown.size)
        assertEquals(2, stack.counted)
        assertEquals(6, stack.named.size)
    }

    @Test
    fun aMaximumIsRoundedDownNoneBelowZeroOrNoNumber() {
        assertEquals(2, stackOfSix(2.7).shown.size)
        assertEquals(6, stackOfSix(-1.0).counted)
        assertEquals(6, stackOfSix(Double.NaN).counted)
        assertTrue(stackOfSix(0.0).shown.isEmpty())
        assertEquals(0, stackOfSix(Double.POSITIVE_INFINITY).counted)
    }

    @Test
    fun aPlaceOfferingNoServiceTakesNoStack() {
        val stacks =
            amenityStacks(
                listOf(
                    pin("start#0", null),
                    pin("gate#0", emptyList()),
                    pin("spring#0", listOf(RestSchema.WaypointAmenity("water"))),
                ),
            )

        assertEquals(listOf("spring#0"), stacks.map { it.pin.key })
    }
}
