package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaceRowTest {
    @Test
    fun aPlaceIsIdentifiedByItsOpenStreetMapIdentity() {
        val row = placeRow(SearchEntity.place(bounds = listOf(6.0, 45.0, 7.0, 46.0)), "ann")
        assertEquals("place:WAY:7", row?.id)
        assertEquals("Annecy", row?.title)
        assertEquals(listOf(0 until 3), row?.matched)
        assertEquals(SearchRowSecondary.Text("Populated place"), row?.secondary)
        assertEquals(SearchRowPayload.Place(listOf(6.13, 45.9), listOf(6.0, 45.0, 7.0, 46.0)), row?.payload)
    }

    @Test
    fun aPlaceWhosePointNamesNoPlaceOrThatHasNoIdentityIsNoRow() {
        assertNull(placeRow(SearchEntity.place(point = listOf(6.13)), ""))
        assertNull(placeRow(SearchEntity.place(point = null), ""))
        assertNull(
            placeRow(
                DataSchema.Place {
                    name = "Annecy"
                    point = listOf(6.13, 45.9)
                },
                "",
            ),
        )
    }

    /**
     * A point that states an altitude still names a place, and the payload is where a map goes, a
     * longitude and a latitude in every SDK.
     */
    @Test
    fun aPointWithAnAltitudeIsARowWhosePayloadDropsTheAltitude() {
        assertEquals(
            SearchRowPayload.Place(listOf(6.13, 45.9), null),
            placeRow(SearchEntity.place(point = listOf(6.13, 45.9, 448.0)), "")?.payload,
        )
    }
}
