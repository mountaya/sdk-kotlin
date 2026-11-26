package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.Mountaya
import kotlin.test.Test
import kotlin.test.assertEquals

class LegEmphasisTargetTest {
    @Test
    fun aMapIsATargetItsEmphasisSlotIsWrittenAndClearedThrough() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_test_a")), geolocation = InertGeolocation())
        val target: LegEmphasisTarget = map
        val line = GeoJsonLineString(listOf(listOf(6.0, 45.0), listOf(6.0, 45.1)))

        target.setGeometry(line, GeometrySlot.Emphasis, "#e8590c")
        assertEquals(SlotGeometry(line, "#e8590c"), map.geometry(GeometrySlot.Emphasis))
        target.clearGeometry(GeometrySlot.Emphasis)
        assertEquals(SlotGeometry(null, GeometrySlot.Emphasis.color), map.geometry(GeometrySlot.Emphasis))
    }
}
