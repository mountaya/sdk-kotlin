package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CursorMarkerTargetTest {
    @Test
    fun aMapIsATargetItsMarkersArePinnedEditedAndTakenOffThrough() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_test_a")), geolocation = InertGeolocation())
        val target: CursorMarkerTarget = map

        target.addMarker(MapMarker("a", listOf(6.0, 45.0)))
        target.editMarker("a", position = listOf(7.0, 46.0), color = null, glyph = null, label = null)
        val edited = map.state.value
        assertEquals(listOf(listOf(7.0, 46.0)), edited.markers.map { it.position })
        assertFailsWith<MapException> {
            target.editMarker("b", position = listOf(7.0, 46.0), color = null, glyph = null, label = null)
        }
        target.removeMarker("a")
        val removed = map.state.value
        assertTrue(removed.markers.isEmpty())
    }
}
