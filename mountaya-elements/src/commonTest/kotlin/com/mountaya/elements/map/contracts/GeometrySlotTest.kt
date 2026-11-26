package com.mountaya.elements

import com.mountaya.geo.LayerRank
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeometrySlotTest {
    @Test
    fun theHighlightDrawsAtTheChromeBandThePreviewAndTheEmphasisJustAboveTheItinerary() {
        assertEquals(LayerRank.Chrome.rank, GeometrySlot.Highlight.rank)
        assertEquals(100, GeometrySlot.Highlight.rank)
        assertEquals(LayerRank.ItineraryFocus.rank, GeometrySlot.Preview.rank)
        assertEquals(52, GeometrySlot.Emphasis.rank)
        assertTrue(GeometrySlot.Preview.rank > LayerRank.ItineraryLine.rank)
        assertTrue(GeometrySlot.Preview.rank < LayerRank.ItineraryRestriction.rank)
    }

    @Test
    fun eachSlotHasItsOwnColourAndDrawsFromASourceNamedAfterIt() {
        assertEquals(listOf("preview", "highlight", "emphasis"), GeometrySlot.entries.map { it.id })
        assertEquals(listOf("#2b8a3e", "#f59f00", "#e9501e"), GeometrySlot.entries.map { it.color })
        assertEquals(
            listOf("mountaya-preview", "mountaya-highlight", "mountaya-emphasis"),
            GeometrySlot.entries.map {
                it.sourceId
            },
        )
    }
}
