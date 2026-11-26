package com.mountaya.elements

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WriteLegEmphasisTest {
    /** Slot is a map recording each write of its emphasis slot. */
    private class Slot : LegEmphasisTarget {
        val writes = mutableListOf<String>()

        override fun setGeometry(
            geometry: GeoJsonGeometry?,
            slot: GeometrySlot,
            color: String?,
        ) {
            writes += "set ${slot.id} ${color ?: "own"}"
        }

        override fun clearGeometry(slot: GeometrySlot) {
            writes += "clear ${slot.id}"
        }
    }

    private val leg = GeoJsonLineString(listOf(listOf(6.0, 45.0), listOf(6.0, 45.1)))

    @Test
    fun aLegPointedAtIsWrittenToTheEmphasisSlotInItsColourOrTheMapsOwn() {
        val map = Slot()

        assertTrue(writeLegEmphasis(map, false, leg))
        assertTrue(writeLegEmphasis(map, true, leg, "#e8590c"))
        assertEquals(listOf("set emphasis own", "set emphasis #e8590c"), map.writes)
    }

    @Test
    fun pointingAtNoneClearsOnlyWhatTheElementLit() {
        val map = Slot()

        assertFalse(writeLegEmphasis(map, false, null))
        assertTrue(map.writes.isEmpty())
        assertFalse(writeLegEmphasis(map, true, null))
        assertEquals(listOf("clear emphasis"), map.writes)
    }
}
