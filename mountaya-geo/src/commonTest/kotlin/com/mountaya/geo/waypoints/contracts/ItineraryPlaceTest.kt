package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ItineraryPlaceTest {
    private fun place(
        id: String = "hut",
        lngLat: List<Double> = listOf(6.86, 45.83),
        name: String? = "Refuge de Miage",
        details: WaypointDetails? = WaypointDetails(description = "A bench"),
    ) = ItineraryPlace(id, lngLat, name, "#e03131", RestSchema.WaypointIcon("hut"), details)

    @Test
    fun twoPlacesAreEqualWhenEveryMemberIsAndHashAlike() {
        assertEquals(place(), place())
        assertEquals(place().hashCode(), place().hashCode())
        assertEquals(place(lngLat = listOf(Double.NaN, 45.0)), place(lngLat = listOf(Double.NaN, 45.0)))
        assertEquals(
            place(lngLat = listOf(Double.NaN, 45.0)).hashCode(),
            place(lngLat = listOf(Double.NaN, 45.0)).hashCode(),
        )
        assertNotEquals(place(lngLat = listOf(-0.0, 45.0)), place(lngLat = listOf(0.0, 45.0)))
        assertNotEquals(place(), place(name = null))
        assertNotEquals(place(), place(details = null))
    }

    @Test
    fun anIdAndANameAreComparedCodeUnitForCodeUnit() {
        assertNotEquals(place(id = "café"), place(id = "café"))
        assertNotEquals(place(name = "Châlet"), place(name = "Châlet"))
    }

    @Test
    fun aPlaceBuiltFromItsIdAndPositionStatesNothingElse() {
        val bare = ItineraryPlace("pin", listOf(6.0, 45.0))
        assertNull(bare.name)
        assertNull(bare.color)
        assertNull(bare.glyph)
        assertNull(bare.details)
    }
}
