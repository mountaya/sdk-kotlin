package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MapContentTest {
    @Test
    fun aContentHoldsItsSlotItsSourcesItsLayersAndItsRestrictedStretchesApart() {
        val content =
            MapContent(
                ContentSlot.Itinerary,
                sourceIds = listOf("itinerary"),
                layerIds = listOf("line", "point"),
                restrictionLayerIds = listOf("line__road-access-stripe", "line__road-access-icon"),
            )
        assertEquals<ContentSlot>(ContentSlot.Itinerary, content.slot)
        assertEquals(listOf("line", "point"), content.layerIds)
        assertEquals(2, content.restrictionLayerIds.size)
        assertNotEquals(content.copy(layerIds = listOf("line")), content)
    }
}
