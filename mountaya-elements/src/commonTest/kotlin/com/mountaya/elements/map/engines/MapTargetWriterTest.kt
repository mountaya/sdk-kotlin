package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapTargetWriterTest {
    private object Offline : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse = throw IllegalStateException("offline")
    }

    private fun map(): MapEngine =
        MapEngine(
            Mountaya(Credential.Publishable("pk_test_a"), exchange = Offline, dispatcher = Dispatchers.Unconfined),
        )

    private val leg = GeoJsonLineString(listOf(listOf(6.0, 45.0), listOf(6.0, 45.01)))

    /**
     * A target pins the element's marker and lights its stretch; a target of nothing takes down what it wrote alone.
     */
    @Test
    fun aTargetPinsTheMarkerAndLightsTheStretchAndNothingTakesDownWhatItWrote() {
        val map = map()
        val writer = MapTargetWriter("mtya-test-cursor")

        writer.write(MapTarget(listOf(6.0, 45.01), leg), map, null, "#e8590c")
        assertEquals(
            listOf(listOf(6.0, 45.01)),
            map.state.value.markers
                .map { it.position },
        )
        assertEquals(leg, map.geometry(GeometrySlot.Emphasis).geometry)
        assertTrue(writer.isLit)
        assertNotNull(writer.pinned, "the writer pins the marker")

        writer.write(MapTarget(), map, null, null)
        assertEquals(emptyList(), map.state.value.markers)
        assertNull(map.geometry(GeometrySlot.Emphasis).geometry)
        assertFalse(writer.isLit)
        assertNull(writer.pinned)
    }

    @Test
    fun aWriterThatLitNothingLeavesTheSlotAsAnotherElementWroteIt() {
        val map = map()
        map.setGeometry(leg, GeometrySlot.Emphasis)

        MapTargetWriter("mtya-test-cursor").write(MapTarget(), map, null, null)

        assertEquals(leg, map.geometry(GeometrySlot.Emphasis).geometry)
    }
}
