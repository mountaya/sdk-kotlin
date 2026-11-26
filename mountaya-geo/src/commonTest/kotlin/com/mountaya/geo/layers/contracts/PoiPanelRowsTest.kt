package com.mountaya.geo.layers.contracts

import com.mountaya.geo.PoiPanelRow
import com.mountaya.geo.PoiPanelRows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class PoiPanelRowsTest {
    @Test
    fun aPickersOfferHoldsItsRowsInOrderAndItsMasterRow() {
        val rows = listOf(PoiPanelRow("parking", "parking", keys = listOf("parking"), isVisible = false))
        val offer = PoiPanelRows(rows, isAnyVisible = false)

        assertEquals(rows, offer.rows)
        assertFalse(offer.isAnyVisible)
        assertNotEquals(offer, PoiPanelRows(emptyList(), isAnyVisible = false))
    }
}
