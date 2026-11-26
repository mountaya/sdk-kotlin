package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchRowPayloadTest {
    @Test
    fun twoPayloadsAreEqualWhereTheyHoldTheSameValuesEachNumberTheSameNumber() {
        assertEquals(
            SearchRowPayload.Place(listOf(6.0, Double.NaN), null),
            SearchRowPayload.Place(listOf(6.0, Double.NaN), null),
        )
        assertNotEquals(
            SearchRowPayload.Place(listOf(0.0, 45.0), null),
            SearchRowPayload.Place(listOf(-0.0, 45.0), null),
        )
        assertEquals(
            SearchRowPayload.Collection("c", "n", null, true),
            SearchRowPayload.Collection("c", "n", null, true),
        )
        assertNotEquals<SearchRowPayload>(
            SearchRowPayload.Zone("z", null, null),
            SearchRowPayload.Itinerary("z", null, null),
        )
    }
}
