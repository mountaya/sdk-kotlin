package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ContentSlotTest {
    @Test
    fun twoSlotsAreOneWhereTheyAreOfOneKindAndNameTheSame() {
        assertEquals<ContentSlot>(ContentSlot.Itinerary, ContentSlot.Itinerary)
        assertNotEquals<ContentSlot>(ContentSlot.Itinerary, ContentSlot.Collection)
        assertNotEquals<ContentSlot>(ContentSlot.Itineraries, ContentSlot.Zones)
        assertEquals<ContentSlot>(ContentSlot.Enrichment("hillshade"), ContentSlot.Enrichment("hillshade"))
        assertNotEquals<ContentSlot>(ContentSlot.Enrichment("hillshade"), ContentSlot.Specification("hillshade"))
        assertNotEquals<ContentSlot>(ContentSlot.Specification("café"), ContentSlot.Specification("café"))
        assertNotEquals<ContentSlot>(ContentSlot.Overlay, ContentSlot.Enrichment("overlay"))
        assertEquals<ContentSlot>(
            ContentSlot.Geometry(GeometrySlot.Preview),
            ContentSlot.Geometry(GeometrySlot.Preview),
        )
        assertNotEquals<ContentSlot>(ContentSlot.Analysis("preview"), ContentSlot.Geometry(GeometrySlot.Preview))
        assertEquals<ContentSlot>(ContentSlot.SearchHighlight, ContentSlot.SearchHighlight)
        assertNotEquals<ContentSlot>(ContentSlot.SearchHighlight, ContentSlot.Geometry(GeometrySlot.Highlight))
    }
}
