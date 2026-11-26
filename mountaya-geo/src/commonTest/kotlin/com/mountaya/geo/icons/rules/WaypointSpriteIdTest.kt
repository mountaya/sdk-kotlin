package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class WaypointSpriteIdTest {
    private val hut = RestSchema.WaypointIcon.Hut

    @Test
    fun theIdIsTheIconAndTheColourWrittenSmall() {
        assertEquals("section-icon-hut-3b82f6", waypointSpriteId(hut, "#3b82f6"))
        assertEquals("section-icon-hut-3b82f6", waypointSpriteId(hut, "#3B82F6"))
        assertEquals("section-icon-map-pin-3b82f6", waypointSpriteId(defaultWaypointIcon, DEFAULT_WAYPOINT_COLOR))
    }

    /** One leading `#` goes and only the ASCII capitals fold; every other character stays as written. */
    @Test
    fun aColourIsReadAsTheHexItWrites() {
        assertEquals("section-icon-hut-3b82f6", waypointSpriteId(hut, "3B82F6"))
        assertEquals("section-icon-hut-#abc", waypointSpriteId(hut, "##ABC"))
        assertEquals("section-icon-hut-a#b", waypointSpriteId(hut, "A#B"))
        assertEquals("section-icon-hut-", waypointSpriteId(hut, "#"))
    }

    @Test
    fun anIconThisBuildDoesNotKnowIsWrittenAsTheWireSpellsIt() {
        assertEquals("section-icon-yurt-000000", waypointSpriteId(RestSchema.WaypointIcon("yurt"), "#000000"))
        assertEquals("map-pin", WAYPOINT_FALLBACK_GLYPH)
    }
}
