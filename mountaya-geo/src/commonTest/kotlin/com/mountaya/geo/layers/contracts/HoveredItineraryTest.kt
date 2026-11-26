package com.mountaya.geo.layers.contracts

import com.mountaya.geo.HoveredItinerary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HoveredItineraryTest {
    @Test
    fun twoAreEqualWhereBothIdsAre() {
        assertEquals(HoveredItinerary("climb", "hut"), HoveredItinerary("climb", "hut"))
        assertEquals(HoveredItinerary("climb", "hut").hashCode(), HoveredItinerary("climb", "hut").hashCode())
        assertEquals(HoveredItinerary(null, null), HoveredItinerary(null, null))
        assertNotEquals(HoveredItinerary("climb", null), HoveredItinerary(null, "climb"))
    }
}
