package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class AmenityGlyphTest {
    @Test
    fun anAmenitysGlyphIsItsNameUnderTheNamespace() {
        assertEquals("amenity-water", amenityGlyph(RestSchema.WaypointAmenity.Water))
        assertEquals("amenity-medical-post", amenityGlyph(RestSchema.WaypointAmenity("medical-post")))
    }

    @Test
    fun theAmenitiesAreTheThirtyTheContractNamesOnceEach() {
        assertEquals(30, waypointAmenities.size)
        assertEquals(30, waypointAmenities.toSet().size)
        assertEquals("water", waypointAmenities.first().value)
        assertEquals("timing-checkpoint", waypointAmenities.last().value)
    }
}
