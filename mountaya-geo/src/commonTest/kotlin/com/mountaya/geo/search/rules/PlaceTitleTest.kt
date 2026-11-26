package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceTitleTest {
    private val english = SearchWording()

    @Test
    fun anEntityIsTitledByItsNameElseItsCategoryOrTag() {
        assertEquals("Annecy", placeTitle(SearchEntity.place(), english))
        assertEquals("Populated place", placeTitle(SearchEntity.place(name = ""), english))
        assertEquals("Cafe", poiTitle(SearchEntity.poi(name = "", category = "CAFE"), english))
        assertEquals("Track", wayTitle(SearchEntity.way(name = null, highway = "track"), english))
        assertEquals("", poiTitle(SearchEntity.poi(name = null, category = null), english))
    }

    @Test
    fun anAddressWithNoNameIsTitledByItsLabel() {
        assertEquals(
            "1 Rue Royale",
            placeTitle(SearchEntity.place(name = "", category = "ADDRESS", label = "1 Rue Royale"), english),
        )
    }
}
