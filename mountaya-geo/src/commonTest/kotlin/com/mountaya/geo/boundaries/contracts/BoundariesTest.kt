package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class BoundariesTest {
    /** Geo names the generated type rather than copying it, so a boundary the API answers is one. */
    @Test
    fun aBoundaryIsTheGeneratedType() {
        val boundary: Boundaries = RestSchema.Boundaries(bbox = listOf(5.0, 44.0, 8.0, 47.0), name = "alps")
        val generated: RestSchema.Boundaries = boundary

        assertEquals("alps", generated.name)
        assertEquals(listOf(5.0, 44.0, 8.0, 47.0), boundary.bbox)
    }
}
