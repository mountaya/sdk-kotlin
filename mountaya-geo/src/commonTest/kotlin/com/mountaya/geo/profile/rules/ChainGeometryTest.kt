package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChainGeometryTest {
    @Test
    fun aChainIsTheCollectionOfItsLegsGeometriesInItsOrderALegWithNoneLeftOut() {
        val first = GeoJsonLineString(listOf(listOf(6.0, 45.0), listOf(6.0, 45.005)))
        val second = GeoJsonLineString(listOf(listOf(6.0, 45.005), listOf(6.0, 45.01)))
        val geometry =
            chainGeometry(
                listOf(
                    ItineraryLeg(id = "s1", name = "a", geometry = first),
                    ItineraryLeg(id = "t", name = "transfer"),
                    ItineraryLeg(id = "s2", name = "b", geometry = second),
                ),
            )

        assertEquals<GeoJsonGeometry?>(GeoJsonGeometryCollection(listOf(first, second)), geometry)
        assertNull(chainGeometry(listOf(ItineraryLeg(id = "t", name = "transfer"))))
        assertNull(chainGeometry(emptyList()))
    }
}
