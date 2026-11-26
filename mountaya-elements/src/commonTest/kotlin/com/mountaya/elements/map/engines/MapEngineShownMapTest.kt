package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MapEngineShownMapTest {
    private fun map(): MapEngine =
        MapEngine(Mountaya(Credential.Publishable("pk_test_shown")), geolocation = InertGeolocation())

    /** The map's real frame is the one the view laid out last records, and only that view's leaving forgets it. */
    @Test
    fun theViewLaidOutLastDecidesAndOnlyItsLeavingForgetsIt() {
        val map = map()
        assertNull(map.shownMap.value, "no view draws the map yet")

        val first = ShownMap(listOf(0.0, 56.0, 390.0, 844.0), listOf(0.0, 56.0, 390.0, 810.0))
        val second = ShownMap(listOf(0.0, 0.0, 390.0, 400.0), listOf(0.0, 24.0, 390.0, 400.0))
        map.shown(first)
        assertEquals(first, map.shownMap.value)
        map.shown(second)
        assertEquals(second, map.shownMap.value)

        map.unshown(first)
        assertEquals(second, map.shownMap.value, "a view drawing the map no more leaves the last one's place")
        map.unshown(second)
        assertNull(map.shownMap.value)
        map.close()
    }
}
